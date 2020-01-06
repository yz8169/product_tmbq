# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/29
setCompoundConfigHeader <- function(compoundConfig){
    colnames(compoundConfig) <- tolower(colnames(compoundConfig))
    setnames(compoundConfig, "mass", "mz")
    setnames(compoundConfig, "rtlw", "rtLeft")
    setnames(compoundConfig, "rtrw", "rtRight")
    setnames(compoundConfig, "peak_location", "peakMethod")
    setnames(compoundConfig, "polynomial_type", "regressMethod")
    setnames(compoundConfig, "ws4pp", "fl")
    setnames(compoundConfig, "rs4rs", "rSquare")
    setnames(compoundConfig, "mp4rs", "minPoint")
    setnames(compoundConfig, "is_correction", "is")
    setnames(compoundConfig, "nups4pp", "nups")
    setnames(compoundConfig, "ndowns4pp", "ndowns")
    setnames(compoundConfig, "ws4pa", "dfl")
    setnames(compoundConfig, "rp4e", "rp")
    setnames(compoundConfig, "lp4e", "lp")
    setnames(compoundConfig, "mp4e", "mpfs")
    setnames(compoundConfig, "i4pp", "iteration")
    setnames(compoundConfig, "snr4pp", "snr")
    setnames(compoundConfig, "function", "fc")
    setnames(compoundConfig, "rmode", "rmode")
    setnames(compoundConfig, "rmis", "rmis")
    setnames(compoundConfig, "rmratio", "rmrate")
    compoundConfig
}

setSampleConfigHeader <- function(sampleConfig){
    colnames(sampleConfig) <- tolower(colnames(sampleConfig))
    setnames(sampleConfig, "file name", "fileName")
    setnames(sampleConfig, "sample type", "sampleType")
    sampleConfig
}

dealStr <- function(element){
    str <- as.character(element)
    str <- tolower(element)
    str
}

myStartsWith<-function(str,prefix){
    substring(str,1,nchar(prefix))==prefix
}

library(data.table)

