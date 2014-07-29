package learning

import git.PullRequest

class TrainingData(owner: String, repository: String) {
  def get: List[(PullRequest, Important)] = {
    val tracker = new RepositoryTracker(owner, repository)
    tracker.getSnapshots.toList
  }
}
