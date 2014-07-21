package git

import org.joda.time.{Minutes, DateTime}

case class PullRequest(number: Int,
                       author: String,
                       sha: String,
                       source: String,
                       target: String,
                       title: String,
                       createdAt: DateTime,
                       mergedAt: DateTime,
                       closedAt: DateTime,
                       linesAdded: Long,
                       linesDeleted: Long,
                       filesChanged: Long,
                       commits: Long,
                       comments: Long,
                       reviewComments: Long)
{
  def age(now: DateTime): Long = Minutes.minutesBetween(createdAt, now).getMinutes
}
