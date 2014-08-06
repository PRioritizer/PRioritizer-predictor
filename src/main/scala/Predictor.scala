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

  def main(args: Array[String]): Unit = {
    train()
  }

  def train(): Unit = {
    val owner = PredictorSettings.repositoryOwner
    val repository = PredictorSettings.repositoryName
    val trainFileName = "train.csv"
    val trainDir = new File(new File(PredictorSettings.modelDirectory, owner), repository)
    val trainFile = new File(trainDir, trainFileName)

    // Get and save data
    logger info "Training - Start"
    logger info "Training - Fetch data"
    val data = new TrainingData(owner, repository).get
    CsvWriter.write(trainFile, data)

    // Train R model
    logger info "Training - Modeling"
    val result = Await.result(R.train(trainDir.getPath), Duration.Inf)

    if (result)
      logger info "Training - End"
    else
      logger error "Training - Failed"
  }

  def predict(): Unit = {}
}
