import java.io.File
import io.CsvWriter
import learning.TrainingData
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import settings.PredictorSettings
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import r.R

object Predictor {
  val owner = PredictorSettings.repositoryOwner
  val repository = PredictorSettings.repositoryName
  val interval = PredictorSettings.modelTrainInterval

  // File names and paths
  val trainFileName = "training.csv"
  val outputFileName = "output.csv"
  val repoDir = new File(new File(PredictorSettings.modelDirectory, owner), repository)
  val trainFile = new File(repoDir, trainFileName)
  val outputFile = new File(repoDir, outputFileName)

  def main(args: Array[String]): Unit = {
    val action = args.headOption
    action match {
      case Some("train") => train()
      case Some("predict") => predict()
      case _ => println("Unknown action. Possible actions: train, predict.")
    }
  }

  def train(): Unit = {
    val logger = LoggerFactory.getLogger("Trainer")

    // Check if model needs training
    val expires = new DateTime(trainFile.lastModified).plusDays(interval)
    if (DateTime.now.isBefore(expires)) {
      logger info "Skip - Already recently updated"
      return
    }

    // Get and save data
    logger info "Data - Start"
    val data = new TrainingData(owner, repository).get
    CsvWriter.write(trainFile, data)
    logger info s"Data - Created ${data.length} snapshots"
    logger info "Data - End"

    // Train R model
    logger info "Modeling - Start"
    val result = Await.result(R.train(repoDir.getPath), Duration.Inf)

    if (!result)
      logger error "Failed"

    logger info "Modeling - End"
  }

  def predict(): Unit = {
    val logger = LoggerFactory.getLogger("Predictor")

    // Predict with R model
    logger info "Prediction - Start"
    val result = Await.result(R.predict(repoDir.getPath), Duration.Inf)

    if (result.nonEmpty)
      CsvWriter.writeData(outputFile, result.map(r => List(r.toString)))
    else
      logger error "Prediction - Failed"

    logger info "Prediction - End"
  }
}
