# Pull request prediction model

model <- important ~ age + coreMember +
                     commitRatio + pullRequestRatio +
                     comments + reviewComments +
                     additions + deletions +
                     commits + files