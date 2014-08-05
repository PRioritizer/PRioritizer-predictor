### Install packages
source("helper/install-packages.R")

### Include helpers
source("algorithms/all.R")
source("helper/data.R")
source("helper/evaluation.R")
source("helper/model.R")
source("helper/utils.R")

# ================================== PROGRAM ================================== #

### Read data
data <- read.data("~/git/prioritizer/predictor/xbmc-100.csv")

### Single run
data <- split.data(data, .75)
results <- models.evaluate(model, data$train, data$test)
print(results)
