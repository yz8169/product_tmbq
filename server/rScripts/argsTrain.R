# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/26
library(pracma)
library(baseline)
name <- "TwMCA"
sampleConfig <- read.table(quote = "", "sample_config.txt", header = T, com = '', sep = "\t", check.names = F)
bat=2
colnames(sampleConfig)[2] <- "fileName"
config = subset(sampleConfig, batch == bat)
fileNames = config$fileName
files <- paste("dta/",name,"/", fileNames,".dta", sep = "")
fl=23
createWhenNoExist <- function(f){
    ! dir.exists(f) && dir.create(f)
}
for (file in files) {
    fileName = basename(file)
    data <- read.table(quote = "", file, header = T, com = '', sep = "\t", check.names = F)
    colnames(data) = c("SEC", "MZ", "INT")
    smoothData <- savgol(data$INT, as.numeric(fl))
    data$INT <- smoothData
    baseLineFrame <- data.frame(Date = data$SEC, Visits = smoothData)
    baseLineFrame <- t(baseLineFrame$Visits)
    baseLine <- baseline(baseLineFrame)
    correctValue = c(getCorrected(baseLine))
    data$INT = correctValue
    dirName="train"
    nameDir <- paste(dirName, name, sep = "/")
    createWhenNoExist(nameDir)
    colnames(data) = c("#SEC", "MZ", "INT")
    write.table(data, paste("train", name, fileName, sep = "/") , quote = FALSE, sep = "\t", row.names = F)
}
