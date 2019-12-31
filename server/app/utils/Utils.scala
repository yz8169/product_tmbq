package utils

import java.io.{File, FileInputStream, FileOutputStream}
import java.lang.reflect.Field
import java.text.SimpleDateFormat

import org.apache.commons.io.{FileUtils, IOUtils}
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.{Cell, DateUtil}
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.joda.time.DateTime
//import org.apache.commons.math3.stat.StatUtils
//import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation
//import org.saddle.io._
//import CsvImplicits._
//import javax.imageio.ImageIO
import org.apache.commons.codec.binary.Base64
//import org.apache.pdfbox.pdmodel.PDDocument
//import org.apache.pdfbox.rendering.PDFRenderer
import play.api.libs.json.Json

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
//import scala.math.log10

object Utils {

  val dbName = "tmbq_database"
  val windowsPath = s"D:\\${dbName}"
  val playPath = new File("../").getAbsolutePath
  val linuxPath = playPath + s"/${dbName}"
  val isWindows = {
    if (new File(windowsPath).exists()) true else false
  }

  val path = {
    if (new File(windowsPath).exists()) windowsPath else linuxPath
  }
  val exampleFile = new File(path, "example")

  val binPath = new File(path, "bin")
  val anno = new File(binPath, "Anno")
  val orthoMcl = new File(binPath, "ORTHOMCLV1.4")
  val pipeLine = new File("/mnt/sdb/linait/pipeline/MicroGenome_pipeline/MicroGenome_pipeline_v3.0")
  val mummer = new File("/mnt/sdb/linait/tools/quickmerge/MUMmer3.23/")
  val blastFile = new File(binPath, "ncbi-blast-2.6.0+/bin/blastn")
  val blastpFile = new File(binPath, "ncbi-blast-2.6.0+/bin/blastp")
  val blastxFile = new File(binPath, "ncbi-blast-2.6.0+/bin/blastx")
  val blast2HtmlFile = new File(binPath, "blast2html-82b8c9722996/blast2html.py")
  val svBin = new File("/mnt/sdb/linait/pipeline/MicroGenome_pipeline/MicroGenome_pipeline_v3.0/src/SV_finder_2.2.1/bin/")
  val rPath = {
    val rPath = "C:\\workspaceForIDEA\\tmbq_scala_js\\server\\rScripts"
    val linuxRPath = linuxPath + "/rScripts"
    if (new File(rPath).exists()) rPath else linuxRPath
  }
  val userDir = new File(path, "user")

  val windowsTestDir = new File("G:\\temp")
  val linuxTestDir = new File(playPath, "workspace")
  val testDir = if (windowsTestDir.exists()) windowsTestDir else linuxTestDir
  val crisprDir = s"${playPath}/../perls/CRISPRCasFinder"
  val vmatchDir = s"${playPath}/../perls/vmatch-2.3.0-Linux_x86_64-64bit"
  val cp = if (isWindows) "xcopy" else "cp"

  def command2Wsl(command: String) = {
    if (isWindows) s"wsl ${command}" else command
  }

  def dosPath2Unix(file: File) = {
    val path = file.getAbsolutePath
    path.replace("\\", "/").replaceAll("D:", "/mnt/d").
      replaceAll("E:", "/mnt/e").replaceAll("C:", "/mnt/c")
  }


  def getAllFiles(file: File): ArrayBuffer[File] = {
    val files = ArrayBuffer[File]()

    def loop(file: File): ArrayBuffer[File] = {
      for (f <- file.listFiles()) {
        if (f.isDirectory) {
          loop(f)
        } else {
          files += f
        }
      }
      files
    }

    loop(file)
  }


  val scriptHtml =
    """
      |<script>
      |	$(function () {
      |			    $("footer:first").remove()
      |        $("#content").css("margin","0")
      |       $(".linkheader>a").each(function () {
      |				   var text=$(this).text()
      |				   $(this).replaceWith("<span style='color: #222222;'>"+text+"</span>")
      |			   })
      |
      |      $("tr").each(function () {
      |         var a=$(this).find("td>a:last")
      |					var text=a.text()
      |					a.replaceWith("<span style='color: #222222;'>"+text+"</span>")
      |				})
      |
      |       $("p.titleinfo>a").each(function () {
      |				   var text=$(this).text()
      |				   $(this).replaceWith("<span style='color: #606060;'>"+text+"</span>")
      |			   })
      |
      |       $(".param:eq(1)").parent().hide()
      |       $(".linkheader").hide()
      |
      |			})
      |</script>
    """.stripMargin

  val Rscript = {
    "Rscript"
  }

