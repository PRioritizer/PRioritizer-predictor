import learning.TrainingData

object Predictor {
  def main(args: Array[String]): Unit = {
    println("it works")

    val data = new TrainingData("scala", "scala").get

    for {
      row <- data
    } println(row)
  }
}
