import java.io.File

import io.CsvWriter
import learning.TrainingData
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import r.R
import settings.PredictorSettings
import util.Extensions._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Predictor {
  val owner = PredictorSettings.repositoryOwner
  val repository = PredictorSettings.repositoryName
  val interval = PredictorSettings.modelTrainInterval

  // File names and paths
  val trainFileName = "training.csv"
  val modelFileName = "model.RData"
  val outputFileName = "output.csv"
  val repoDir = new File(new File(PredictorSettings.modelDirectory, owner), repository)
  val trainFile = new File(repoDir, trainFileName)
  val modelFile = new File(repoDir, modelFileName)
  val outputFile = new File(repoDir, outputFileName)

  def main(args: Array[String]): Unit = {
    val action = args.headOption
    val result = action match {
      case Some("train") => train()
      case Some("predict") => predict()
      case _ =>
        println("Unknown action. Possible actions: train, predict.")
        false
    }

    if (result)
      System.exit(0)
    else
      System.exit(1)
  }

  def train(): Boolean = {
    val logger = LoggerFactory.getLogger("Trainer")

    // Check if model needs training
    val expires = new DateTime(modelFile.lastModified).plusDays(interval)
    if (DateTime.now.isBefore(expires)) {
      logger info "Skip - Already recently updated"
      return true
    }

    // Get and save data
    logger info "Data - Start"
    try {
      val data = new TrainingData(owner, repository).get
      CsvWriter.write(trainFile, data)
      logger info s"Data - Created ${data.length} snapshots"
    } catch {
      case e: Throwable =>
        logger error s"Data - Failed - ${e.getMessage}"
        logger error s"Stack trace - Begin\n${e.stackTraceToString}"
        logger error s"Stack trace - End"
        return false
    } finally {
      logger info "Data - End"
    }

    // Train R model
    logger info "Modeling - Start"
    val result = Await.result(R.train(repoDir.getPath), Duration.Inf)

    if (!result)
      logger error "Modeling - Failed"

    logger info "Modeling - End"

    result
  }

  def predict(): Boolean = {
    val logger = LoggerFactory.getLogger("Predictor")

    // Predict with R model
    logger info "Prediction - Start"
    val result = Await.result(R.predict(repoDir.getPath), Duration.Inf)

    if (result.nonEmpty)
      CsvWriter.writeData(outputFile, result.map(r => List(r.toString)))
    else
      logger error "Prediction - Failed"

    logger info "Prediction - End"

    result.nonEmpty
  }
}
