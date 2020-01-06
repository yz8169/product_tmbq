# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/10/15
getMedian <- function(originalData){
    times = 1000
    tmpVec = Filter(function(f) f >= 0, originalData)
    vec = c()
    for (i in 1 : times) {
        vec = c(vec, min(sample(tmpVec, length(tmpVec) / 10)))
    }
    median(vec)
}

getFilterPeak <- function(peak, compoundRow){
    colnames(peak) = c("INT", "rt", "rtmin", "rtmax")
    peak$rt = data$SEC[peak$rt]
    peak$rtmin = data$SEC[peak$rtmin]
    peak$rtmax = data$SEC[peak$rtmax]
    filterPeak = subset(peak, rt >= compoundRow$rtLeft & rt <= compoundRow$rtRight)
    filterPeak
}

createWhenNoExist <- function(f){
    ! dir.exists(f) && dir.create(f)
}

getSub <- function(data, filterPeak, response){
    subData = subset(data, SEC >= filterPeak$rtmin & SEC <= filterPeak$rtmax)
    if (response == "height") {
        index <- order(abs(subData$SEC - filterPeak$rt))[1]
        subData <- subData[index,]
    }
    subData
}

getArea <- function(subData){
    area = 0
    if (nrow(subData) == 1) {
        area = subData$INT
    }else {
        for (i in 2 : nrow(subData)) {
            currentRow <- subData[i,]
            beforeRow <- subData[i - 1,]
            curArea = (currentRow$INT + beforeRow$INT) * (currentRow$SEC - beforeRow$SEC) / 2
            if (curArea < 0) {
                curArea = 0
            }
            area = curArea + area
        }
    }
    area
}

plotArea <- function(subData, response){
    if (response == "height") {
        lines(x = c(subData$SEC, subData$SEC), y = c(0, subData$INT), col = "green", lwd = 1.5)
    }else {
        n = length(subData$SEC)
        polygon(c(subData$SEC[1], subData$SEC, subData$SEC[n]), c(0, subData$INT, 0), col = "green")
    }
}

changeRtTime <- function(compoundRow, args){
    compoundRow$rtLeft <- (args$rt - args$rtlw)
    compoundRow$rtRight <- (args$rt + args$rtrw)
    compoundRow$rt <- args$rt
    compoundRow
}

myRound <- function(value) {
    int = floor(value)
    double = value - int
    if (double > 0.5) {
        int = int + 1
    }else if (double == 0.5) {
        int = int + 0.5
    }else {
        int = int
    }
    int
}

plotSlightCorrect <- function(data, compoundRow){
    plot(data$SEC , data$INT, col = "red", cex = 0.5, xlab = "RT(m)",
    main = paste("peak area | window size:", compoundRow$dfl, "    BLine: ", compoundRow$bline, sep = ""),
    ylab = "Intensity", xaxt = "n")
    at <- seq(from = myRound(min(data$SEC)), to = myRound(max(data$SEC)), by = 0.5)
    axis(side = 1, at = at)
    lines(data$SEC, data$INT, col = "grey")
}

getFirstPeak <- function(filterPeak, compoundRow){
    minDif = 0
    for (i in 1 : nrow(filterPeak)) {
        dif = filterPeak[i, "rt"] - compoundRow$rtLeft
        if (dif <= minDif) {
            firstRow = filterPeak[i,]
            minDif = dif
        }else if (minDif == 0) {
            minDif = dif
            firstRow = filterPeak[i,]
        }
    }
    firstRow
}
getLargestPeak <- function(filterPeak, data, compoundRow){
    maxArea = 0
    response = dealStr(compoundRow$response)
    for (i in 1 : nrow(filterPeak)) {
        subData = getSub(data, filterPeak[i,], response)
        area = getArea(subData)
        if (area >= maxArea || maxArea == 0) {
            maxArea = area
            firstRow = filterPeak[i,]
        }
    }
    firstRow
}
getNearestPeak <- function(filterPeak, compoundRow){
    minDif = 0
    for (i in 1 : nrow(filterPeak)) {
        dif = abs(filterPeak[i, "rt"] - compoundRow$rt)
        if (dif <= minDif || minDif == 0) {
            firstRow = filterPeak[i,]
            minDif = dif
        }
    }
    firstRow
}

plotGreenArea <- function(filterPeak, data, compoundRow){
    intensityMethod = tolower(compoundRow$peakMethod)
    intensityMethod <- as.character(intensityMethod)
    response = dealStr(compoundRow$response)
    if (intensityMethod == "all") {
        for (i in 1 : nrow(filterPeak)) {
            row <- filterPeak[i,]
            subData = getSub(data, row, response)
        }
        row <- filterPeak
    }else if (intensityMethod == "first") {
        row = getFirstPeak(filterPeak, compoundRow)
        subData = getSub(data, row, response)
    }else if (intensityMethod == "largest") {
        row = getLargestPeak(filterPeak, data, compoundRow)
        subData = getSub(data, row, response)
    }else if (intensityMethod == "nearest") {
        row <- getNearestPeak(filterPeak, compoundRow)
        subData = getSub(data, row, response)
    }

    abline(v = row$rt)
    abline(v = row$rtmin, col = "red")
    abline(v = row$rtmax, col = "red")

    plotSlightCorrect(data, compoundRow)
    if (intensityMethod == "all") {
        for (i in 1 : nrow(filterPeak)) {
            row <- filterPeak[i,]
            subData = getSub(data, row, response)
            plotArea(subData, response)
        }
    }else {
        plotArea(subData, response)
    }
}

