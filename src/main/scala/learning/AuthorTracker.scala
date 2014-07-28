package learning

import git.{PullRequest, Commit}
import org.joda.time.DateTime

class AuthorTracker(username: String) {
  val ghAuthorId = 0

  val coreMember: Option[DateTime] = null
  val commits = List[Commit]()
  val pullRequests = List[PullRequest]()

  // TODO: get lists
}

object AuthorTracker {
  private val trackers = scala.collection.mutable.Map[String, AuthorTracker]()

  def get(username: String) = trackers.get(username) match {
    case Some(tracker) => tracker
    case None =>
      val tracker = new AuthorTracker(username)
      trackers += (username -> tracker)
      tracker
  }
}