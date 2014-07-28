package learning

import git.Commit

class RepositoryTracker(owner: String, repository: String) {
  lazy val commits = List[Commit]()

  // TODO: get lists
}
