# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/25
library(xcms)
mzxmlPath <- paste("mzxml", "TwMCA", sep = "/")
dataPath <- paste("dta", "TwMCA", sep = "/")
sampleConfig <- read.table(quote = "", "sample_config.txt", header = T, com = '', sep = "\t", check.names = F,
na.strings = NULL, skipNul = T)
colnames(sampleConfig)[2] <- "fileName"
# uniqBatch = unique(sampleConfig$batch)
uniqBatch = c(2)
intensity = data.frame()
bat=3
# for (bat in uniqBatch) {
    config = subset(sampleConfig, batch == bat)
    fileNames = config$fileName
    mzxmlFiles <- paste(mzxmlPath, "/", fileNames, ".dta", sep = "")
    set = xcmsSet(mzxmlFiles,profmethod="binlinbase")
    set <- group(set)
    # set <- group(set)
    set <-  retcor.obiwarp(set,profStep=0.1,distFunc= "cov")
    set <- group(set)
    set
    set <- fillPeaks(set)
    peaks = set@peaks
    peaks = as.data.frame(peaks)
    peaks
    pdf(file = paste("pngs/TwMCA", "_", bat, ".pdf", paste = ""), width = 15, height = 9)
    for (i in 1 : length(fileNames)) {
        fileName = fileNames[i]
        rt = 7.43
        rtWin = 0.2
        rtMin = (rt - rtWin) * 60
        rtMax = (rt + rtWin) * 60
        data <- read.table(quote = "", paste(dataPath, "/", fileName, ".dta", sep = ""), header = T, com = '', sep = "\t", check.names = F)
        colnames(data) = c("SEC", "MZ", "INT")
        tmpI = i
        plot(data$SEC , data$INT, col = "red", cex = 0.5, main = fileName)
        lines(data$SEC, data$INT, col = "grey")
        # abline(v = c(rtMin, rtMax), col = "blue")

        peak <- subset(peaks, sample == tmpI)
        filterPeak = subset(peak, rt >= rtMin & rt <= rtMax)
        filterPeak
        abline(v = peak$rt)
        abline(v = peak$rtmin,col="red")
        abline(v = peak$rtmax,col="red")
        intensityMethod = "all"
        response = "area"
        totalInt = 0
        getSumVec <- function(filterPeak, response){
            if (response == "height") {
                vec = filterPeak$maxo
            }else if (response == "area") {
                vec = filterPeak$into
            }
            vec
        }
        if (intensityMethod == "all") {
            vec = getSumVec(filterPeak, response)
            totalInt = sum(vec) + totalInt
        }else if (intensityMethod == "first") {
            minDif = 0
            for (i in 1 : nrow(filterPeak)) {
                dif = filterPeak[i, "rt"] - rtMin
                if (dif <= minDif) {
                    firstRow = filterPeak[i,]
                    minDif = dif
                }else if (minDif == 0) {
                    minDif = dif
                    firstRow = filterPeak[i,]
                }
            }
            vec = getSumVec(firstRow, response)
            totalInt = sum(vec) + totalInt
        }else if (intensityMethod == "largest") {
            maxArea = 0
            for (i in 1 : nrow(filterPeak)) {
                area = filterPeak[i,]$into
                if (area >= maxArea) {
                    maxArea = area
                    firstRow = filterPeak[i,]
                }
            }
            vec = getSumVec(firstRow, response)
            totalInt = sum(vec) + totalInt
        }else if (intensityMethod == "nearest") {
            minDif = 0
            for (i in 1 : nrow(filterPeak)) {
                dif = abs(filterPeak[i, "rt"] - rt * 60)
                if (dif <= minDif) {
                    firstRow = filterPeak[i,]
                    minDif = dif
                }else if (minDif == 0) {
                    minDif = dif
                    firstRow = filterPeak[i,]
                }
            }
            vec = getSumVec(firstRow, response)
            totalInt = sum(vec) + totalInt
        }
        intensity[tmpI, "batch"] = bat
        intensity[tmpI, "sample"] = fileName
        intensity[tmpI, "TwMCA"] = totalInt
    }
    dev.off()
# }
intensity
write.table(intensity, "intensity.txt" , quote = FALSE, sep = "\t", row.names = F)









