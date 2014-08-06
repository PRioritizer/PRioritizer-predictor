import java.io.File
import io.CsvWriter
import learning.TrainingData
import org.slf4j.LoggerFactory
import settings.PredictorSettings
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import r.R

object Predictor {
  val logger = LoggerFactory.getLogger("Predictor")
  val owner = PredictorSettings.repositoryOwner
  val repository = PredictorSettings.repositoryName
  val repoDir = new File(new File(PredictorSettings.modelDirectory, owner), repository)

  def main(args: Array[String]): Unit = {
    val action = args.headOption
    action match {
      case Some("train") => train()
      case Some("predict") => predict()
      case _ => println("Unknown action. Possible actions: train, predict.")
    }
  }

  def train(): Unit = {
    val trainFileName = "training.csv"
    val trainFile = new File(repoDir, trainFileName)

    // Get and save data
    logger info "Training - Start"
    logger info "Training - Fetch data"
    val data = new TrainingData(owner, repository).get
    CsvWriter.write(trainFile, data)

    // Train R model
    logger info "Training - Modeling"
    val result = Await.result(R.train(repoDir.getPath), Duration.Inf)

    if (result)
      logger info "Training - End"
    else
      logger error "Training - Failed"
  }

  def predict(): Unit = {
    val outputFileName = "output.csv"
    val outputFile = new File(repoDir, outputFileName)

    // Predict with R model
    logger info "Prediction - Start"
    val result = Await.result(R.predict(repoDir.getPath), Duration.Inf)

    if (result.nonEmpty) {
      CsvWriter.writeData(outputFile, result.map(r => List(r.toString)))
      logger info "Prediction - End"
    } else {
      logger error "Prediction - Failed"
    }
  }
}
