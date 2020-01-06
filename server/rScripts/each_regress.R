# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/26

#修改日志：
#1.最终标样小于最小点个数 则最小点个数重置为最终版标样数
#2.标样强度为0 则剔除这个样进行拟合 最后这个样的浓度为0
#3.新增标样点数过少，拟合不成功或R方为0的处理
#4.新增R方为NA的处理,新增最大运行次数
createWhenNoExist <- function(f) {
  !dir.exists(f) && dir.create(f)
}

getFinalLmData <- function(lmData, compoundRow) {
  rowNum <- nrow(lmData)
  minPointNum <- compoundRow$minPoint
  finalLmData <- lmData
  stop <- FALSE
  minRSquare <- compoundRow$rSquare
  maxRsquare <- 0
  lm <- NULL

  if (is.null(compoundRow$mrt)) {
    maxTimes <- 1000000
  }else {
    maxTimes <- compoundRow$mrt
  }

  times <- 0

  compound <- compoundRow$compound
  compound <- as.character(compound)
  if (nrow(lmData) < minPointNum) {
    minPointNum = nrow(lmData)
  }


  for (j in rowNum:minPointNum) {
    rowFrame <- t(combn(rownames(lmData), j))
    for (i in 1:nrow(rowFrame)) {
      times = times + 1
      rowNumVec <- rowFrame[i,]
      perLmData <- lmData[rowNumVec,]
      if (tolower(compoundRow$regressMethod) == "linear") {
        if (tolower(compoundRow$origin) == "exclude") {
          tmpLm <- lm(y ~ x + 1, data = perLmData)
        }else {
          tmpLm <- lm(y ~ x - 1, data = perLmData)
        }
      }else {
        if (tolower(compoundRow$origin) == "exclude") {
          tmpLm <- lm(y ~ poly(x, 2), data = perLmData)
        }else {
          tmpLm <- lm(y ~ -1 + x + I(x^2), data = perLmData)
        }
      }
      tmpRSquare <- summary(tmpLm)$r.squared
      if (is.na(tmpRSquare)) {
        tmpRSquare <- 0
      }
      if (tmpRSquare >= minRSquare || tmpRSquare >= maxRsquare) {
        finalLmData = perLmData
        lm = tmpLm
        maxRsquare = tmpRSquare
      }
      if (tmpRSquare >= minRSquare) {
        stop = TRUE
        break
      }
      if (times >= maxTimes) {
        stop = TRUE
        break
      }
    }
    if (stop)break
  }
  list(lm = lm, data = finalLmData, rsquare = maxRsquare)
}

getIsData <- function(data, compoundRow) {
  is <- compoundRow$is
  is <- as.character(is)
  compoundName <- compoundRow$compound
  compoundName <- as.character(compoundName)
  if (tolower(is) != "none") {
    isCompound <- compoundConfig[which(compoundConfig$index == is),]$compound
    isCompound <- as.character(isCompound)
    data[, compoundName] <- data[, compoundName] / data[, isCompound]
  }
  data
}

getPlotLmData <- function(predictData, lmData, compoundRow) {
  predictX <- predictData$x
  minPredictX <- min(predictX)
  # minPredictX <- 611805
  maxPredictX <- max(predictX)
  lineNums = which(lmData$x >= minPredictX & lmData$x <= maxPredictX)
  if (length(lineNums) == 0) {
    minDif = 0
    for (i in 1:nrow(lmData)) {
      dif = abs(lmData[i, "x"] - minPredictX)
      if (dif <= minDif || minDif == 0) {
        trueI = i
        minDif = dif
      }
    }
    lineNums <- c(trueI)
  }
  plotLmData <- lmData[lineNums,]

  rp <- compoundRow$rp
  lp <- compoundRow$lp
  if (compoundRow$mpfs > nrow(lmData)) {
    compoundRow$mpfs = nrow(lmData)
  }
  while (nrow(plotLmData) < compoundRow$mpfs) {
    leftIndex <- min(lineNums) - lp
    if (leftIndex < 1) {
      leftIndex = 1
      if (rp == 0) {
        rp = 1
      }
    }
    rightIndex <- max(lineNums) + rp
    if (rightIndex > nrow(lmData)) {
      rightIndex = nrow(lmData)
      if (lp == 0) {
        lp = 1
      }
    }
    lineNums <- leftIndex:rightIndex
    plotLmData <- lmData[lineNums,]
  }

  plotLmData
}

