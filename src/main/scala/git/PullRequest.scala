package git

import org.joda.time.DateTime

case class PullRequest(number: Int,
                       author: String,
                       target: String,
                       title: String,
                       intraBranch: Boolean,
                       createdAt: DateTime,
                       closedAt: DateTime)
{
  // Time dependent features
  var linesAdded: Long = 0L
  var linesDeleted: Long = 0L
  var filesChanged: Long = 0L
  var commits: Long = 0L
  var comments: Long = 0L
  var reviewComments: Long = 0L
  var coreMember: Boolean = false
  var contributedCommitRatio: Double = 0D // [0-1]
  var pullRequestAcceptRatio: Double = 0D // [0-1]
  var age: Long = 0L // minutes
}
