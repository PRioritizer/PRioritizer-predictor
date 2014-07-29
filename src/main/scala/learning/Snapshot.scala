package learning

import git.PullRequest
import org.joda.time.{Minutes, DateTime}

class Snapshot(tracker: PullRequestTracker, val moment: DateTime) {

  private lazy val additions = tracker.commits.filter(c => c.createdAt.isBefore(moment)).map(_.additions).sum
  private lazy val deletions = tracker.commits.filter(c => c.createdAt.isBefore(moment)).map(_.deletions).sum
  private lazy val fileCount = tracker.commits.filter(c => c.createdAt.isBefore(moment)).flatMap(_.files).distinct.length
  private lazy val commitCount = tracker.commits.count(c => c.createdAt.isBefore(moment))

  private lazy val issueCommentsCount = tracker.issueComments.count(c => c.createdAt.isBefore(moment))
  private lazy val reviewCommentsCount = tracker.reviewComments.count(c => c.createdAt.isBefore(moment))

  private lazy val isCoreMember = tracker.author.coreMember.exists(d => d.isBefore(moment))

  private lazy val authorCommits = tracker.author.commits.count(c => c.createdAt.isBefore(moment))
  private lazy val totalCommits = tracker.repository.commits.count(c => c.createdAt.isBefore(moment))
  private lazy val commitRatio = authorCommits.toDouble / totalCommits.toDouble

  private lazy val previousPullRequests = tracker.author.pullRequests.filter(p => p.createdAt.isBefore(moment))
  private lazy val pullRequestRatio = previousPullRequests.count(p => p.mergedAt.isDefined).toDouble / previousPullRequests.length.toDouble

  val pullRequest: PullRequest = {
    // Create pull request snapshot
    val pullRequest = tracker.pullRequest.copy()

    pullRequest.linesAdded = additions
    pullRequest.linesDeleted = deletions
    pullRequest.filesChanged = fileCount
    pullRequest.commits = commitCount

    pullRequest.comments = issueCommentsCount
    pullRequest.reviewComments = reviewCommentsCount

    pullRequest.coreMember = isCoreMember
    pullRequest.contributedCommitRatio = commitRatio
    pullRequest.pullRequestAcceptRatio = pullRequestRatio

    pullRequest.age = minutesAgo(pullRequest.createdAt)

    // TODO: mergeable properties

    pullRequest
  }

  def minutesAgo(time: DateTime): Long =
    Minutes.minutesBetween(tracker.pullRequest.createdAt, moment).getMinutes
}
