package learning

import git.PullRequest
import org.joda.time.{DateTime, Minutes}

class Snapshot(tracker: PullRequestTracker, val moment: DateTime) {

  private lazy val commits = tracker.commits.filter(c => c.createdAt.isBefore(moment))
  private lazy val files = commits.flatMap(_.files).distinct

  private lazy val additions = commits.map(_.additions).sum
  private lazy val deletions = commits.map(_.deletions).sum
  private lazy val fileCount = files.length
  private lazy val commitCount = commits.length

  private lazy val issueCommentsCount = tracker.issueComments.count(c => c.createdAt.isBefore(moment))
  private lazy val reviewCommentsCount = tracker.reviewComments.count(c => c.createdAt.isBefore(moment))
  private lazy val lastComment = tracker.issueComments.union(tracker.reviewComments).filter(c => c.createdAt.isBefore(moment)).lastOption

  private lazy val isCoreMember = tracker.author.coreMember.exists(d => d.isBefore(moment))

  private lazy val authorCommits = tracker.author.commits.count(c => c.createdAt.isBefore(moment))
  private lazy val totalCommits = tracker.repository.commits.count(c => c.createdAt.isBefore(moment))
  private lazy val commitRatio = authorCommits.toDouble / totalCommits.toDouble

  private lazy val previousPullRequests = tracker.author.pullRequests.filter(p => p.createdAt.isBefore(moment))
  private lazy val pullRequestRatio = previousPullRequests.count(p => p.mergedAt.isDefined).toDouble / previousPullRequests.length.toDouble

  private lazy val hasTestCode: Boolean = files.exists(f => f.toLowerCase.contains("test") || f.toLowerCase.contains("spec"))

  val pullRequest: PullRequest = {
    // Create pull request snapshot
    val pullRequest = tracker.pullRequest.copy()

    pullRequest.linesAdded = additions
    pullRequest.linesDeleted = deletions
    pullRequest.filesChanged = fileCount
    pullRequest.commits = commitCount

    pullRequest.comments = issueCommentsCount
    pullRequest.reviewComments = reviewCommentsCount
    pullRequest.lastCommentMention = lastComment.exists(c => c.hasUserMention)

    pullRequest.coreMember = isCoreMember
    pullRequest.contributedCommitRatio = commitRatio
    pullRequest.pullRequestAcceptRatio = pullRequestRatio

    pullRequest.age = minutesAgo(pullRequest.createdAt)

    pullRequest.hasTestCode = hasTestCode

    // TODO: mergeable properties

    pullRequest
  }

  def minutesAgo(time: DateTime): Long =
    Minutes.minutesBetween(tracker.pullRequest.createdAt, moment).getMinutes
}
