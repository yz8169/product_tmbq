# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/11/11
# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/11/9
myStartsWith <- function(str, prefix){
    substring(str, 1, nchar(prefix)) == prefix
}

library(xlsx)

dirs <- list.dirs(".", recursive = F)
dirs <- dirs[myStartsWith(dirs, "./is_") | myStartsWith(dirs, "./c_")]
compoundConfig <- read.xlsx("compound_config.xlsx", 1, check.names = F)
colnames(compoundConfig) <- tolower(colnames(compoundConfig))
compoundNames <- compoundConfig$compound
compoundNames <- as.character(compoundNames)
color = data.frame()
regress <- data.frame()
for (dir in dirs) {
    data = read.table(quote = "", paste(dir, "/color.txt", sep = ""), header = T, com = '', sep = "\t", check.names = F)
    regressData <- read.table(quote = "", paste(dir, "/regress.txt", sep = ""), header = T, com = '', sep = "\t", check.names = F)
    if (nrow(color) == 0) {
        color <- data
        regress <- regressData
    } else {
        color <- merge(color, data, by = c("sample", "batch"),sort=F)
        regress <- merge(regress, regressData, by = c("sample", "batch"),sort=F)
    }
    # unlink(dir)
}
color <- color[, c("sample", "batch", compoundNames)]
regress <- regress[, c("sample", "batch", compoundNames)]
write.table(color, "color.txt" , quote = FALSE, sep = "\t", row.names = F)
write.table(regress, "regress.txt" , quote = FALSE, sep = "\t", row.names = F,na="")

