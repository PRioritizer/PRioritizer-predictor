package git

import org.joda.time.DateTime

case class AuthorPullRequest(createdAt: DateTime, mergedAt: Option[DateTime])