library(pracma)
library(baseline)
source("base.R")
library(xlsx)
sampleConfig <- read.xlsx("sample_config.xlsx", 1, check.names = F)
sampleConfig <- setSampleConfigHeader(sampleConfig)
sampleConfig$fileName <- tolower(sampleConfig$fileName)
args = read.table(quote = "", "args.txt", header = T, com = '', sep = "\t", check.names = F)
compoundConfig <- read.xlsx("compound_config.xlsx", 1, check.names = F)
compoundConfig <- setCompoundConfigHeader(compoundConfig)
compoundConfig <- changeRtTime(compoundConfig, args)
uniqBatch = unique(sampleConfig$batch)
compoundName <- args$compound
compoundName <- as.character(compoundName)
dirName = "plot_peaks"
createWhenNoExist(dirName)
for (fl in seq(args$flMin, args$flMax, args$step)) {
    compoundRow <- compoundConfig[which(compoundConfig$compound == compoundName),]
    pdf(file = paste(dirName, "/", compoundName, "_ws_", fl, ".pdf", sep = ""), width = 15, height = 9)
    for (bat in uniqBatch) {
        config = subset(sampleConfig, batch == bat)
        fileNames = config$fileName
        for (fileName in fileNames) {
            data <- read.table(quote = "", paste("dta/", compoundName, "/", fileName, ".dta", sep = ""), header = T, com = '', sep = "\t", check.names = F)
            colnames(data) = c("SEC", "MZ", "INT")
            par(mfrow = c(3, 1))
            originalData = data$INT
            slightSmoothData <- savgol(data$INT, compoundRow$dfl)

            smoothData <- data$INT
            for (i in 1 : args$iteration) {
                smoothData <- savgol(smoothData, fl)
            }

            if (args$bline == "no") {
                data$INT <- slightSmoothData
                slightCorrectValue <- slightSmoothData
                correctValue <- smoothData
            }else {
                baseLineFrame <- data.frame(Date = data$SEC, Visits = slightSmoothData)
                baseLineFrame <- t(baseLineFrame$Visits)
                slightBaseLine <- baseline(baseLineFrame, method = 'irls')
                slightCorrectValue = c(getCorrected(slightBaseLine))
                data$INT = slightCorrectValue

                baseLineFrame <- data.frame(Date = data$SEC, Visits = smoothData)
                baseLineFrame <- t(baseLineFrame$Visits)
                baseLine <- baseline(baseLineFrame, method = 'irls')
                correctValue = c(getCorrected(baseLine))
            }

            median = getMedian(slightCorrectValue)
            std <- as.character(compoundRow$std)
            std <- tolower(std)
            mic <- sampleConfig[which(sampleConfig$fileName == fileName), std]

            plot(data$SEC , originalData, col = "red", cex = 0.5, main = paste("raw chromatogram | batch: ", bat, "    sample: ",
            fileName, "    conc: ", mic, "    function: ", compoundRow$fc, "    mass: ", compoundRow$mz, sep = ""),
            xlab = "RT(m)", ylab = "Intensity", xaxt = "n")
            at <- seq(from = myRound(min(data$SEC)), to = myRound(max(data$SEC)), by = 0.5)
            axis(side = 1, at = at)
            lines(data$SEC, originalData, col = "grey")

            noiseStr <- signif(median, 3)
            compoundRow$peakMethod <- args$peakLocation

            plot2 = plot(data$SEC , correctValue, col = "red", cex = 0.5, xlab = "RT(m)",
            main = paste("peak picking | window size: ", fl, "   iteration: ", args$iteration, "    lp: ", args$nups,
            "    rp: ", args$ndowns,
            "    snr: ", args$snr, "    peak location: ",
            compoundRow$peakMethod, "    noise: ", noiseStr, "    BLine: ", args$bline, "    RT: ", args$rt,
            "    RTLW: ", args$rtlw, "    RTRW: ", args$rtlw, sep = ""), ylab = "Intensity", xaxt = "n")
            at <- seq(from = myRound(min(data$SEC)), to = myRound(max(data$SEC)), by = 0.5)
            axis(side = 1, at = at)

            lines(data$SEC, correctValue, col = "grey")

            plot2 + abline(h = median, col = "blue")
            plot2 + abline(h = median * args$snr, col = "blue")

            peak = findpeaks(correctValue, threshold = median * args$snr, nups = args$nups, ndowns = args$ndowns)
            peak = as.data.frame(peak)
            valid <- nrow(peak) != 0
            filterPeak <- data.frame()
            if (valid) {
                filterPeak <- getFilterPeak(peak, compoundRow)
            }
            abline(v = c(compoundRow$rt, compoundRow$rtLeft, compoundRow$rtRight), col = "blue", lty = 3)
            valid <- nrow(filterPeak) != 0
            if (valid) {
                plotGreenArea(filterPeak, data, compoundRow)
            }else {
                plotSlightCorrect(data, compoundRow)
            }
        }
    }
    dev.off()
}




