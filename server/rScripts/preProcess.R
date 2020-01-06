# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/25
createWhenNoExist <- function(f){
    ! dir.exists(f) && dir.create(f)
}

library(pracma)
library(baseline)
source("base.R")
compound <- read.table(quote = "", "compound_config.txt", header = T, com = '', sep = "\t", check.names = F)
compound<-setCompoundConfigHeader(compound)
dirName = "preProcess"
dir.create("preProcess")
for (i in 1 : nrow(compound)) {
    row <- compound[i,]
    name <- row$compound
    files <- list.files(paste("dta", name, sep = "/"), recursive = TRUE, full.names = TRUE)
    for (file in files) {
        fileName = basename(file)
        data <- read.table(quote = "", file, header = T, com = '', sep = "\t", check.names = F)
        colnames(data) = c("SEC", "MZ", "INT")
        smoothData <- savgol(data$INT, as.numeric(row$fl))
        data$INT <- smoothData
        baseLineFrame <- data.frame(Date = data$SEC, Visits = smoothData)
        baseLineFrame <- t(baseLineFrame$Visits)
        baseLine <- baseline(baseLineFrame)
        correctValue = c(getCorrected(baseLine))
        data$INT = correctValue
        nameDir <- paste(dirName, name, sep = "/")
        createWhenNoExist(nameDir)
        colnames(data) = c("#SEC", "MZ", "INT")
        write.table(data, paste("preProcess", name, fileName, sep = "/") , quote = FALSE, sep = "\t", row.names = F)
    }
}



