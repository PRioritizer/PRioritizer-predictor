# Random forest

suppressPackageStartupMessages(library("randomForest")) # Random forest

random.forest.train <- function(model, train.set) {
  rfmodel <- randomForest(model, data = train.set, importance = TRUE)
  # print(rfmodel)
  # print(importance(rfmodel))
  # varImpPlot(rfmodel, type = 1)
  # varImpPlot(rfmodel, type = 2)
  # plot(rfmodel)
  rfmodel
}

random.forest.predict <- function(trained.model, test.set) {
  predictions <- predict(trained.model, newdata = test.set, type = "prob")
  predictions[,2]
}
