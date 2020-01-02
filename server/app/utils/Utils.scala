package utils

import java.io.{File, FileInputStream, FileOutputStream}
import java.lang.reflect.Field
import java.text.SimpleDateFormat

import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.{Cell, DateUtil}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTime
import org.apache.commons.codec.binary.Base64
import play.api.libs.json.Json

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.jdk.CollectionConverters._
import implicits.Implicits._

//import scala.math.log10

object Utils {

  def isWindows = {
    System.getProperty("os.name") match {
      case x if x.contains("Windows") => true
      case _ => false
    }
  }

  val Rscript = {
    "Rscript"
  }

  def createDirectoryWhenNoExist(file: File): Unit = {
    if (!file.exists && !file.isDirectory) FileUtils.forceMkdir(file)

  }

  def getPrefix(file: File): String = {
    val fileName = file.getName
    getPrefix(fileName)
  }

  def getPrefix(fileName: String): String = {
    val index = fileName.lastIndexOf(".")
    fileName.substring(0, index)
  }


  def deleteDirectory(direcotry: File) = {
    try {
      FileUtils.deleteDirectory(direcotry)
    } catch {
      case _ =>
    }
  }

  def getTime(startTime: Long) = {
    val endTime = System.currentTimeMillis()
    (endTime - startTime) / 1000.0
  }

  def isDoubleP(value: String, p: Double => Boolean): Boolean = {
    try {
      val dbValue = value.toDouble
      p(dbValue)
    } catch {
      case _: Exception =>
        false
    }
  }

  def lfJoin(seq: Seq[String]) = {
    seq.mkString("\n")
  }

  def execFuture[T](f: Future[T]): T = {
    Await.result(f, Duration.Inf)
  }

  def getValue[T](kind: T, noneMessage: String = "暂无"): String = {
    kind match {
      case x if x.isInstanceOf[DateTime] => val time = x.asInstanceOf[DateTime]
        time.toString("yyyy-MM-dd HH:mm:ss")
      case x if x.isInstanceOf[Option[T]] => val option = x.asInstanceOf[Option[T]]
        if (option.isDefined) getValue(option.get, noneMessage) else noneMessage
      case _ => kind.toString
    }
  }


  def getArrayByTs[T](x: Seq[T]) = {
    x.map { y =>
      y.getClass.getDeclaredFields.toBuffer.map { x: Field =>
        x.setAccessible(true)
        val kind = x.get(y)
        val value = getValue(kind)
        (x.getName, value)
      }.init.toMap
    }
  }

  def getJsonByT[T](y: T) = {
    val map = y.getClass.getDeclaredFields.toBuffer.map { x: Field =>
      x.setAccessible(true)
      val kind = x.get(y)
      val value = getValue(kind, "")
      (x.getName, value)
    }.init.toMap
    Json.toJson(map)
  }

  def getJsonByTs[T](x: Seq[T]) = {
    val array = getArrayByTs(x)
    Json.toJson(array)
  }

  def peakAreaNormal(dataFile: File, coefficient: Double) = {
    val buffer = FileUtils.readLines(dataFile).asScala
    val array = buffer.map(_.split("\t"))
    val sumArray = new Array[Double](array(0).length)
    for (i <- 1 until array.length; j <- 1 until array(i).length) {
      sumArray(j) += array(i)(j).toDouble
    }
    for (i <- 1 until array.length; j <- 1 until array(i).length) {
      array(i)(j) = (coefficient * array(i)(j).toDouble / sumArray(j)).toString
    }
    FileUtils.writeLines(dataFile, array.map(_.mkString("\t")).asJava)
  }

  //
  //  def log2(x: Double) = log10(x) / log10(2.0)
  //
  //  def getStdErr(values: Array[Double]) = {
  //    val standardDeviation = new StandardDeviation
  //    val stderr = standardDeviation.evaluate(values) / Math.sqrt(values.length)
  //    stderr
  //  }

  def dealGeneIds(geneId: String) = {
    geneId.split("\n").map(_.trim).distinct.toBuffer
  }

  def dealInputFile(file: File) = {
    val lines = FileUtils.readLines(file).asScala
    val buffer = lines.map(_.trim)
    FileUtils.writeLines(file, buffer.asJava)
  }

  def dealFileHeader(file: File) = {
    val lines = FileUtils.readLines(file).asScala
    val headers = lines(0).split("\t")
    headers(0) = ""
    lines(0) = headers.mkString("\t")
    FileUtils.writeLines(file, lines.asJava)
  }


  def getInfoByFile(file: File) = {
    val lines =file.lines
    getInfoByLines(lines)
  }

  def getInfoByLines(lines: List[String]) = {
    val columnNames = lines.head.split("\t")
    val array = lines.drop(1).map { line =>
      val columns = line.split("\t").map { x =>
        x.replaceAll("^\"", "").replaceAll("\"$", "")
      }
      columnNames.zip(columns).toMap
    }
    (columnNames, array)
  }

  def getBase64Str(imageFile: File): String = {
    val inputStream = new FileInputStream(imageFile)
    val bytes = IOUtils.toByteArray(inputStream)
    val bytes64 = Base64.encodeBase64(bytes)
    inputStream.close()
    new String(bytes64)
  }

}
