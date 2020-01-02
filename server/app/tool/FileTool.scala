package tool

import java.io.File

import org.apache.commons.lang3.StringUtils
import tool.Pojo.{AdminMyDataDir, MyDataDir, MyMessage}
import implicits.Implicits._


/**
 * Created by Administrator on 2019/12/19
 */
object FileTool {

  def fileCheck(myTmpDir: MyDataDir) = {
    val fileNames = myTmpDir.tmpDataDir.allFiles.map(_.getName).filter(StringUtils.isNotBlank(_)).
      map(_.fileNamePrefix).map(_.toLowerCase())
    val compoundConfigFile = myTmpDir.compoundConfigFile
    val sampleConfigFile = myTmpDir.sampleConfigExcelFile
    FileTool.compoundFileCheck(compoundConfigFile, sampleConfigFile).andThen { b =>
      FileTool.sampleFileCheck(sampleConfigFile, fileNames)
    }.toMyMessage

  }

  def adminFileCheck(myTmpDir: AdminMyDataDir) = {
    val compoundConfigFile = myTmpDir.compoundConfigFile
    FileTool.adminCompoundFileCheck(compoundConfigFile).toMyMessage
  }

  def compoundFileCheck(file: File, sampleConfigFile: File) = {
    val sampleHeaders = sampleConfigFile.xlsxLines().map(x => x.toLowerCase).head
    val lines = file.xlsxLines().map(_.toLowerCase)
    CompoundFileValidTool.valid(lines, sampleHeaders)
  }

  def adminCompoundFileCheck(file: File) = {
    val lines = file.xlsxLines().map(_.toLowerCase)
    CompoundFileValidTool.adminValid(lines)
  }

  def sampleFileCheck(file: File, fileNames: List[String]) = {
    val lines = file.xlsxLines().map(_.toLowerCase)
    SampleFileValidTool.valid(lines, fileNames)
  }

}
