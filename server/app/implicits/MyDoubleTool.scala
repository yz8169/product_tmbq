package implicits

import java.io.File

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils

import scala.math.log10
import scala.util.Try

/**
 * Created by Administrator on 2019/9/12
 */
trait MyDoubleTool {

  implicit class MyDouble(db: Double) {

    def getGb = {
      (db / 1024 / 1024 / 1024).toInt
    }

    def log2 = log10(db) / log10(2.0)


  }


}
