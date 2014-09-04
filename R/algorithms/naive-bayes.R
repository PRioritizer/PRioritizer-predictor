# Naive Bayes

load.package("e1071") # Bayes

naive.bayes.train <- function(model, train.set) {
  bayesModel <- naiveBayes(model, data = train.set)
  # print(summary(bayesModel))
  # print(bayesModel)
  bayesModel
}

naive.bayes.raw <- function(trained.model, test.set) {
  predictions <- predict(trained.model, newdata = test.set, type = "raw")
  predictions[,2]
}

naive.bayes.predict <- function(trained.model, test.set, threshold = 0.5, limit = -1) {
  predictions <- naive.bayes.raw(trained.model, test.set)
  predictions <- as.boolean.factor(predictions, threshold, limit) # Convert to boolean
  predictions
}
