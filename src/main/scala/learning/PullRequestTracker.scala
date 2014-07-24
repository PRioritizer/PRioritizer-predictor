package learning

import git.{Event, Comment, PullRequest}
import org.joda.time.DateTime
import settings.PredictorSettings
import util.Extensions._
import util.Window

class PullRequestTracker(pullRequest: PullRequest) {
  val ghId = 0
  val issueEvents = List[Event]()
  val pullRequestEvents = List[Event]()
  val issueComments = List[Comment]()
  val pullRequestComments = List[Comment]() // review comments

  def track: Iterable[(PullRequest, Important)] = {
    val windows = getWindows(pullRequest.createdAt, pullRequest.closedAt).toList
    val pulls = windows.map(w => getPullRequestSnapshot(w.start))
    val important = windows.map(w => isActedUponWithin(w))

    pulls.zip(important)
  }

  def getPullRequestSnapshot(moment: DateTime): PullRequest = {
    null
  }

  def isActedUponWithin(window: Window): Boolean = {
    // Is the pull request merged or closed within the period?
    pullRequestEvents.filter(e => e.createdAt.isWithin(window)).nonEmpty ||
    // Is the pull request mentioned or referenced?
    issueEvents.filter(e => e.createdAt.isWithin(window)).nonEmpty ||
    // Are there any issue comments created within the period?
    issueComments.filter(c => c.createdAt.isWithin(window)).nonEmpty ||
    // Are there any review comments created within the period?
    pullRequestComments.filter(c => c.createdAt.isWithin(window)).nonEmpty
  }

  def getWindows(start: DateTime, end: DateTime): Iterable[Window] = {
    val interval = PredictorSettings.windowInterval * 60 * 1000 // convert minutes to milliseconds
    val range = start.getMillis to (end.getMillis + interval) by interval
    val slidingWindows = range.sliding(2).toIterable
    slidingWindows.map(w => Window(new DateTime(w(0)), new DateTime(w(1))))
  }
}
