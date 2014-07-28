package git

import org.joda.time.DateTime

case class Commit(createdAt: DateTime, sha: String) {
 var additions: Long = 0L
 var deletions: Long = 0L
 var files: List[String] = List()
}
