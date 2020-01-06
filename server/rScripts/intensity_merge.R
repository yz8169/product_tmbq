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
intensity <- data.frame()
for (dir in dirs) {
    intensityData <- read.table(quote = "", paste(dir, "/intensity.txt", sep = ""), header = T, com = '', sep = "\t", check.names = F)
    if (nrow(intensity) == 0) {
        intensity <- intensityData
    } else {
        intensity <- merge(intensity, intensityData, by = c("sample", "batch"), sort = F)
    }
}
intensity <- intensity[, c("sample", "batch", compoundNames)]
write.table(intensity, "intensity.txt" , quote = FALSE, sep = "\t", row.names = F)
