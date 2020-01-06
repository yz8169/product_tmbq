# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/11/9

myStartsWith <- function(str, prefix) {
  substring(str, 1, nchar(prefix)) == prefix
}

dirs <- list.dirs(".", recursive = F)
dirs <- dirs[myStartsWith(dirs, "./is_")]
rtData <- data.frame()
for (dir in dirs) {
  eachData <- read.table(quote = "", paste(dir, "/is_rt.txt", sep = ""), header = T, com = '', sep = "\t",
                         check.names = F)
  if (nrow(rtData) == 0) {
    rtData <- eachData
  } else {
    rtData <- merge(rtData, eachData, by = c("sample"), sort = F)
  }

}
write.table(rtData, "is_rt.txt", quote = FALSE, sep = "\t", row.names = F)
