package learning

class AuthorTrackers(repository: RepositoryTracker) {
  private val trackers = scala.collection.mutable.Map[String, AuthorTracker]()

  def get(username: String) = synchronized {
    trackers.get(username) match {
      case Some(tracker) => tracker
      case None =>
        val tracker = new AuthorTracker(repository, username)
        trackers += (username -> tracker)
        tracker
    }
  }
}
