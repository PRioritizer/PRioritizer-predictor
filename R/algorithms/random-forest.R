# Random forest

load.package("randomForest") # Random forest

random.forest.train <- function(model, train.set) {
  
  minority.size <- nrow(subset(train.set, important == TRUE))
  rfmodel <- randomForest(model, data = train.set, 
                          importance = TRUE, do.trace = 1, ntree = 250,
                          sampsize = c('FALSE' = (2 * minority.size), 'TRUE' = (minority.size - 1)))
  print(rfmodel)
  print(importance(rfmodel))
  varImpPlot(rfmodel, type = 1)
  varImpPlot(rfmodel, type = 2)
  plot(rfmodel)
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
