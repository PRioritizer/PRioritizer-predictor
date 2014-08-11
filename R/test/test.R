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
dir <- "~/git/prioritizer/predictor/csv"
input.file <- paste(dir, "xbmc-100.csv", sep = "/")
model.file <- paste(dir, "model.RData", sep = "/")

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
test <- head(data$test, n = 1)
predictions <- random.forest.predict(trained.model, test)
predictions <- as.logical(predictions)
print(predictions)

