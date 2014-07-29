package learning

import git.{AuthorPullRequest, Commit}
import org.joda.time.DateTime

class AuthorTracker(repository: RepositoryTracker, username: String) {
  val ghAuthorId = 0

  implicit lazy val session = repository.session

  lazy val coreMember: Option[DateTime] = None
  lazy val commits = List[Commit]()
  lazy val pullRequests = List[AuthorPullRequest]()
}