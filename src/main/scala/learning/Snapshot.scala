package learning

import git.PullRequest
import org.joda.time.DateTime

class Snapshot(tracker: PullRequestTracker, moment: DateTime) {
  val additions = tracker.commits.filter(c => c.createdAt.isBefore(moment)).map(_.additions).sum
  val deletions = tracker.commits.filter(c => c.createdAt.isBefore(moment)).map(_.deletions).sum
  val fileCount = tracker.commits.filter(c => c.createdAt.isBefore(moment)).flatMap(_.files).distinct.length
  val commitCount = tracker.commits.count(c => c.createdAt.isBefore(moment))

  val issueCommentsCount = tracker.issueComments.count(c => c.createdAt.isBefore(moment))
  val reviewCommentsCount = tracker.reviewComments.count(c => c.createdAt.isBefore(moment))

  val isCoreMember = tracker.author.coreMember.exists(d => d.isBefore(moment))

  val authorCommits = tracker.author.commits.count(c => c.createdAt.isBefore(moment))
  val totalCommits = tracker.repository.commits.count(c => c.createdAt.isBefore(moment))
  val commitRatio = authorCommits.toDouble / totalCommits.toDouble

  val previousPullRequests = tracker.author.pullRequests.filter(p => p.createdAt.isBefore(moment))
  val pullRequestRatio = previousPullRequests.count(p => p.mergedAt.isDefined).toDouble / previousPullRequests.length.toDouble

  def get: PullRequest = getPullRequest

  def getPullRequest: PullRequest = {
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

    // TODO: mergeable properties

    pullRequest
  }
}
