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
data <- read.data("test/csv/angular-5000.csv")

### Single run
data <- split.data(data, .75)
results <- models.evaluate(model, data$train, data$test)
print(results)
