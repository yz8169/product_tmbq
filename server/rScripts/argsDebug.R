# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/19
library(pracma)
library(baseline)
library(xcms)
sampleConfig <- read.table(quote = "", "sample_config.txt", header = T, com = '', sep = "\t", check.names = F)
bat=1
config=subset(sampleConfig, batch == bat)
fileNames=config$fileName
unlink("result",TRUE)
dir.create("result")



cdfpath <- "D:/workspaceForIDEA/tmbq_26/pyScripts/mzxml"
cdffiles<-paste(cdfpath,"/",fileNames,".TXT",sep="")
set=xcmsSet(cdffiles,profmethod="binlinbase")
set<-group(set)
set<-fillPeaks(set)
save(set,file="set.RData")

load("set.RData")
peaks = set@peaks
groupval(set, "medret", "into","rt")
peaks=as.data.frame(peaks)
for(i in 1:length(fileNames)){
    fileName=fileNames[i]
    rt = 7.43
    rtWin = 0.1
    rtMin = (rt - rtWin) * 60
    rtMax = (rt + rtWin) * 60
    data <- read.table(quote = "", paste("out_dir/",fileName,".TXT",sep=""), header = T, com = '', sep = "\t", check.names = F)
    # png(file = paste("result_1/",fileName,".png",paste=""), width = 1200, height = 900)
    png(file = paste("result/",fileName,".png",paste=""), width = 1200, height = 900)
    par(mfrow = c(3, 1))
    originalData = data$INT
    colnames(data)=c("SEC","MZ","INT")
    tmpI=i
    data$SEC=set@rt$corrected[[tmpI]]
    plot(data$SEC , originalData, col = "red", cex = 0.5)
    lines(data$SEC, originalData, col = "grey")
    abline(v = c(rtMin, rtMax), col = "blue")
    abline(v = c(8.63*60,8.83*60), col = "blue")
    abline(v = c(10.1*60,10.3*60), col = "blue")
    f1 = 7
    smoothData <- savgol(data$INT, f1)
    plot(data$SEC , smoothData, col = "red", cex = 0.5)
    lines(data$SEC, smoothData, col = "grey")
    baseLineFrame <- data.frame(Date = data$SEC, Visits = smoothData)
    baseLineFrame <- t(baseLineFrame$Visits)
    baseLine <- baseline(baseLineFrame, method = 'irls')
    correctValue = c(getCorrected(baseLine))
    plot(data$SEC , correctValue, col = "red", cex = 0.5)
    lines(data$SEC, correctValue, col = "grey")
    data$INT = correctValue

    # raw = new("xcmsRaw")
    # raw@env$mz = data$MZ
    # raw@scantime = data$SEC
    # numPoints = length(data$INT)
    # raw@scanindex = 0 : (numPoints - 1)
    # raw@env$intensity = data$INT
    # fwhm = 30
    # peak = findPeaks.matchedFilter(raw, fwhm = fwhm)
    # peak=as.data.frame(peak)

    peak<-subset(peaks,sample==tmpI)

    filterPeak = subset(peak, rt >= rtMin & rt <= rtMax)
    filterPeak
    abline(v = peak$rt)
    abline(v = peak$rtmin, col = "red")
    abline(v = peak$rtmax, col = "red")
    # abline(v = c(rtMin, rtMax), col = "blue")
    # text(peak$rt[1],0,paste(peak$rt,sep=","))
    dev.off()
    intensityMethod = "all"
    response = "area"
    totalInt = 0
    getSub <- function(data, filterPeak, response){
        if (response == "height") {
            subData = subset(data, SEC == filterPeak[i, "rt"])
        }else if (response == "area") {
            subData = subset(data, SEC > filterPeak[i, "rtmin"] & SEC <= filterPeak[i, "rtmax"])
        }
        subData
    }
    if (intensityMethod == "all") {
        for (i in 1 : nrow(filterPeak)) {
            subData = getSub(data, filterPeak, response)
            totalInt = sum(subData$INT) + totalInt
        }
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
        subData = getSub(data, firstRow, response)
        totalInt = sum(subData$INT) + totalInt
    }else if (intensityMethod == "largest") {
        maxArea = 0
        for (i in 1 : nrow(filterPeak)) {
            subData = getSub(data, filterPeak, "area")
            area = sum(subData$INT)
            if (area >= maxArea) {
                maxArea = area
                firstRow = filterPeak[i,]
            }
        }
        subData = getSub(data, firstRow, response)
        totalInt = sum(subData$INT) + totalInt
    }else if(intensityMethod == "nearest"){
        minDif = 0
        for (i in 1 : nrow(filterPeak)) {
            dif = abs(filterPeak[i, "rt"] - rt*60)
            if (dif <= minDif) {
                firstRow = filterPeak[i,]
                minDif = dif
            }else if (minDif == 0) {
                minDif = dif
                firstRow = filterPeak[i,]
            }
        }
        subData = getSub(data, firstRow, response)
        totalInt = sum(subData$INT) + totalInt
    }
}

