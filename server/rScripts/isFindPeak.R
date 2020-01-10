# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/19

library(tidyverse)

getSub <- function(data, filterPeak, response) {
  subData = subset(data, SEC >= filterPeak$rtmin & SEC <= filterPeak$rtmax)
  if (response == "height") {
    index <- order(abs(subData$SEC - filterPeak$rt))[1]
    subData <- subData[index,]
  }
  subData
}
getFirstPeak <- function(filterPeak, compoundRow) {
  minDif = 0
  for (i in 1:nrow(filterPeak)) {
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
getLargestPeak <- function(filterPeak, data, compoundRow) {
  maxArea = 0
  response = dealStr(compoundRow$response)
  for (i in 1:nrow(filterPeak)) {
    subData = getSub(data, filterPeak[i,], response)
    area = getArea(subData)
    if (area >= maxArea || maxArea == 0) {
      maxArea = area
      firstRow = filterPeak[i,]
    }
  }
  firstRow
}
getNearestPeak <- function(filterPeak, compoundRow) {
  nearestPeak <- as_tibble(filterPeak) %>%
    mutate(min = abs(rt - compoundRow$rt)) %>%
    arrange(min) %>%
    head(1) %>%
    select(-"min") %>%
    as.data.frame()
  nearestPeak
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

plotSlightCorrect <- function(data, compoundRow) {
  plot(data$SEC, data$INT, col = "red", cex = 0.5, xlab = "RT(m)",
       main = paste("peak area", sep = ""),
       ylab = "Intensity", xaxt = "n")
  at <- seq(from = myRound(min(data$SEC)), to = myRound(max(data$SEC)), by = 0.5)
  axis(side = 1, at = at)
  lines(data$SEC, data$INT, col = "grey")
}

plotArea <- function(subData, response) {
  if (response == "height") {
    lines(x = c(subData$SEC, subData$SEC), y = c(0, subData$INT), col = "green", lwd = 1.5)
  }else {
    n = length(subData$SEC)
    polygon(c(subData$SEC[1], subData$SEC, subData$SEC[n]), c(0, subData$INT, 0), col = "green")
  }
}

getArea <- function(subData) {
  area = 0
  if (nrow(subData) == 1) {
    area = subData$INT
  }else {
    for (i in 2:nrow(subData)) {
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

plotGreenArea <- function(filterPeak, data, compoundRow) {
  totalInt = 0
  intensityMethod = tolower(compoundRow$peakMethod)
  intensityMethod <- as.character(intensityMethod)
  response = dealStr(compoundRow$response)
  if (intensityMethod == "all") {
    for (i in 1:nrow(filterPeak)) {
      row <- filterPeak[i,]
      subData = getSub(data, row, response)
      totalInt = getArea(subData) + totalInt
    }
    row <- filterPeak
  }else if (intensityMethod == "first") {
    row = getFirstPeak(filterPeak, compoundRow)
    subData = getSub(data, row, response)
    totalInt = getArea(subData) + totalInt
  }else if (intensityMethod == "largest") {
    row = getLargestPeak(filterPeak, data, compoundRow)
    subData = getSub(data, row, response)
    totalInt = getArea(subData) + totalInt
  }else if (intensityMethod == "nearest") {
    row <- getNearestPeak(filterPeak, compoundRow)
    subData = getSub(data, row, response)
    totalInt = getArea(subData) + totalInt
  }

  abline(v = row$rt)
  abline(v = row$rtmin, col = "red")
  abline(v = row$rtmax, col = "red")

  plotSlightCorrect(data, compoundRow)
  if (intensityMethod == "all") {
    for (i in 1:nrow(filterPeak)) {
      row <- filterPeak[i,]
      subData = getSub(data, row, response)
      plotArea(subData, response)
    }
  }else {
    plotArea(subData, response)
  }

  if (totalInt < 0) {
    totalInt = 0
  }

  list(totalInt = totalInt, firstRow = row)
}

findIsRt <- function(filterPeak, data, compoundRow) {
  intensityMethod = tolower(compoundRow$peakMethod)
  intensityMethod <- as.character(intensityMethod)
  if (intensityMethod == "first") {
    rtRow = getFirstPeak(filterPeak, compoundRow)
  }else if (intensityMethod == "largest" || intensityMethod == "all") {
    rtRow = getLargestPeak(filterPeak, data, compoundRow)
  }else if (intensityMethod == "nearest") {
    rtRow <- getNearestPeak(filterPeak, compoundRow)
  }
  rtRow$rt
}

getMedian <- function(originalData, times) {
  tmpVec = Filter(function(f) f >= 0, originalData)
  vec = c()
  for (i in 1:times) {
    vec = c(vec, min(sample(tmpVec, length(tmpVec) / 10)))
  }
  median(vec)
}

getFilterPeak <- function(peak, compoundRow) {
  colnames(peak) = c("INT", "rt", "rtmin", "rtmax")
  peak$rt = data$SEC[peak$rt]
  peak$rtmin = data$SEC[peak$rtmin]
  peak$rtmax = data$SEC[peak$rtmax]
  filterPeak = subset(peak, rt >= compoundRow$rtLeft & rt <= compoundRow$rtRight)
  filterPeak
}

createWhenNoExist <- function(f) {
  !dir.exists(f) && dir.create(f)
}

getColorByRow <- function(firstRow, compoundRow) {
  if (abs(compoundRow$rt - firstRow$rt) > 0.2) {
    "red"
  }else {
    "NA"
  }
}

getColor <- function(compoundRow, list) {
  colorStr = "NA"
  intensityMethod = tolower(compoundRow$peakMethod)
  if (intensityMethod != "all") {
    colorStr <- getColorByRow(list$firstRow, compoundRow)
  }

  colorStr
}

changeRtTime <- function(compoundRow) {
  compoundRow$rtLeft <- (compoundRow$rt - compoundRow$rtLeft)
  compoundRow$rtRight <- (compoundRow$rt + compoundRow$rtRight)
  compoundRow$rt <- compoundRow$rt
  compoundRow
}
library(optparse)
library(pracma)
library(baseline)
library(xlsx)
library(tidyverse)

baseDir <- "../"

source(paste0(baseDir, "base.R"))
option_list <- list(
  make_option("--ci", default = "compoundName.xlsx", type = "character", help = "compound name file"),
  make_option("--si", default = "../sample_config.xlsx", type = "character", help = "sample config input file"),
  make_option("--co", default = "color.txt", type = "character", help = "color output file"),
  make_option("--io", default = "intensity.txt", type = "character", help = "intensity output file"),
  make_option("--ro", default = "is_rt.txt", type = "character", help = "is rt output file")
)

opt <- parse_args(OptionParser(option_list = option_list))
sampleConfig <- read.xlsx(opt$si, 1, check.names = F)
sampleConfig <- setSampleConfigHeader(sampleConfig)
sampleConfig$fileName <- tolower(sampleConfig$fileName)
compoundConfig <- read.xlsx("../compound_config.xlsx", 1, check.names = F)
str(compoundConfig)
compoundConfig <- setCompoundConfigHeader(compoundConfig)
str(compoundConfig)
compoundConfig <- changeRtTime(compoundConfig)
compoundConfig
intensity = data.frame(sample = sampleConfig$fileName)
color = data.frame(sample = sampleConfig$fileName)
uniqBatch = unique(sampleConfig$batch)
compoundNameData <- read.xlsx(opt$ci, 1, check.names = F)
isRtData <- data.frame(sample = sampleConfig$fileName)

# for (compoundName in compoundConfig$compound) {
for (compoundName in compoundNameData$CompoundName) {
  print(compoundName)
  dirName = paste0(baseDir, "plot_peaks")
  createWhenNoExist(dirName)
  compoundRow <- compoundConfig[which(compoundConfig$compound == compoundName),]

  print(compoundRow)

  pdf(file = paste(dirName, "/", compoundName, ".pdf", sep = ""), width = 15, height = 9)
  for (bat in uniqBatch) {
    config = subset(sampleConfig, batch == bat)
    fileNames = config$fileName
    for (fileName in fileNames) {
      print(fileName)
      data <- read.table(quote = "", paste(baseDir, "dta/", compoundName, "/", fileName, ".dta", sep = ""), header = T, com = '', sep = "\t", check.names = F)
      # print(fileName)
      # print(paste("dta/", compoundName, "/", fileName, ".dta", sep = ""))
      colnames(data) = c("SEC", "MZ", "INT")
      par(mfrow = c(3, 1))
      originalData = data$INT

      slightSmoothData <- savgol(data$INT, compoundRow$dfl)

      smoothData <- data$INT
      for (i in 1:compoundRow$iteration) {
        smoothData <- savgol(smoothData, compoundRow$fl)
      }
      if (compoundRow$bline == "no") {
        slightCorrectValue <- slightSmoothData
        correctValue <- smoothData
      }else {
        baseLineFrame <- data.frame(Date = data$SEC, Visits = slightSmoothData)
        baseLineFrame <- t(baseLineFrame$Visits)
        slightBaseLine <- baseline(baseLineFrame, method = 'irls')
        slightCorrectValue = c(getCorrected(slightBaseLine))

        baseLineFrame <- data.frame(Date = data$SEC, Visits = smoothData)
        baseLineFrame <- t(baseLineFrame$Visits)
        baseLine <- baseline(baseLineFrame, method = 'irls')
        correctValue = c(getCorrected(baseLine))
      }

      if (compoundRow$bline4pa == "no") {
        data$INT <- slightSmoothData
      }else {
        baseLineFrame <- data.frame(Date = data$SEC, Visits = slightSmoothData)
        baseLineFrame <- t(baseLineFrame$Visits)
        slightBaseLine <- baseline(baseLineFrame, method = 'irls')
        slightCorrectValue = c(getCorrected(slightBaseLine))
        data$INT = slightCorrectValue
      }

      median = getMedian(correctValue, compoundRow$npt)
      std <- as.character(compoundRow$std)
      std <- tolower(std)
      index <- dealStr(compoundRow$index)

      if (myStartsWith(index, "is")) {
        mic <- compoundRow$std
      }else {
        mic <- sampleConfig[which(sampleConfig$fileName == fileName), std]
      }
      plot(data$SEC, originalData, col = "red", cex = 0.5, main = paste("raw chromatogram | batch: ", bat, "    sample: ",
                                                                        fileName, "    conc: ", mic, sep = ""),
           xlab = "RT(m)", ylab = "Intensity", xaxt = "n")
      at <- seq(from = myRound(min(data$SEC)), to = myRound(max(data$SEC)), by = 0.5)
      axis(side = 1, at = at)
      lines(data$SEC, originalData, col = "grey")

      noiseStr <- signif(median, 3)

      plot2 = plot(data$SEC, correctValue, col = "red", cex = 0.5, xlab = "RT(m)",
                   main = paste("peak picking", sep = ""), ylab = "Intensity",
                   xaxt = "n")
      at <- seq(from = myRound(min(data$SEC)), to = myRound(max(data$SEC)), by = 0.5)
      axis(side = 1, at = at)
      lines(data$SEC, correctValue, col = "grey")
      plot2 + abline(h = median, col = "blue")
      plot2 + abline(h = median * compoundRow$snr, col = "blue")
      peak = findpeaks(correctValue, threshold = median * compoundRow$snr, nups = compoundRow$nups, ndowns = compoundRow$ndowns)
      peak = as.data.frame(peak)
      valid <- nrow(peak) != 0
      filterPeak <- data.frame()
      if (valid) {
        filterPeak <- getFilterPeak(peak, compoundRow)
      }
      abline(v = c(compoundRow$rt, compoundRow$rtLeft, compoundRow$rtRight), col = "blue", lty = 3)
      valid <- nrow(filterPeak) != 0
      compoundIndex <- as.character(compoundRow$index)
      if (!valid) {
        intensity[which(intensity$sample == fileName), "batch"] = bat
        intensity[which(intensity$sample == fileName), compoundName] = 0

        color[which(color$sample == fileName), "batch"] = bat
        color[which(color$sample == fileName), compoundName] = "NA"
        isRtData[which(isRtData$sample == fileName), compoundIndex] = "NA"

        plotSlightCorrect(data, compoundRow)
        next
      }
      list <- plotGreenArea(filterPeak, data, compoundRow)
      totalInt <- list$totalInt
      colorStr <- getColor(compoundRow, list)

      isRt <- findIsRt(filterPeak, data, compoundRow)
      isRtData[which(isRtData$sample == fileName), compoundIndex] = isRt


      intensity[which(intensity$sample == fileName), "batch"] = bat

      intensity[which(intensity$sample == fileName), compoundName] = totalInt
      color[which(color$sample == fileName), "batch"] = bat
      color[which(color$sample == fileName), compoundName] = colorStr
    }
  }
  dev.off()
}
write.table(isRtData, opt$ro, quote = FALSE, sep = "\t", row.names = F)
write.table(intensity, opt$io, quote = FALSE, sep = "\t", row.names = F)
write.table(color, opt$co, quote = FALSE, sep = "\t", row.names = F)




