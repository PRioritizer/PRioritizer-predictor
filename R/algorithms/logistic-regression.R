# Logistic regression

logistic.regression.train <- function(model, train.set) {
  binlog <- glm(model, data = train.set, family = "binomial")
  # print(summary(binlog))
  binlog
}

logistic.regression.raw <- function(trained.model, test.set) {
  predictions <- predict(trained.model, newdata = test.set)
  predictions
}

logistic.regression.predict <- function(trained.model, test.set) {
  threshold <- 0.5
  predictions <- predict(trained.model, newdata = test.set, type = "response")
  predictions <- predictions >= threshold # Convert to boolean
  predictions <- factor(predictions, c(FALSE, TRUE)) # Convert to factor
  predictions
}
