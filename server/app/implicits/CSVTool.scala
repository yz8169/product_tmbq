package implicits

import java.io.File

import com.github.tototoshi.csv._
import org.apache.commons.io.FileUtils
import implicits.Implicits._
import org.apache.commons.lang3.StringUtils

/**
 * Created by Administrator on 2019/9/12
 */
trait CSVTool {

  implicit class CSVFile(file: File) {

    def csvLines = {
      val reader = CSVReader.open(file)
      val lines = reader.all()
      reader.close()
      lines
    }

  }

  implicit class CSVLines(lines: List[List[String]]) {

    def toFile(file: File) = {
      val writer = CSVWriter.open(file)
      writer.writeAll(lines)
      writer.close()
    }

    def convertHeader(map: Map[String, String]) = {
      val newHeaders = lines.head.map { header =>
        map.getOrElse(header.toLowerCase, header)
      }
      newHeaders +: lines.drop(1)
    }

    def lineMap = {
      val headers = lines.head.toLowerCase
      lines.drop(1).map { columns =>
        headers.map(_.toLowerCase).zip(columns).toMap
      }
    }

    def selectOneColumn(columnName: String) = {
      val maps = lineMap
      maps.map { map =>
        map(columnName.toLowerCase)
      }
    }

    def mapOtherByColumns[T](f: List[String] => T) = {
      lines.map { columns =>
        f(columns)
      }

    }

    def notEmptyLines = lines.filter(x => x.exists(y => StringUtils.isNotEmpty(y)))

    def toTxtFile(file: File) = {
      lines.map(_.mkString("\t")).toFile(file)
    }

    def toXlsxFile(file: File) = {
      lines.map(_.mkString("\t")).toXlsxFile(file)
    }


  }


}