getFormula <- function(list, compoundRow) {
  b <- coef(list$lm)["(Intercept)"]
  a <- coef(list$lm)["x"]
  if (tolower(compoundRow$regressMethod) == "linear") {
    a <- signif(a, 3)
    formula <- paste("y=", a, "x", sep = "")
    b <- as.numeric(b)
  }else {
    if (tolower(compoundRow$origin) == "exclude") {
      a2 <- coef(list$lm)["poly(x, 2)2"]
      a <- coef(list$lm)["poly(x, 2)1"]
    }else {
      a2 <- coef(list$lm)["I(x^2)"]
    }
    plusStr <- ""
    if (as.numeric(a) >= 0) {
      plusStr <- "+"
    }
    a2 <- signif(a2, 3)
    a <- signif(a, 3)
    formula <- paste("y=", a2, "x^2", plusStr, a, "x", sep = "")
  }
  if (!is.na(b)) {
    addStr <- ""
    if (as.numeric(b) >= 0) {
      addStr <- "+"
    }
    b <- signif(b, 3)
    formula <- paste(formula, addStr, b, sep = "")
  }
  formula
}

library(xlsx)
library(optparse)
library(tidyverse)

baseDir <- "../"

source(paste0(baseDir, "base.R"))
option_list <- list(
  make_option("--ci", default = "compoundName.xlsx", type = "character", help = "compound config input file"),
  make_option("--coi", default = "color.txt", type = "character", help = "color  input file"),
  make_option("--ro", default = "regress.txt", type = "character", help = "regress result output file")
)
opt <- parse_args(OptionParser(option_list = option_list))

opar <- par(no.readonly = TRUE)
par(cex = 3)


compoundConfig <- read.xlsx("../compound_config.xlsx", 1, check.names = F, stringsAsFactors = F)
compoundConfig <- setCompoundConfigHeader(compoundConfig)
data <- read.table(quote = "", paste0(baseDir, "intensity.txt"), header = T, com = '', sep = "\t", check.names = F)
sampleConfig <- read.xlsx("../sample_config.xlsx", 1, check.names = F)
sampleConfig <- setSampleConfigHeader(sampleConfig)

mic <- data.frame(sample = data$sample, batch = data$batch)

compoundNameData <- read.xlsx(opt$ci, 1, check.names = F)
usefulCompoundConfig <- subset(compoundConfig, compound %in% compoundNameData$CompoundName)
print(usefulCompoundConfig)

sampleConfig <- subset(sampleConfig, !is.na(fileName))
uniqBatch = unique(sampleConfig$batch)
formulaBat <- as.vector(sapply(uniqBatch, function(x) rep(x, 2)))
formulaData <- data.frame(sample = rep(c("r-square", "formula"), length(uniqBatch)), batch = formulaBat)

