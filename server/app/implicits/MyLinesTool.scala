package implicits

import java.io.{File, FileOutputStream}

import implicits.Implicits._
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import utils.Utils

import scala.collection.JavaConverters._

/**
 * Created by Administrator on 2019/9/12
 */
trait MyLinesTool {

  implicit class MyLines(lines: List[String])(implicit sep: String = "\t") {

    def mySplit(str: String) = str.split(sep).toList

    def isUniq = {
      lines.toSet.size == lines.size
    }


    def toLowerCase = {
      lines.map(_.toLowerCase)
    }

    def headers = lines.head.split(sep).toList.map(x => x.trimQuote)

    def lineMap = {
      lines.drop(1).map { line =>
        val columns = line.split(sep)
        headers.map(_.toLowerCase).zip(columns).toMap
      }

    }

    def toFile(file: File, append: Boolean = false, encoding: String = "UTF-8") = {
      FileUtils.writeLines(file, encoding, lines.asJava, append)

    }

    def notEmptyLines = lines.filter(x => StringUtils.isNotBlank(x))

    def notNALines = lines.filter(x => x != "NA")

    def notAnnoLines = lines.filter(x => !x.trim.startsWith("#"))

    def mapByColumns(n: Int, f: List[String] => List[String]) = {
      lines.take(n) ++ lines.drop(n).map { line =>
        val columns = mySplit(line)
        val newColumns = f(columns)
        newColumns.mkString(sep)
      }

    }

    def mapByColumns(f: List[String] => List[String]): List[String] = {
      mapByColumns(0, f)
    }

    def mapOtherByColumns[T](f: List[String] => T) = {
      lines.map { line =>
        val columns = mySplit(line)
        f(columns)
      }

    }


    def flatMapSetByColumns[T](f: List[String] => Set[T])(implicit sep: String = "\t") = {
      lines.flatMap { line =>
        val columns = line.mySplit(sep)
        f(columns)
      }.toSet

    }

    def flatMapByColumns[T](f: List[String] => Seq[T])(implicit sep: String = "\t") = {
      lines.flatMap { line =>
        val columns = line.mySplit(sep)
        f(columns)
      }

    }

    def uniqByColumnName(columnName: String) = {
      val contentLines = lines.drop(1).map { line =>
        val columns = mySplit(line)
        val map = columns.zip(headers).map { case (value, header) =>
          (header, value)
        }.toMap
        (map(columnName).toLowerCase, line)
      }.toMap.values.toList
      lines.head :: contentLines
    }

    def filterLineSize(f: Int => Boolean) = {
      lines.filter { line =>
        val size = line.split("\t").size
        f(size)
      }
    }

    def selectOneColumn(columnName: String) = {
      val maps = lineMap
      maps.map { map =>
        map(columnName)
      }

    }

    def trimQuote = {
      lines.mapByColumns { columns =>
        columns.map(x => x.trimQuote)
      }
    }

    def lfJoin = {
      lines.mkString("\n")
    }

    def toXlsxFile(xlsxFile: File) = {
      val outputWorkbook = new XSSFWorkbook()
      val outputSheet = outputWorkbook.createSheet("Sheet1")
      val cellStyle = outputWorkbook.createCellStyle()
      cellStyle.setDataFormat(outputWorkbook.getCreationHelper.createDataFormat().getFormat("#.#"))
      for (i <- 0 until lines.size) {
        val outputEachRow = outputSheet.createRow(i)
        val line = lines(i)
        val columns = line.split("\t")
        for (j <- 0 until columns.size) {
          val cell = outputEachRow.createCell(j)
          if (columns(j).isDouble) {
            cell.setCellValue(columns(j).toDouble)
//            cell.setCellStyle(cellStyle)
          } else {
            cell.setCellValue(columns(j))
          }

        }
      }

      val fileOutputStream = new FileOutputStream(xlsxFile)
      outputWorkbook.write(fileOutputStream)
      fileOutputStream.close()
      outputWorkbook.close()
    }


  }


}
