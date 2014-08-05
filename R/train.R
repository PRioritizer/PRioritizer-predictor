### Install packages
source("helper/install-packages.R")

### Include helpers
source("algorithms/random-forest.R")
source("helper/data.R")
source("helper/model.R")

# ================================== PROGRAM ================================== #

### Files
dir <- "~/git/prioritizer/predictor"
input.file <- paste(dir, "xbmc-100.csv", sep = "/")
output.file <- paste(dir, "model.RData", sep = "/")

### Read data
data <- read.data(input.file)

### Train model
trained.model <- random.forest.train(model, data)
save(trained.model, file = output.file)
