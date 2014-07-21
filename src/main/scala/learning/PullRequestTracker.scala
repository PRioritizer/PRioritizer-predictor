package learning

import git.{Event, Comment, PullRequest}
import org.joda.time.DateTime
import util.Extensions._

class PullRequestTracker(number: Int) {
  val ghId = 0
  val issueEvents = List[Event]()
  val pullRequestEvents = List[Event]()
  val issueComments = List[Comment]()
  val pullRequestComments = List[Comment]() // review comments

  def track: List[(PullRequest, Important)] = {
    val windows = List[(DateTime, DateTime)]()
    val pulls = windows.map(w => getPullRequestSnapshot(w._2))
    val important: List[Important] = windows.map((isActedUponBetween _).tupled)

    pulls.zip(important)
  }

  def getPullRequestSnapshot(moment: DateTime): PullRequest = {
    null
  }

  def isActedUponBetween(now: DateTime, future: DateTime): Boolean = {
    // Is the pull request merged or closed within the period?
    pullRequestEvents.filter(e => e.createdAt.isBetween(now, future)).nonEmpty ||
    // Is the pull request mentioned or referenced?
    issueEvents.filter(e => e.createdAt.isBetween(now, future)).nonEmpty ||
    // Are there any issue comments created within the period?
    issueComments.filter(c => c.createdAt.isBetween(now, future)).nonEmpty ||
    // Are there any review comments created within the period?
    pullRequestComments.filter(c => c.createdAt.isBetween(now, future)).nonEmpty
  }
}
