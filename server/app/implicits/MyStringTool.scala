package implicits

import java.io.File

import org.apache.commons.codec.binary.Base64
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import utils.Utils

import scala.util.Try

/**
 * Created by Administrator on 2019/9/12
 */
trait MyStringTool {

  implicit class MyString(v: String) {

    def isInt = {
      Try(v.toInt).toOption match {
        case Some(value) => true
        case None => false
      }
    }

    def unixPath = {
      v.replace("\\", "/").replaceAll("D:", "/mnt/d").
        replaceAll("E:", "/mnt/e").replaceAll("C:", "/mnt/c").
        replaceAll("G:", "/mnt/g")
    }

    def startWithsIgnoreCase(prefix: String) = {
      v.toLowerCase.startsWith(prefix.toLowerCase)
    }

    def toFile(file: File, encoding: String = "UTF-8", append: Boolean = false): Unit = {
      FileUtils.writeStringToFile(file, v, encoding, append)
    }

    def trimQuote = {
      v.replaceAll("^\"", "").replaceAll("\"$", "")
    }

    def mySplit(sep: String = "\t") = {
      v.split(sep).toList
    }

    def isBlank = StringUtils.isBlank(v)

    def wsl = {
      if (Utils.isWindows) s"wsl ${v}" else v
    }

    def isValidRVar = {
      (!v.matches("^\\.\\d+.*$")) && !(v.matches("^[\\d_]+.*$")) && (v.matches("^[\\w\\.]+$"))
    }


    def fileNamePrefix: String = {
      val index = v.lastIndexOf(".")
      v.substring(0, index)
    }

    def splitByTab = {
      v.mySplit("\t")
    }

    def isDoubleP(p: Double => Boolean): Boolean = {
      Try(v.toDouble).toOption match {
        case Some(value) => p(value)
        case None => false
      }
    }

    def isDouble: Boolean = {
      Try(v.toDouble).toOption match {
        case Some(value) => true
        case None => false
      }
    }

    def base64Str = {
      Base64.encodeBase64String(v.getBytes)
    }

    def base64DecodeStr = {
      new String(Base64.decodeBase64(v))
    }

    def replaceLf={
      v.replaceAll("\n"," ").replaceAll("\r"," ")
    }

  }


}