# for (compoundName in usefulCompoundConfig$compound) {
for (compoundName in usefulCompoundConfig$compound) {
  print(compoundName)
  compoundName <- as.character(compoundName)
  compoundRow <- compoundConfig[which(compoundConfig$compound == compoundName),]
  print(compoundRow)
  data <- getIsData(data, compoundRow)
  orignalData <- data.frame(sample = data$sample, batch = data$batch)
  orignalData[, "x"] <- data[, compoundName]
  orignalData <- subset(orignalData, !is.infinite(x) & !is.nan(x))
  std <- as.character(compoundRow$std)
  std <- tolower(std)
  index <- dealStr(compoundRow$index)
  sampleConfig$fileName = tolower(sampleConfig$fileName)
  mic[, compoundName] = rep(0, nrow(mic))
  formulaData[, compoundName] = rep("NA", nrow(formulaData))
  if (nrow(orignalData) == 0) {
    plot(1, type = "n", xlab = "", ylab = "", main = "error:data is not enough after filtering 0 value !")
    next
  }
  if (myStartsWith(index, "is")) {
    y <- compoundRow$std
    y <- as.numeric(y)
    y <- rep(y, nrow(orignalData))
    orignalData$y <- y

    compoundRow$origin = "include"
    compoundRow$rSquare <- 0
    compoundRow$regressMethod <- "linear"
  }else {
    for (i in 1:nrow(orignalData)) {
      row <- orignalData[i,]
      orignalData[i, "y"] <- sampleConfig[which(sampleConfig$fileName == as.character(row$sample)), std]
    }
  }
  for (i in 1:nrow(orignalData)) {
    row <- orignalData[i,]
    orignalData[i, "sampleType"] <- sampleConfig[which(sampleConfig$fileName == as.character(row$sample)), "sampleType"]
  }
  dirName <- paste0(baseDir, "plot_regress")
  createWhenNoExist(dirName)
  pdf(file = paste(dirName, "/", compoundName, ".pdf", sep = ""), width = 15, height = 9)
  for (bat in uniqBatch) {
    orignalData <- orignalData[order(orignalData$x),]
    lmData <- subset(orignalData, batch == bat & tolower(sampleType) == "standard")
    if (nrow(lmData) == 0) {
      plot(1, type = "n", xlab = "", ylab = "", main = "error:standard data is not enough after filtering 0 value !")
      next
    }
    predictData <- subset(orignalData, batch == bat & tolower(sampleType) == "analyte")
    lmData <- lmData[order(lmData$x),]
    if (myStartsWith(index, "is")) {
      plotLmData <- orignalData
    }else {
      plotLmData <- lmData
    }

    plot1 = plot(plotLmData$x, plotLmData$y, col = "black", cex = 1.5, xlab = "Intensity", ylab = "Concentration",
                 main = paste("compound: ", compoundName, "    batch: ", bat, "    raw_points: ", length(plotLmData$x),
                              "    conc: ", std, sep = ""), bty = "l")
    plot1 + lines(plotLmData$x, plotLmData$y, col = "black", lwd = 1)
    if (!myStartsWith(index, "is")) {
      plotLmData <- getPlotLmData(predictData, lmData, compoundRow)
      plot1 + abline(v = c(min(plotLmData$x), max(plotLmData$x)), col = "black", lty = 3)
    }
    # uniquexy <- unique(plotLmData[, c("x", "y")])
    # if (nrow(uniquexy) < 2) {
    #     next
    # }
    tryCatch(
      { list <- getFinalLmData(plotLmData, compoundRow)
      if (list$rsquare <= 0 || is.nan(list$rsquare)) {
        plot(1, type = "n", xlab = "", ylab = "", main = "error:rsquare is 0 or not a number!")
        next
      }
    },
      error = function(e) {
        plot(1, type = "n", xlab = "", ylab = "", main = "error:regress error!")
        next
      }
    )

    predictVec <- predict(list$lm, data.frame(x = list$data$x))
    allPredictData <- rbind(predictData, list$data)

    allPredictData <- allPredictData[order(allPredictData$x),]
    allPredictVec <- predict(list$lm, data.frame(x = allPredictData$x))
    yMax <- max(c(allPredictVec, list$data$y))
    yMin <- min(c(allPredictVec, list$data$y))
    xMin <- min(c(allPredictData$x, list$data$x))
    xMax <- max(c(allPredictData$x, list$data$x))
    randomX <- seq(xMin, xMax, length.out = 10000)
    randomY <- predict(list$lm, data.frame(x = randomX))
    plot2 = plot(randomX, randomY, col = "black", cex = 1.5, width = 5, height = 15,
                 xlab = "Intensity", ylab = "Concentration", main = "",
                 type = "l", ylim = c(yMin, yMax), xlim = c(xMin, xMax), , bty = "l"
    )

    plot2 + points(list$data$x, list$data$y, col = "black", cex = 1.5)

    for (i in 1:length(list$data$x)) {
      lines(c(list$data$x[i], list$data$x[i]), c(list$data$y[i], predictVec[i]), col = "black", lty = 3)
    }
    point = data.frame(x = predictData$x)
    predict <- predict(list$lm, point, interval = "prediction", level = 0.95)
    predict <- as.data.frame(predict)
    predictData$y <- predict$fit


    if (!myStartsWith(index, "is")) {
      plot2 + points(predictData$x, predictData$y, col = "black", cex = 0.6, pch = 4)
      plot2 + abline(v = c(min(predictData$x), max(predictData$x)), col = "black")
    }
    formula <- getFormula(list, compoundRow)

    formulaData[which(formulaData$batch == bat & formulaData$sample == "r-square"), compoundName] = list$rsquare
    formulaData[which(formulaData$batch == bat & formulaData$sample == "formula"), compoundName] = formula
    rsquareStr <- signif(list$rsquare, 3)
    is <- as.character(compoundRow$is)
    if (!tolower(is) == "none") {
      is <- compoundConfig[which(compoundConfig$index == is),]$compound
    }
    plot2 + title(main =
                    paste("compound: ", compoundName, "    batch: ", bat,
                          "    used_points: ", length(plotLmData$x),
                          "    fit_points: ", length(list$data$x), "    conc: ", std, "\n    R-square: ", rsquareStr,
                          "    ", formula, sep = ""),)

    point = data.frame(x = lmData$x)
    predict <- predict(list$lm, point, interval = "prediction", level = 0.95)
    predict <- as.data.frame(predict)
    lmData$y <- predict$fit

    out <- rbind(predictData, lmData)
    f <- function(x) if (x < 0)0 else x
    for (i in 1:nrow(out)) {
      row <- out[i,]
      sample <- row$sample
      sample <- as.character(sample)
      mic[which(mic$batch == bat & mic$sample == sample), compoundName] = sapply(row$y, f)
    }
  }
  dev.off()
}
getColorStr <- function(int, list) {
  if (int < list$lod) {
    "red"
  }else if (int < list$loq) {
    "yellow"
  }else {
    "green"
  }
}
color <- read.table(quote = "", opt$coi, header = T, com = '', sep = "\t", check.names = F)
for (compoundName in usefulCompoundConfig$compound) {
  compoundName <- as.character(compoundName)
  compoundRow <- compoundConfig[which(compoundConfig$compound == compoundName),]
  mics <- mic[, compoundName]
  colors <- color[, compoundName]
  list <- list(loq = compoundRow$loq, lod = compoundRow$lod)
  for (i in 1:length(mics)) {
    everyMic <- mics[i]
    tmpMic <- mic[i,]
    sample <- tmpMic$sample
    sample <- as.character(sample)
    sample <- tolower(sample)
    sampleRow <- subset(sampleConfig, tolower(fileName) == sample)
    kind <- as.character(sampleRow$sampleType)
    kind <- tolower(sampleRow$sampleType)
    if (is.na(colors[i])) {
      if (kind == "standard") {
        colors[i] <- "NA"
      }else {
        colors[i] <- getColorStr(everyMic, list)
      }
    }
  }
  color[, compoundName] <- colors
}

