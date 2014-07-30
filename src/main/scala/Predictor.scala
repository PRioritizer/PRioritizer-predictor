import io.CsvWriter
import learning.TrainingData
import org.slf4j.LoggerFactory
import settings.PredictorSettings

object Predictor {
  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger("Predictor")
    val owner = PredictorSettings.repositoryOwner
    val repository = PredictorSettings.repositoryName
    val outputFile = "output.csv"

    val data = new TrainingData(owner, repository).get
    CsvWriter.write(outputFile, data)
  }
}
