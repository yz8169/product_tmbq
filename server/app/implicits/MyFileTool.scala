package implicits

import java.io.{File, FileInputStream}

import com.github.tototoshi.csv._
import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._
import implicits.Implicits._
import org.apache.commons.codec.binary.Base64

/**
 * Created by Administrator on 2019/9/12
 */
trait MyFileTool {

  implicit class MyFile(file: File) {

    def unixPath = {
      val path = file.getAbsolutePath
      path.unixPath
    }

    def lines: List[String] = lines()

    def lines(encoding: String = "UTF-8") = FileUtils.readLines(file, encoding).asScala.toList

    def txtLines = lines().map(_.split("\t").toList)

    def str = FileUtils.readFileToString(file, "UTF-8")

    def copyTo(destFile: File) = {
      FileUtils.copyFile(file, destFile)
    }

    def fileCopyToDir(destDir: File) = {
      FileUtils.copyFileToDirectory(file, destDir)
    }

    def dirCopyToDir(destDir: File) = {
      FileUtils.copyDirectoryToDirectory(file, destDir)
    }

    def allFiles: List[File] = {

      def loop(acc: List[File], files: List[File]): List[File] = {
        files match {
          case Nil => acc
          case x :: xs => x.isDirectory match {
            case false => loop(x :: acc, xs)
            case true => loop(acc, xs ::: x.listFiles().toList)
          }
        }
      }

      loop(List(), List(file))
    }

    def createDirectoryWhenNoExist = {
      if (!file.exists && !file.isDirectory) FileUtils.forceMkdir(file)
      file
    }

    def reCreateDirectoryWhenExist = {
      if (file.exists && file.isDirectory) {
        file.deleteQuietly
        FileUtils.forceMkdir(file)
      }
      file
    }

    def namePrefix: String = {
      val fileName = file.getName
      fileName.fileNamePrefix
    }

    def deleteQuietly = {
      FileUtils.deleteQuietly(file)
    }

    def toXlsxFile(xlsxFile: File) = {
      val lines = file.lines
      lines.toXlsxFile(xlsxFile)
    }

    def base64: String = {
      val inputStream = new FileInputStream(file)
      val bytes = IOUtils.toByteArray(inputStream)
      val bytes64 = Base64.encodeBase64(bytes)
      inputStream.close()
      new String(bytes64)
    }


  }


}
