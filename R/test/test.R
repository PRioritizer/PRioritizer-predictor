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
input.file <- file.path(dir, "all.csv")
model.file <- file.path(dir, "model.RData")

### Read data
data <- read.data(input.file)
data <- split.data(data, .90)

### Train model
trained.model <- random.forest.train(model, data$train)
save(trained.model, file = model.file)

### Read trained model (access via the trained.model variable)
rm(trained.model)
load(model.file)

### Predict value
test <- head(data$test, n = 1)
predictions <- random.forest.predict(trained.model, test)
predictions <- as.logical(predictions)
print(predictions)
