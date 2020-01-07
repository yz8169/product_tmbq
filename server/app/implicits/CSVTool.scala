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
        headers.zip(columns).toMap
      }
    }

    def lineSeqMap = {
      val headers = lines.head.toLowerCase
      lines.drop(1).map { columns =>
        headers.zip(columns).toSeqMap
      }
    }

    //    def leftJoin(otherLines: List[List[String]], by: String) = {
    //      val otherHeaders = otherLines.head.filter(x => x.toLowerCase != by.toLowerCase())
    //      val otherIndex = otherLines.head.toLowerCase.indexOf(by.toLowerCase())
    //      val otherMap = otherLines.drop(1).map { columns =>
    //        (columns(otherIndex) -> (columns.take(otherIndex) ::: columns.drop(otherIndex + 1)))
    //      }.toMap
    //      val newHeaders = lines.head ::: otherHeaders
    //      val index = lines.head.toLowerCase.indexOf(by.toLowerCase())
    //      newHeaders :: lines.drop(1).map { columns =>
    //        val key = columns(index)
    //        columns ::: otherMap(key)
    //      }
    //    }

    def leftJoin(otherLines: List[List[String]], by: String) = {
      val lowerBy = by.toLowerCase()
      val otherHeaders = otherLines.head.filter(x => x.toLowerCase != lowerBy)
      val otherMap = otherLines.lineSeqMap.map { map =>
        (map(lowerBy) -> map.valuesList.filter(x => x != map(lowerBy)))
      }.toMap
      val newHeaders = lines.head ::: otherHeaders
      newHeaders :: lines.lineSeqMap.map { map =>
        val key = map(lowerBy)
        val columns = map.valuesList
        columns ::: otherMap(key)
      }
    }

    def reOrder(headers: List[String]) = {
      val newHeaders = headers ::: lines.head.filter(x => !headers.contains(x))
      val otherLines = lines.lineSeqMap.map { map =>
        val newKeys = newHeaders.toLowerCase
        newKeys.map { header =>
          map(header)
        }
      }
      newHeaders :: otherLines
    }

    def selectRemove(variableName: String) = {
      val index = lines.head.toLowerCase.indexOf(variableName.toLowerCase())
      lines.map { columns =>
        columns.take(index) ::: columns.drop(index + 1)
      }
    }

    def selectOneColumn(columnName: String) = {
      val maps = lineMap
      maps.map { map =>
        map(columnName.toLowerCase)
      }
    }

    def selectColumns(columnNames: List[String]) = {
      val maps = lineSeqMap
      val newHeaders = lines.head.filter(x => columnNames.toLowerCase.contains(x.toLowerCase()))
      newHeaders :: maps.map { map =>
        columnNames.map { columnName =>
          map(columnName.toLowerCase)
        }
      }
    }

    def rename(t: (String, String)) = {
      lines.head.map { header =>
        if (header.equalsIgnoreCase(t._1)) {
          t._2
        } else header
      } :: lines.tail
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
      file
    }

    def toLowerCase = {
      lines.map(_.toLowerCase)
    }


  }


}
