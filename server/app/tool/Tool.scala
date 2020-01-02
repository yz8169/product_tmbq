package tool

import java.awt.Color
import java.io.{File, FileInputStream, FileOutputStream}
import java.net.URLEncoder
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.concurrent.ForkJoinPool

import javax.inject.Inject
import org.joda.time.DateTime
import utils.Utils
import dao._
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.{Cell, DateUtil, FillPatternType, IndexedColors}
import org.apache.poi.xssf.usermodel.{XSSFColor, XSSFWorkbook}
import play.api.mvc.{MultipartFormData, Request, RequestHeader}

import scala.collection.parallel.ForkJoinTaskSupport
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import implicits.Implicits._
import org.zeroturnaround.zip.ZipUtil
import play.api.libs.Files.TemporaryFile
import tool.Pojo.{AdminMyDataDir, CommandData, IndexData, MyDataDir}

import scala.collection.parallel.CollectionConverters._
import scala.jdk.CollectionConverters._


/**
 * Created by yz on 2018/9/19
 */

object Tool {

  val dbName = "product_tmbq_database"
  val playPath = new File("../").getAbsolutePath
  val linuxPath = playPath + s"/${dbName}"
  val windowsTestDir = new File("G:\\temp")
  val linuxTestDir = new File(playPath, "workspace")
  val testDir = if (windowsTestDir.exists()) windowsTestDir else linuxTestDir
  val windowsPath = s"D:\\${dbName}"
  val path = {
    if (new File(windowsPath).exists()) windowsPath else linuxPath
  }
  val dataDir = new File(path, "data")
  val kitDir=new File(dataDir,"kit")


  val rPath = {
    val rPath = "C:\\workspaceForIDEA\\research_tmbq\\server\\rScripts"
    val linuxRPath = linuxPath + "/rScripts"
    if (new File(rPath).exists()) rPath else linuxRPath
  }


  def generateMissionName = {
    (new DateTime).toString("yyyy_MM_dd_HH_mm_ss")
  }

  def isTestMode(implicit modeDao: ModeDao) = {
    val mode = Utils.execFuture(modeDao.select)
    if (mode.test == "t") true else false
  }

  def getCompoundDatas(compoundConfigFile: File) = {
    val compoundLines = compoundConfigFile.xlsxLines()
    case class CompoundData(name: String, function: String, mz: Double, index: Int)
    val headers = compoundLines.head.map(_.toLowerCase)
    val maps = compoundLines.drop(1).map { columns =>
      headers.zip(columns).toMap
    }
    val mzMap = maps.map { map =>
      val mzs = map("mass").split(">")
      val mz1 = if (mzs.size == 1) 0.0 else mzs(1).toDouble
      val function = map("function")
      ((function, mzs(0).toDouble), mz1)
    }.distinct.groupBy(_._1).mapValues(x => x.map(_._2).sorted)
    maps.map { map =>
      val name = map("compound")
      val function = map("function")
      val mzs = map("mass").split(">")
      val mz = mzs(0).toDouble
      val index = if (mzs.size == 1) {
        0
      } else {
        val mz1 = mzs(1).toDouble
        mzMap((function, mz)).indexOf(mz1)
      }
      CompoundData(name, function, mz, index)
    }
  }

  def getFtMap(file: File, functions: List[String]) = {
    val lines = FileUtils.readLines(file).asScala

    val acc = Vector[(String, String)]()
    val map = lines.foldLeft((acc, "")) { (info, line) =>
      if (line.startsWith("FUNCTION")) {
        (info._1, line)
      } else {
        val key = info._2
        (info._1 :+ (key, line), key)
      }
    }.accGroupMap

    val ftMap = map.withFilter(x => functions.contains(x._1)).map { case (key, lines) =>
      val acc = Vector[((Double, Double), Double)]()
      val map = lines.filterNot(StringUtils.isBlank(_)).filterNot(_.startsWith("Scan")).
        foldLeft((acc, 0.0)) { (info, line) =>
          if (line.startsWith("Retention Time")) {
            val timeKey = line.split("\t")(1).toDouble
            (info._1, timeKey)
          } else {
            val timeKey = info._2
            val columns = line.split("\t")
            val t = (columns(0).toDouble, timeKey)
            val value = columns(1).toDouble
            val curT = (t, value)
            (info._1 :+ curT, timeKey)
          }
        }.accGroupMap
      (key, map)
    }

    ftMap
  }

