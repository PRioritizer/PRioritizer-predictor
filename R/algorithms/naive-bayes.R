# Naive Bayes

suppressPackageStartupMessages(library("e1071")) # Bayes

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

naive.bayes.predict <- function(trained.model, test.set) {
  predictions <- predict(trained.model, newdata = test.set, type = "class")
  predictions
}
