package learning

import ghtorrent.Schema.Tables
import git.{Commit, Event, Comment, PullRequest}
import org.joda.time.DateTime
import settings.{MongoDbSettings, PredictorSettings}
import util.Extensions._
import util.Window
import scala.slick.driver.MySQLDriver.simple._
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class PullRequestTracker(val repository: RepositoryTracker, val pullRequest: PullRequest) {
  implicit lazy val session = repository.session
  lazy val mongo = repository.mongo

  lazy val author = repository.authors.get(pullRequest.author)
  lazy val windows = getWindows(pullRequest.createdAt, pullRequest.closedAt).toList

  // Manual implement laziness (because lazy freezes on Await?)
  private var _ghPullRequestId: Int = -1
  def ghPullRequestId = {
    if (_ghPullRequestId == -1)
      _ghPullRequestId = Await.result(getPullRequestId, Duration.Inf)
    _ghPullRequestId
  }

  private var _ghIssueId: Int = -1
  def ghIssueId = {
    if (_ghIssueId == -1)
      _ghIssueId = Await.result(getIssueId, Duration.Inf)
    _ghIssueId
  }

  private var _commits: List[Commit] = null
  def commits = {
    if (_commits == null)
      _commits = Await.result(getCommits, Duration.Inf)
    _commits
  }

  private var _issueEvents: List[Event] = _
  def issueEvents = {
    if (_issueEvents == null)
      _issueEvents = Await.result(getIssueEvents, Duration.Inf)
    _issueEvents
  }

  private var _pullRequestEvents: List[Event] = _
  def pullRequestEvents = {
    if (_pullRequestEvents == null)
      _pullRequestEvents = Await.result(getPullRequestsEvents, Duration.Inf)
    _pullRequestEvents
  }

  private var _issueComments: List[Comment] = _
  def issueComments = {
    if (_issueComments == null)
      _issueComments = Await.result(getIssueComments, Duration.Inf)
    _issueComments
  }

  private var _reviewComments: List[Comment] = _
  def reviewComments = {
    if (_reviewComments == null)
      _reviewComments = Await.result(getReviewComments, Duration.Inf)
    _reviewComments
  }

  def track: Future[Iterable[(PullRequest, Important)]] = Future {
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

  private def getPullRequestId: Future[Int] = Future {
    val pullRequests = for {
      p <- Tables.pullRequests
      if p.number === pullRequest.number
      if p.baseRepoId === repository.ghRepoId
    } yield p.id

    pullRequests.first
  }

  private def getIssueId: Future[Int] = Future {
    val issues = for {
      i <- Tables.issues
      if i.pullRequestId === ghPullRequestId
    } yield i.id

    issues.first
  }

  private def getCommits: Future[List[Commit]] = Future {
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
    Await.result(Future.sequence(commits.list.map(enrichFromMongo)), Duration.Inf)
  }

  private def enrichFromMongo(commit: Commit): Future[Commit] = Future {
    val fields = List(
      "stats.additions",
      "stats.deletions",
      "files.filename")

    val obj = mongo.getBySha(MongoDbSettings.collectionCommits, commit.sha, fields)

    commit.additions = obj.getOrElse("stats.additions", 0).asInstanceOf[Int]
    commit.deletions = obj.getOrElse("stats.deletions", 0).asInstanceOf[Int]
    commit.files = obj.getOrElse("files.filename", List[String]()).asInstanceOf[List[String]]

    commit
  }

  private def getIssueEvents: Future[List[Event]] = Future {
    val events = for {
      // From
      ie <- Tables.issueEvents
      // Where
      if ie.issueId === ghIssueId
    } yield ie

    events.list
  }

  private def getPullRequestsEvents: Future[List[Event]] = Future {
    val events = for {
      // From
      prh <- Tables.pullRequestHistory
      // Where
      if prh.pullRequestId === ghPullRequestId
    } yield prh

    events.list
  }

  private def getIssueComments: Future[List[Comment]] = Future {
    val comments = for {
      // From
      c <- Tables.comments
      // Where
      if c.issueId === ghIssueId
    } yield c

    comments.list
  }

  private def getReviewComments: Future[List[Comment]] = Future {
    val comments = for {
      // From
      c <- Tables.reviewComments
      // Where
      if c.pullRequestId === ghPullRequestId
    } yield c

    comments.list
  }
}
