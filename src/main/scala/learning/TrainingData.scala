package learning

import git.PullRequest
import org.joda.time.DateTime

class TrainingData(owner: String, repository: String) {
  def get: List[(PullRequest, Important)] = {
    List()
  }
}
