### Install packages
source("helper/install-packages.R")

### Include helpers
source("algorithms/random-forest.R")
source("helper/data.R")
source("helper/model.R")
source("helper/utils.R")

# ================================== PROGRAM ================================== #

### Files
dir <- "~/git/prioritizer/predictor"
model.file <- paste(dir, "model.RData", sep = "/")

### Input
input <- data.frame(age              = 0,
                    coreMember       = 1,
                    commitRatio      = 0.33,
                    pullRequestRatio = 0.75,
                    comments         = 0,
                    reviewComments   = 0,
                    additions        = 10,
                    deletions        = 5,
                    commits          = 1,
                    files            = 1,
                    important        = 0,
                    stringsAsFactors = FALSE)

#input <- read.data(paste(dir, "xbmc-100.csv", sep = "/"))
data2 <- prepare.data(input)

### Read trained model (access via the trained.model variable)
load(model.file)

### Predict value
predictions <- random.forest.predict(trained.model, data2)
predictions <- as.logical(predictions) # Convert to booleans
output <- paste(predictions, collapse="\n")
printf("%s\n", output)
