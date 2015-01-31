# Support Vector Machine

load.package("e1071") # Support Vector Machine

svm.train <- function(model, train.set) {
  svmmodel <- svm(model, data = train.set,
                  type = "C-classification", probability = TRUE, scale = FALSE)
  # print(svmmodel)
  # plot(svmmodel)
  svmmodel
}

svm.raw <- function(trained.model, test.set) {
  predictions <- predict(trained.model, test.set, probability = TRUE)
  predictions <- attr(predictions, "probabilities")
  predictions[,2]
}

svm.predict <- function(trained.model, test.set, threshold = 0.5, limit = -1) {
  predictions <- svm.raw(trained.model, test.set)
  predictions <- as.boolean.factor(predictions, threshold, limit) # Convert to boolean
  predictions
}
