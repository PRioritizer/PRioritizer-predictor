# Logistic regression

logistic.regression.train <- function(model, train.set) {
  binlog <- glm(model, data = train.set, family = "binomial")
  # print(summary(binlog))
  binlog
}

logistic.regression.predict <- function(trained.model, test.set) {
  predictions <- predict(trained.model, newdata = test.set)
  predictions
}
