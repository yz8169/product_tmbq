# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/4
library(optparse)
library(pracma)
library(baseline)
library(xcms)
option_list <- list(
make_option("--i", type = 'character', action = "store", default = "", help = "input file")
)
opt <- parse_args(OptionParser(option_list = option_list))
data = read.table(quote="","dta/TwMCA/COTH10.dta", header = T, com = '', sep = "\t", check.names = F)
# data=xcmsSet()
# findPeaks()
# peaks=findpeaks(correctValue,sortstr=TRUE,minpeakheight=50)
# print(peaks)
# points(data$RT[peaks[,2]],peaks[,1],pch=20,col="maroon")
# cdfpath <- "D:/workspaceForIDEA/tmbq_26/pyScripts/mzxml"
# cdffiles <- list.files(cdfpath, recursive = TRUE, full.names = TRUE)
# set=xcmsSet(cdffiles)
# set@peaks
# raw_data=xcmsRaw(cdffiles,msLevel=1)

# processingData(sp2)
# bpis <- chromatogram(raw_data, aggregationFun = "max")
# pdf(file = 'tmp.pdf', width = 15, height = 9)
# par(mfrow=c(3,1))
# originalData=data$INT
# plot(data$SEC , originalData, col = "red",cex=0.5)
# lines(data$SEC, originalData, col = "grey")
# smoothData=savgol(data$INT,7)
# plot(data$SEC , smoothData, col = "red",cex=0.5)
# lines(data$SEC, smoothData, col = "grey")

# correctValue=c(getCorrected(bc.irls))
# plot(data$SEC , correctValue, col = "red",cex=0.5)
# lines(data$SEC, correctValue, col = "grey")

# data$INT=correctValue

# raw=new("xcmsRaw")
# numPoints=length(data$INT)
# raw@env$mz=data$MZ
# raw@scantime=data$SEC
# raw@scanindex=0:(numPoints-1)
# raw@env$intensity=data$INT
# raw@tic=data$INT
# for (sigma in seq(2,5,0.02) ){
#     plot(data$SEC , correctValue, col = "red",cex=0.5,xlab=as.character(sigma))
#     lines(data$SEC, correctValue, col = "grey")
#     peak=findPeaks.matchedFilter(raw,sigma=sigma)
#     peak=as.data.frame(peak)
#     peak
#     peakData=subset(data,SEC  %in% peak$rt)
#     peakData
#     rtmin=peak$rtmin
#     rtmax=peak$rtmax
#     maxData=subset(data,SEC>=rtmin[1] & SEC<=rtmax[1])
#     sum(maxData$INT)
#     maxData=subset(data,SEC>=rtmin[2] & SEC<=rtmax[2])
#     sum(maxData$INT)
#     abline(v=peakData$SEC)
#     abline(v=peak$rtmin,col="red")
#     abline(v=peak$rtmax,col="red")
# }



# plot(bpis, col = "red")
# print(names(attributes(sp2@processingData)))
# print(sp2@rt)
# cwp=MatchedFilterParam(binSize = 0.1, impute = "none", fwhm = 30, max = 5,
# snthresh = 10, steps = 2,
# index = FALSE)
# xdata <- findChromPeaks(raw_data, param = cwp)
# highlightChromPeaks(xdata,lty = 3,type="point")
# head(bpis)
# peaks=findPeaks(xraw,method="matchedFilter")
# print(xraw)
# print(peaks@peaks)
# xfrag=xcmsFragments(xraw)
# plotTree(xfrag)
# print(xfrag)
# xset <- xcmsSet(cdffiles,fwhm = 30)
# xset <- xcmsSet(cdffiles,fwhm = 10)
# numPoints=length(data$INT)
# raw=new("xcmsRaw")
# raw@env$mz=data$MZ
# raw@scantime=data$SEC
# raw@scanindex=1:numPoints
# raw@env$intensity=data$INT
# raw@tic=data$INT
# peak=findPeaks.matchedFilter(raw)
# peak=as.data.frame(peak)
# print(peak)
# # print(xset@peaks)
# # peak=xset@peaks
# # peak=xset@peaks
# rt=peak$rt
# rtmin=peak$rtmin
# rtmax=peak$rtmax
# # print(rt)
# png(file = "tmp.png",width=1200,height=800)
# # smoothData21 = savgol(data$INT,21)
# smoothData21 = data$INT
# plot(data$SEC, smoothData21, col = "red",pch=20)
# lines(data$SEC, smoothData21, col = "grey")
# abline(v=rt, col = "red",lty=3)
# abline(v=rtmin, col = "blue",lty=3)
# abline(v=rtmax, col = "blue",lty=3)
# dev.off()
# plotChrom(raw,fitgauss=T)


# peak=findPeaks.matchedFilter(raw,fwhm=30)
# print(peak)
# peak=as.data.frame(peak)
# print(peak)
# print(summary(peak))
# print(xset)
# diffreport(xset,class1="MS1")
# dev.off()



