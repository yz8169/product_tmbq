# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/11/9
library(optparse)
library(xlsx)
option_list <- list(
make_option("--d", default = "1,2,3,4", type = "character", help = "merge dir,split ,")
)
opt <- parse_args(OptionParser(option_list = option_list))
dirs = unlist(strsplit(opt$d, split = ","))
compoundConfig <- read.xlsx("compound_config.xlsx", 1, check.names = F)
colnames(compoundConfig) <- tolower(colnames(compoundConfig))
compoundNames <- compoundConfig$compound
compoundNames <- as.character(compoundNames)
intensity <- data.frame()
for (dir in dirs) {
    print(dir)
    intensityData <- read.table(quote = "", paste(dir, "/intensity.txt", sep = ""), header = T, com = '', sep = "\t", check.names = F)
    if (nrow(intensity) == 0) {
        intensity <- intensityData
    } else {
        intensity <- merge(intensity, intensityData, by = c("sample", "batch"),sort=F)
    }
}
print(compoundNames)
intensity <- intensity[, c("sample", "batch", compoundNames)]
write.table(intensity, "intensity.txt" , quote = FALSE, sep = "\t", row.names = F)
