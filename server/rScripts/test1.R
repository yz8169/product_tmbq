# Title     : TODO
# Objective : TODO
# Created by: yz
# Created on: 2018/9/30
getColorStr <- function(int, list){
    if (int < list$lod) {
        "red"
    }else if (int < list$loq) {
        print("yellow")
        "yellow"
    }else {
        "green"
    }
}

color<-getColorStr(15,list(loq=3,lod=2))
color