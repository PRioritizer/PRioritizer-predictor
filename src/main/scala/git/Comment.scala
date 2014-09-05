package git

import org.joda.time.DateTime

trait Comment {
  def userId: Int
  def createdAt: DateTime
  def extRefId: String

  var body: String = ""
  def hasUserMention: Boolean = {
    val regex = "(?:\\s|^)@[a-zA-Z0-9]+".r
    body != null && regex.findFirstMatchIn(body).isDefined
  }
}
