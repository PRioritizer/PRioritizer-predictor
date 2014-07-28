package learning

import git.{Commit, Event, Comment, PullRequest}
import org.joda.time.DateTime
import settings.PredictorSettings
import util.Extensions._
import util.Window

class PullRequestTracker(val repository: RepositoryTracker, val pullRequest: PullRequest) {
  val ghPullRequestId = 0
  val ghIssueId = 0

  val author = AuthorTracker.get(pullRequest.author)

  val commits = List[Commit]()
  val issueEvents = List[Event]()
  val pullRequestEvents = List[Event]()
  val issueComments = List[Comment]()
  val reviewComments = List[Comment]()

  // TODO: get lists

  def track: Iterable[(DateTime, PullRequest, Important)] = {
    val windows = getWindows(pullRequest.createdAt, pullRequest.closedAt).toList
    val dates = windows.map(_.start)
    val pulls = dates.map(d => new Snapshot(this, d).get)
    val important = windows.map(w => isActedUponWithin(w))

    (dates, pulls, important).zipped.toIterable
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

  def getWindows(start: DateTime, end: DateTime): Iterable[Window] = {
    val interval = PredictorSettings.windowInterval * 60 * 1000 // convert minutes to milliseconds
    val range = start.getMillis to (end.getMillis + interval) by interval
    val slidingWindows = range.sliding(2).toIterable
    slidingWindows.map(w => Window(new DateTime(w(0)), new DateTime(w(1))))
  }
}
