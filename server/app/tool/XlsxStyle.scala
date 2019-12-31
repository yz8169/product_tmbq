package tool

import java.awt.Color

import org.apache.poi.ss.usermodel.{FillPatternType, IndexedColors}
import org.apache.poi.xssf.usermodel.{XSSFColor, XSSFWorkbook}

/**
 * Created by Administrator on 2019/12/10
 */
class XlsxStyle(workbook: XSSFWorkbook) {

  val emptyStyle = {
    workbook.createCellStyle()
  }

  val blueStyle = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    val color = new XSSFColor(Color.BLUE)
    style.setFillForegroundColor(color)
    style
  }

  val cyanStyle = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    val color = new XSSFColor(Color.cyan)
    style.setFillForegroundColor(color)
    style
  }

  val darkRedStyle = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex)
    style
  }

  val redStyle = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    style.setFillForegroundColor(IndexedColors.RED.getIndex)
    style
  }

  val orangeStyle = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    style.setFillForegroundColor(IndexedColors.ORANGE.getIndex)
    style
  }

  val yellowStyle = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    style.setFillForegroundColor(IndexedColors.YELLOW.getIndex)
    style
  }

  val greenStyle = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    val color = new XSSFColor(new Color(146, 208, 80))
    style.setFillForegroundColor(color)
    style
  }


}
