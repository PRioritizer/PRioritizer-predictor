package git

import org.joda.time.DateTime

case class ReviewComment(userId: Int, createdAt: DateTime, extRefId: String = "") extends Comment