compoundNameData <- read.xlsx(opt$ci, 1, check.names = F)
compoundInfoData <- subset(compoundConfig, compound %in% compoundNameData$CompoundName)
compoundInfoData <- t(compoundInfoData)
colnames(compoundInfoData) <- compoundInfoData["compound",]
infoData <- data.frame(sample = rownames(compoundInfoData))
infoData$batch <- rep("", nrow(infoData))
infoData <- cbind(infoData, compoundInfoData)
infoData <- infoData[-c(2),]
for (i in 3:ncol(infoData)) {
  infoData[, i] <- as.character(infoData[, i])
}
infoData <- rbind(infoData, formulaData)

calculateData <- data.frame(sample = c("RSD(%)", "mean", "SD"), batch = rep("", 3))
for (compoundName in usefulCompoundConfig$compound) {
  sd <- sd(mic[, compoundName])
  mean <- mean(mic[, compoundName])
  rsd = sd * 100 / mean
  calculateData[which(calculateData$sample == "RSD(%)"), compoundName] <- rsd
  calculateData[which(calculateData$sample == "mean"), compoundName] <- mean
  calculateData[which(calculateData$sample == "SD"), compoundName] <- sd
}
infoData <- rbind(infoData, calculateData)

regressData <- rbind(infoData, mic)

colorData <- rbind(infoData, color)

write.table(colorData, opt$coi, quote = FALSE, sep = "\t", row.names = F)

regressData <- regressData %>%
  filter(!sample %in% c("index", "mz", "rtLeft", "rtRight", "peakMethod", "response", "is", "regressMethod", "origin",
                        "fl", "iteration", "snr", "lod", "loq", "nups", "ndowns", "dfl", "lp", "rp", "mpfs", "bline", "rmode", "rmis",
                        "rmrate", "rSquare","fc"))

regressData

write.table(regressData, opt$ro, quote = FALSE, sep = "\t", row.names = F)

par(opar)



