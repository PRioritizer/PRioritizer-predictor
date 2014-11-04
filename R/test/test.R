### Install packages
source("helper/install-packages.R")

### Include helpers
source("algorithms/all.R")
source("helper/data.R")
source("helper/evaluation.R")
source("helper/model.R")
source("helper/utils.R")

# ================================== PROGRAM ================================== #

### Files
dir <- "test/csv"
input.file <- file.path(dir, "angular.csv")
model.file <- file.path(dir, "model.RData")

### Read data
data <- read.data(input.file)
data <- split.data(data, .75)

### Train model
trained.model <- random.forest.train(model, data$train)
save(trained.model, file = model.file)

### Read trained model (access via the trained.model variable)
rm(trained.model)
load(model.file)

### Predict value
test <- head(data$test, n = 5)
predictions <- random.forest.raw(trained.model, test)
predictions <- as.double(predictions)
print(predictions)