  def getAgilentFtMap(file: File) = {
    val lines = FileUtils.readLines(file).asScala
    lines.zipWithIndex.withFilter { case (line, i) =>
      line.trim.startsWith("index:")
    }.map { case (line, i) =>
      val trimLine = line.trim
      val r = "^index:\\s+(\\d+)$".r
      val r(index) = trimLine
      val buffer = lines.drop(i + 1).takeWhile { x =>
        !x.trim().startsWith("index:")
      }
      val times = buffer.zipWithIndex.withFilter { case (line, i) =>
        line.contains("cvParam: time array,")
      }.map(x => buffer(x._2 + 1)).flatMap { line =>
        line.trim.replaceAll("^.*\\]\\s+", "").split("\\s+").map(_.toDouble).toBuffer
      }
      val values = buffer.zipWithIndex.withFilter { case (line, i) =>
        line.contains("cvParam: intensity array,")
      }.map(x => buffer(x._2 + 1)).flatMap { line =>
        line.trim.replaceAll("^.*\\]\\s+", "").split("\\s+").map(_.toDouble).toBuffer
      }
      (index.toDouble, times.zip(values))
    }.toMap
  }

  def productDtaFiles(tmpDir: File, compoundConfigFile: File, dataDir: File, threadNum: Int) = {
    val dtaDir = new File(tmpDir, "dta").createDirectoryWhenNoExist
    val compounds = getCompoundDatas(compoundConfigFile)
    val functions = compounds.map(x => s"FUNCTION ${x.function}")
    val files = dataDir.listFiles()
    val finalThreadNum = threadNum
    val map = files.zipWithIndex.map { case (v, i) =>
      val j = (i % finalThreadNum) + 1
      (j, v)
    }.groupBy(_._1).mapValues(_.map(_._2))
    val f = map.map { case (i, files) =>
      Future {
        files.foreach { file =>
          val ftMap = getFtMap(file, functions)
          compounds.foreach { compound =>
            val function = s"FUNCTION ${compound.function}"
            val headers = (s"#SEC\tMZ\tINT")
            val trueMz = ftMap(function).map { case ((mz, time), value) =>
              mz
            }.toBuffer.distinct.sortBy { x: Double => (compound.mz - x).abs }.head
            val ftLines = ftMap(function).filter { case ((mz, time), value) =>
              mz == trueMz
            }.map { case ((mz, time), values) =>
              s"${time}\t${mz}\t${values(compound.index)}"
            }.toList
            val newLines = headers :: ftLines
            val dir = new File(dtaDir, compound.name)
            dir.createDirectoryWhenNoExist
            val prefix = file.namePrefix
            FileUtils.writeLines(new File(dir, s"${prefix}.dta"), newLines.asJava)
          }
        }
      }
    }.toBuffer.reduceLeft(_ zip _ map (x => ()))
    Utils.execFuture(f)
  }

  def productAgilentDtaFiles(tmpDir: File, compoundConfigFile: File, dataDir: File, threadNum: Int) = {
    val dtaDir = new File(tmpDir, "dta").createDirectoryWhenNoExist
    val compounds = getCompoundDatas(compoundConfigFile)
    val files = dataDir.listFiles()
    val finalThreadNum = threadNum
    val map = files.zipWithIndex.map { case (v, i) =>
      val j = (i % finalThreadNum) + 1
      (j, v)
    }.groupBy(_._1).mapValues(_.map(_._2))
    val f = map.map { case (i, files) =>
      Future {
        files.foreach { file =>
          val ftMap = getAgilentFtMap(file)
          compounds.foreach { compound =>
            val function = s"${compound.function}"
            val headers = (s"#SEC\tMZ\tINT")
            val ftLines = ftMap(function.toDouble).map { case (time, value) =>
              s"${time}\t${compound.mz}\t${value}"
            }.toList
            val newLines = headers :: ftLines
            val dir = new File(dtaDir, compound.name).createDirectoryWhenNoExist
            val prefix = file.namePrefix
            FileUtils.writeLines(new File(dir, s"${prefix}.dta"), newLines.asJava)
          }
        }
      }
    }.toBuffer.reduceLeft(_ zip _ map (x => ()))
    Utils.execFuture(f)
  }

  def deleteDirectory(direcotry: File)(implicit modeDao: ModeDao) = {
    if (!isTestMode) Utils.deleteDirectory(direcotry)
  }

  def getAdminDataDir(dataDir: File)(implicit request: Request[MultipartFormData[TemporaryFile]]) = {
    val compoundConfigFile = new File(dataDir, "compound_config.xlsx")
    WebTool.fileMove("compoundConfigFile", compoundConfigFile)
    compoundConfigFile.removeEmptyLine
    AdminMyDataDir(dataDir, compoundConfigFile)
  }

  def getUserId(implicit request: RequestHeader) = {
    request.session.get("id").get.toInt
  }

  def getUserIdDir(implicit request: RequestHeader) = {
    val userId = getUserId
    new File(Utils.userDir, userId.toString)
  }