  val pyPath = {
    val path = "D:\\workspaceForIDEA\\p3bacter\\pyScripts"
    val linuxPyPath = linuxPath + "/pyScripts"
    if (new File(path).exists()) path else linuxPyPath
  }

  val goPy = {
    val path = "C:\\Python\\python.exe"
    if (new File(path).exists()) path else "python"
  }

  val pyScript =
    """
      |<script>
      |Plotly.Plots.resize(document.getElementById($('#charts').children().eq(0).attr("id")));
      |window.addEventListener("resize", function (ev) {
      |				Plotly.Plots.resize(document.getElementById($('#charts').children().eq(0).attr("id")));
      |					})
      |</script>
      |
    """.stripMargin

  val phylotreeCss =
    """
      |<style>
      |.tree-selection-brush .extent {
      |    fill-opacity: .05;
      |    stroke: #fff;
      |    shape-rendering: crispEdges;
      |}
      |
      |.tree-scale-bar text {
      |  font: sans-serif;
      |}
      |
      |.tree-scale-bar line,
      |.tree-scale-bar path {
      |  fill: none;
      |  stroke: #000;
      |  shape-rendering: crispEdges;
      |}
      |
      |.node circle, .node ellipse, .node rect {
      |fill: steelblue;
      |stroke: black;
      |stroke-width: 0.5px;
      |}
      |
      |.internal-node circle, .internal-node ellipse, .internal-node rect{
      |fill: #CCC;
      |stroke: black;
      |stroke-width: 0.5px;
      |}
      |
      |.node {
      |font: 10px sans-serif;
      |}
      |
      |.node-selected {
      |fill: #f00 !important;
      |}
      |
      |.node-collapsed circle, .node-collapsed ellipse, .node-collapsed rect{
      |fill: black !important;
      |}
      |
      |.node-tagged {
      |fill: #00f;
      |}
      |
      |.branch {
      |fill: none;
      |stroke: #999;
      |stroke-width: 2px;
      |}
      |
      |.clade {
      |fill: #1f77b4;
      |stroke: #444;
      |stroke-width: 2px;
      |opacity: 0.5;
      |}
      |
      |.branch-selected {
      |stroke: #f00 !important;
      |stroke-width: 3px;
      |}
      |
      |.branch-tagged {
      |stroke: #00f;
      |stroke-dasharray: 10,5;
      |stroke-width: 2px;
      |}
      |
      |.branch-tracer {
      |stroke: #bbb;
      |stroke-dasharray: 3,4;
      |stroke-width: 1px;
      |}
      |
      |
      |.branch-multiple {
      |stroke-dasharray: 5, 5, 1, 5;
      |stroke-width: 3px;
      |}
      |
      |.branch:hover {
      |stroke-width: 10px;
      |}
      |
      |.internal-node circle:hover, .internal-node ellipse:hover, .internal-node rect:hover {
      |fill: black;
      |stroke: #CCC;
      |}
      |
      |.tree-widget {
      |}
      |</style>
    """.stripMargin

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

  def xlsx2Txt(xlsxFile: File, txtFile: File) = {
    val lines = xlsx2Lines(xlsxFile)
    FileUtils.writeLines(txtFile, lines.asJava)
  }

  def removeXlsxBlankLine(file: File) = {
    val lines = Utils.xlsx2Lines(file)
    Utils.lines2Xlsx(lines, file)
  }

  def xlsx2Lines(xlsxFile: File) = {
    val is = new FileInputStream(xlsxFile.getAbsolutePath)
    val xssfWorkbook = new XSSFWorkbook(is)
    val xssfSheet = xssfWorkbook.getSheetAt(0)
    val lines = ArrayBuffer[String]()
    for (i <- 0 to xssfSheet.getLastRowNum) {
      val xssfRow = xssfSheet.getRow(i)
      val columns = ArrayBuffer[String]()
      val firstRow = xssfSheet.getRow(0)
      for (j <- 0 until firstRow.getLastCellNum) {
        val cell = xssfRow.getCell(j)
        var value = ""
        if (cell != null) {
          cell.getCellType match {
            case Cell.CELL_TYPE_STRING =>
              value = cell.getStringCellValue
            case Cell.CELL_TYPE_NUMERIC =>
              if (DateUtil.isCellDateFormatted(cell)) {
                val dateFormat = new SimpleDateFormat("yyyy/MM/dd")
                value = dateFormat.format(cell.getDateCellValue)
              } else {
                val doubleValue = cell.getNumericCellValue
                value = if (doubleValue == doubleValue.toInt) {
                  doubleValue.toInt.toString
                } else doubleValue.toString
              }
            case Cell.CELL_TYPE_BLANK =>
              value = ""
            case _ =>
              value = ""
          }
        }

        columns += value.trim
      }
      val line = columns.mkString("\t")
      lines += line
    }
    xssfWorkbook.close()
    lines.filter(StringUtils.isNotBlank(_))
  }

