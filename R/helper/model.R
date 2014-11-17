# Pull request prediction model

model <- important ~ age + coreMember +
                   # Disabled because of potential new levels in string factor
                   # target + author +
                     intraBranch + containsFix +
                     commitRatio + pullRequestRatio +
                     comments + reviewComments +
                     lastCommentMention +
                     additions + deletions +
                     commits + files +
                     hasTestCode
