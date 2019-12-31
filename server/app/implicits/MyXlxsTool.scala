package implicits

import java.io.{File, FileInputStream}
import java.text.SimpleDateFormat

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.{Cell, DateUtil}
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import scala.collection.JavaConverters._
import implicits.Implicits._

/**
 * Created by Administrator on 2019/11/11
 */

trait MyXlxsTool {

  implicit class MyXlxsFile(file: File) {

    def xlsxLines(sheetIndex: Int = 0) = {
      val is = new FileInputStream(file.getAbsolutePath)
      val xssfWorkbook = new XSSFWorkbook(is)
      val xssfSheet = xssfWorkbook.getSheetAt(sheetIndex)
      val lines = (0 to xssfSheet.getLastRowNum).toList.map { i =>
        val xssfRow = xssfSheet.getRow(i)
        val firstRow = xssfSheet.getRow(0)
        (0 until firstRow.getLastCellNum).map { j =>
          val cell = xssfRow.getCell(j)
          val value = if (cell == null) {
            ""
          } else {
            cell.getCellType match {
              case Cell.CELL_TYPE_STRING =>
                cell.getStringCellValue
              case Cell.CELL_TYPE_NUMERIC =>
                if (DateUtil.isCellDateFormatted(cell)) {
                  val dateFormat = new SimpleDateFormat("yyyy/MM/dd")
                  dateFormat.format(cell.getDateCellValue)
                } else {
                  val doubleValue = cell.getNumericCellValue
                  if (doubleValue == doubleValue.toInt) {
                    doubleValue.toInt.toString
                  } else doubleValue.toString
                }
              case _ =>
                ""
            }
          }
          value.trim
        }.toList
      }
      xssfWorkbook.close()
      lines.filter(x => x.exists(y => StringUtils.isNotBlank(y)))
    }

    def xlsx2Txt(txtFile: File) = {
      val lines = file.xlsxLines()
      lines.toTxtFile(txtFile)
    }

    def removeEmptyLine = {
      file.xlsxLines().toXlsxFile(file)
    }


  }


}


