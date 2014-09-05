package git

import org.joda.time.DateTime

case class IssueComment(userId: Int, createdAt: DateTime, extRefId: String = "") extends Comment
