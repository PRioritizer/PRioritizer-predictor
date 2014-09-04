# Random forest

load.package("randomForest") # Random forest

random.forest.train <- function(model, train.set) {
  rfmodel <- randomForest(model, data = train.set, importance = TRUE)
  # print(rfmodel)
  # print(importance(rfmodel))
  # varImpPlot(rfmodel, type = 1)
  # varImpPlot(rfmodel, type = 2)
  # plot(rfmodel)
  rfmodel
}

random.forest.raw <- function(trained.model, test.set) {
  predictions <- predict(trained.model, newdata = test.set, type = "prob")
  predictions[,2]
}

random.forest.predict <- function(trained.model, test.set, threshold = 0.5, limit = -1) {
  predictions <- random.forest.raw(trained.model, test.set)
  predictions <- as.boolean.factor(predictions, threshold, limit) # Convert to boolean
  predictions
}
