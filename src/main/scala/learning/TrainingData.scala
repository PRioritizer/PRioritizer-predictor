package learning

import git.PullRequest

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TrainingData(owner: String, repository: String) {
  def get: List[(PullRequest, Important)] = {
    val tracker = new RepositoryTracker(owner, repository)
    val snapshots = Await.result(tracker.getSnapshots, Duration.Inf)
    snapshots
  }
}