  def txt2Xlsx(txtFile: File, xlsxFile: File) = {
    val lines = FileUtils.readLines(txtFile).asScala
    lines2Xlsx(lines, xlsxFile)
  }

  def lines2Xlsx(lines: mutable.Buffer[String], xlsxFile: File) = {
    val outputWorkbook = new XSSFWorkbook()
    val outputSheet = outputWorkbook.createSheet("Sheet1")
    for (i <- 0 until lines.size) {
      val outputEachRow = outputSheet.createRow(i)
      val line = lines(i)
      val columns = line.split("\t")
      for (j <- 0 until columns.size) {
        var cell = outputEachRow.createCell(j)
        if (Utils.isDouble(columns(j))) {
          cell.setCellValue(columns(j).toDouble)
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

  def getDataJson(file: File) = {
    val lines = FileUtils.readLines(file).asScala
    val sampleNames = lines.head.split("\t").drop(1)
    val array = lines.drop(1).map { line =>
      val columns = line.split("\t")
      val map = mutable.Map[String, String]()
      map += ("geneId" -> columns(0))
      sampleNames.zip(columns.drop(1)).foreach { case (sampleName, data) =>
        map += (sampleName -> data)
      }
      map
    }
    Json.obj("array" -> array, "sampleNames" -> sampleNames)
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


  //  def pdf2png(tmpDir: File, fileName: String) = {
  //    val pdfFile = new File(tmpDir, fileName)
  //    val outFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".png"
  //    val outFile = new File(tmpDir, outFileName)
  //    val document = PDDocument.load(pdfFile)
  //    val renderer = new PDFRenderer(document)
  //    ImageIO.write(renderer.renderImage(0, 3), "png", outFile)
  //    document.close()
  //  }
  //
  def getInfoByFile(file: File) = {
    val lines = FileUtils.readLines(file).asScala
    val columnNames = lines.head.split("\t").drop(1)
    val array = lines.drop(1).map { line =>
      val columns = line.split("\t")
      val map = mutable.Map[String, String]()
      map += ("geneId" -> columns(0))
      columnNames.zip(columns.drop(1)).foreach { case (columnName, data) =>
        map += (columnName -> data)
      }
      map
    }
    (columnNames, array)
  }

  def checkFile(file: File): (Boolean, String) = {
    val buffer = FileUtils.readLines(file).asScala
    val headers = buffer.head.split("\t")
    var error = ""
    if (headers.size < 2) {
      error = "错误：文件列数小于2！"
      return (false, error)
    }
    val headersNoHead = headers.drop(1)
    val repeatElement = headersNoHead.diff(headersNoHead.distinct).distinct.headOption
    repeatElement match {
      case Some(x) => val nums = headers.zipWithIndex.filter(_._1 == x).map(_._2 + 1).mkString("(", "、", ")")
        error = "错误：样品名" + x + "在第" + nums + "列重复出现！"
        return (false, error)
      case None =>
    }

    val ids = buffer.drop(1).map(_.split("\t")(0))
    val repeatid = ids.diff(ids.distinct).distinct.headOption
    repeatid match {
      case Some(x) => val nums = ids.zipWithIndex.filter(_._1 == x).map(_._2 + 2).mkString("(", "、", ")")
        error = "错误：第一列:" + x + "在第" + nums + "行重复出现！"
        return (false, error)
      case None =>
    }

    val headerSize = headers.size
    for (i <- 1 until buffer.size) {
      val columns = buffer(i).split("\t")
      if (columns.size != headerSize) {
        error = "错误：数据文件第" + (i + 1) + "行列数不对！"
        return (false, error)
      }

    }

    for (i <- 1 until buffer.size) {
      val columns = buffer(i).split("\t")
      for (j <- 1 until columns.size) {
        val value = columns(j)
        if (!isDouble(value)) {
          error = "错误：数据文件第" + (i + 1) + "行第" + (j + 1) + "列不为数字！"
          return (false, error)
        }
      }
    }
    (true, error)
  }

  def isDouble(value: String): Boolean = {
    try {
      value.toDouble
    } catch {
      case _: Exception =>
        return false
    }
    true
  }

  def isInt(value: String): Boolean = {
    try {
      val double = value.toDouble
      double == double.toInt
    } catch {
      case _: Exception =>
        false
    }
  }

  def getBase64Str(imageFile: File): String = {
    val inputStream = new FileInputStream(imageFile)
    val bytes = IOUtils.toByteArray(inputStream)
    val bytes64 = Base64.encodeBase64(bytes)
    inputStream.close()
    new String(bytes64)
  }

}
