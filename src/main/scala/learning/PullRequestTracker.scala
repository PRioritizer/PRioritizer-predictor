package learning

import ghtorrent.Schema.Tables
import git.{Commit, Event, Comment, PullRequest}
import org.joda.time.DateTime
import settings.{MongoDbSettings, PredictorSettings}
import util.Extensions._
import util.Window
import scala.slick.driver.MySQLDriver.simple._

class PullRequestTracker(val repository: RepositoryTracker, val pullRequest: PullRequest) {
  implicit lazy val session = repository.session
  lazy val mongo = repository.mongo

  lazy val author = repository.authors.get(pullRequest.author)
  // TODO: limit window count
  lazy val windows = getWindows(pullRequest.createdAt, pullRequest.closedAt).toList

  lazy val ghPullRequestId = getPullRequestId
  lazy val ghIssueId = getIssueId

  lazy val commits = getCommits
  lazy val issueEvents = getIssueEvents
  lazy val pullRequestEvents = getPullRequestsEvents
  lazy val issueComments = getIssueComments
  lazy val reviewComments = getReviewComments

  def track: Iterable[(PullRequest, Important)] = {
    val dates = windows.map(_.start)
    val pulls = dates.map(d => new Snapshot(this, d).pullRequest)
    val important = windows.map(w => isActedUponWithin(w))

    (pulls, important).zipped.toIterable
  }

  def isActedUponWithin(window: Window): Boolean = {
    // Is the pull request merged or closed within the period?
    pullRequestEvents.filter(e => e.createdAt.isWithin(window)).nonEmpty ||
    // Is the pull request mentioned or referenced?
    issueEvents.filter(e => e.createdAt.isWithin(window)).nonEmpty ||
    // Are there any issue comments created within the period?
    issueComments.filter(c => c.createdAt.isWithin(window)).nonEmpty ||
    // Are there any review comments created within the period?
    reviewComments.filter(c => c.createdAt.isWithin(window)).nonEmpty
  }

  private def getWindows(start: DateTime, end: DateTime): Iterable[Window] = {
    val interval = PredictorSettings.windowInterval * 60 * 1000 // convert minutes to milliseconds
    val range = start.getMillis to (end.getMillis + interval) by interval
    val slidingWindows = range.sliding(2).toIterable
    slidingWindows.map(w => Window(new DateTime(w(0)), new DateTime(w(1))))
  }

  private def getPullRequestId: Int = {
    val pullRequests = for {
      p <- Tables.pullRequests
      if p.number === pullRequest.number
      if p.baseRepoId === repository.ghRepoId
    } yield p.id

    pullRequests.first
  }

  private def getIssueId: Int = {
    val issues = for {
      i <- Tables.issues
      if i.pullRequestId === ghPullRequestId
    } yield i.id

    issues.first
  }

  private def getCommits: List[Commit] = {
    val commits = for {
      // From
      pc <- Tables.pullRequestCommits
      c <- Tables.commits
      // Join
      if c.id === pc.commitId
      // Where
      if pc.pullRequestId === ghPullRequestId
    } yield c

    // Get commit info from MongoDB
    commits.list.map(enrichFromMongo)
  }

  private def enrichFromMongo(commit: Commit): Commit = {
    val fields = List(
      "stats.additions",
      "stats.deletions",
      "files.filename")

    val obj = mongo.getBySha(MongoDbSettings.collectionCommits, commit.sha, fields)

    commit.additions = obj.get("stats.additions").get.asInstanceOf[Int]
    commit.deletions = obj.get("stats.deletions").get.asInstanceOf[Int]
    commit.files = obj.get("files.filename").get.asInstanceOf[List[String]]

    commit
  }

  private def getIssueEvents: List[Event] = {
    val events = for {
      // From
      ie <- Tables.issueEvents
      // Where
      if ie.issueId === ghIssueId
    } yield ie

    events.list
  }

  private def getPullRequestsEvents: List[Event] = {
    val events = for {
      // From
      prh <- Tables.pullRequestHistory
      // Where
      if prh.pullRequestId === ghPullRequestId
    } yield prh

    events.list
  }

  private def getIssueComments: List[Comment] = {
    val comments = for {
      // From
      c <- Tables.comments
      // Where
      if c.issueId === ghIssueId
    } yield c

    comments.list
  }

  private def getReviewComments: List[Comment] = {
    val comments = for {
      // From
      c <- Tables.reviewComments
      // Where
      if c.pullRequestId === ghPullRequestId
    } yield c

    comments.list
  }
}