  def getUserIdDir(userId: Int) = {
    new File(Utils.userDir, userId.toString)
  }

  def getKitFile(id: Int) = {
    new File(Tool.kitDir, s"${id}.xlsx")
  }

  def getUserMissionDir(implicit request: RequestHeader) = {
    val userIdDir = getUserIdDir
    new File(userIdDir, "mission")
  }

  def getUserAdjustMissionDir(implicit request: RequestHeader) = {
    val userIdDir = getUserIdDir
    new File(userIdDir, "adjust_mission")
  }

  def getMissionIdDirById(missionId: Int)(implicit request: RequestHeader) = {
    val userMissionFile = getUserMissionDir
    new File(userMissionFile, missionId.toString)
  }

  def getAdjustMissionIdDirById(missionId: Int)(implicit request: RequestHeader) = {
    val userMissionFile = getUserAdjustMissionDir
    new File(userMissionFile, missionId.toString)
  }

  def getWorkspaceDirById(missionId: Int)(implicit request: RequestHeader) = {
    val missionIdDir = getMissionIdDirById(missionId)
    new File(missionIdDir, "workspace")
  }

  def getYellowStyle(workbook: XSSFWorkbook) = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    style.setFillForegroundColor(IndexedColors.YELLOW.getIndex)
    style
  }

  def getRedStyle(workbook: XSSFWorkbook) = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    style.setFillForegroundColor(IndexedColors.RED.getIndex)
    style
  }

  def getGreenStyle(workbook: XSSFWorkbook) = {
    val style = workbook.createCellStyle()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    val color = new XSSFColor(new Color(146, 208, 80))
    style.setFillForegroundColor(color)
    style
  }

  def dye(file: File, colorFile: File, outFile: File) = {
    val outputWorkbook = new XSSFWorkbook()
    val outputSheet = outputWorkbook.createSheet("Sheet1")
    val format = outputWorkbook.createDataFormat()
    val lines = FileUtils.readLines(file).asScala
    val colorLines = FileUtils.readLines(colorFile).asScala.map(_.split("\t"))
    val xlsxStyle = new XlsxStyle(outputWorkbook)
    val yellowStyle = xlsxStyle.yellowStyle
    val redStyle = xlsxStyle.redStyle
    val greenStyle = xlsxStyle.greenStyle
    for (i <- 0 until lines.size) {
      val outputEachRow = outputSheet.createRow(i)
      val line = lines(i)
      val columns = line.split("\t")
      for (j <- 0 until columns.size) {
        val cell = outputEachRow.createCell(j)
        cell.setCellValue(columns(j))
        val bat = colorLines(i)(1)
        if (i > 0 && j > 1 && bat.isDouble) {
          if (columns(j).isDouble) {
            cell.setCellValue(columns(j).toDouble)
          }
          colorLines(i)(j) match {
            case "yellow" =>
              cell.setCellStyle(yellowStyle)
            case "red" => cell.setCellStyle(redStyle)
            case "green" => cell.setCellStyle(greenStyle)
            case _ =>
          }
        }

      }
    }
    val fileOutputStream = new FileOutputStream(outFile)
    outputWorkbook.write(fileOutputStream)
    fileOutputStream.close()
    outputWorkbook.close()
  }

  def getCompoundMap(comoundConfigFile: File) = {
    val lines = comoundConfigFile.xlsxLines()
    lines.drop(1).map { columns =>
      getRowMap(lines.head, columns)
    }
  }

  def getRowMap(headers: List[String], columns: List[String]) = {
    headers.zip(columns).toMap
  }

  def productBaseRFile(tmpDir: File) = {
    val rBaseFile = new File(Utils.rPath, "base.R")
    FileUtils.copyFileToDirectory(rBaseFile, tmpDir)
  }

  def createTempDirectory(prefix: String)(implicit modeDao: ModeDao) = {
    if (isTestMode) Tool.testDir else Files.createTempDirectory(prefix).toFile
  }

  val standardUnit = "mol/L"

  def getmwMap(mw: Double) = {
    val map = Map(
      (standardUnit, "g/mL") -> math.pow(10, -3) * mw,
      (standardUnit, "mg/mL") -> 1 * mw,
      (standardUnit, "mg/g") -> 1 * mw,
      (standardUnit, "ug/mL") -> math.pow(10, 3) * mw,
      (standardUnit, "ug/g") -> math.pow(10, 3) * mw,
      (standardUnit, "ug/uL") -> 1 * mw,
      (standardUnit, "ug/L") -> math.pow(10, 6) * mw,
      (standardUnit, "ng/g") -> math.pow(10, 6) * mw,
      (standardUnit, "ppm") -> math.pow(10, 3) * mw,
      (standardUnit, "ppb") -> math.pow(10, 6) * mw,
      (standardUnit, "ng/mL") -> math.pow(10, 6) * mw,
      (standardUnit, "ppt") -> math.pow(10, 9) * mw
    )
    map ++ map.map { case ((standardUnit, unit), v) =>
      (unit, standardUnit) -> 1 / v
    }
  }

  def getNoMwMap = {
    val map = Map(
      (standardUnit, "nM") -> math.pow(10, 9),
      (standardUnit, "uM") -> math.pow(10, 6),
      (standardUnit, "mM") -> math.pow(10, 3),
      (standardUnit, "nmol/g") -> math.pow(10, 6),
      (standardUnit, "nmol/mg") -> math.pow(10, 3),
      (standardUnit, "pmol/mg") -> math.pow(10, 6),
      (standardUnit, "umol/mg") -> math.pow(10, 0),
      (standardUnit, "umol/g") -> math.pow(10, 3)
    )
    map ++ map.map { case ((standardUnit, unit), v) =>
      (unit, standardUnit) -> 1 / v
    }
  }

  def urlEncode(url: String) = {
    URLEncoder.encode(url, "UTF-8")
  }

  def getContentDisposition(url: String) = {
    val encodeUrl = urlEncode(url)
    s"attachment; filename*=utf-8''${encodeUrl}"
  }

  def isFindPeak(tmpDir: File, isIndexs: Seq[IndexData], threadNum: Int) = {
    val isIndexPar = isIndexs.par.zipWithIndex
    isIndexPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    val commandsPar = isIndexPar.map { case (indexData, i) =>
      val fileName = s"is_${i}"
      val isDir = new File(tmpDir, fileName).createDirectoryWhenNoExist
      val compoundNameFile = new File(isDir, "compoundName.xlsx")
      val newLines = List("CompoundName", indexData.compoundName)
      newLines.toXlsxFile(compoundNameFile)
      val command =
        s"""
           |Rscript ${new File(Tool.rPath, "isFindPeak.R").unixPath} --ci ${compoundNameFile.getName} --co color.txt --io intensity.txt
           """.stripMargin
      CommandData(isDir, List(command))
    }
    commandsPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    commandsPar

  }

  def cFindPeak(tmpDir: File, indexDatas: Seq[IndexData], threadNum: Int) = {
    val cIndexs = indexDatas.filter(x => !x.index.startWithsIgnoreCase("is"))
    val cIndexPar = cIndexs.par.zipWithIndex
    cIndexPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    val commandsPar = cIndexPar.map { case (indexData, i) =>
      val fileName = s"c_${i}"
      val cDir = new File(tmpDir, fileName).createDirectoryWhenNoExist
      val compoundNameFile = new File(cDir, "compoundName.xlsx")
      val newLines = List("CompoundName", indexData.compoundName)
      newLines.toXlsxFile(compoundNameFile)
      val command =
        s"""
           |Rscript ${new File(Tool.rPath, "c_findPeak.R").unixPath} --ci ${compoundNameFile.getName} --co color.txt --io intensity.txt
           """.stripMargin
      CommandData(cDir, List(command))
    }
    commandsPar.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(threadNum))
    commandsPar
  }

  def eachRegress(tmpDir: File, threadNum: Int) = {
    val dirs = tmpDir.listFiles().filter(_.isDirectory).
      filter(x => x.getName.startsWith("is_") || x.getName.startsWith("c_")).toList
    val commandsPar = dirs.map { dir =>
      val command =
        s"""
           |Rscript ${new File(Tool.rPath, "each_regress.R").unixPath} --ci compoundName.xlsx  --coi color.txt --ro regress.txt
                          """.stripMargin
      CommandData(dir, List(command))
    }.par

    commandsPar.threadNum(threadNum)
  }


  def intensityMerge(tmpDir: File) = {
    val command =
      s"""
         |Rscript ${new File(Tool.rPath, "intensity_merge.R").unixPath}
           """.stripMargin
    CommandData(tmpDir, List(command))
  }

  def allMerge(tmpDir: File) = {
    val command =
      s"""
         |Rscript ${new File(Tool.rPath, "all_merge.R").unixPath}
           """.stripMargin
    CommandData(tmpDir, List(command))
  }

  def isMerge(tmpDir: File, isIndexs: Seq[IndexData]) = {
    val command = if (isIndexs.isEmpty) {
      ""
    } else {
      s"""
         |Rscript ${new File(Tool.rPath, "is_rt_merge.R").unixPath}
           """.stripMargin
    }
    CommandData(tmpDir, List(command))
  }


}
