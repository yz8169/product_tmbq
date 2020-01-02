package tool

import cats.data.Validated
import cats.implicits._
import implicits.Implicits._
import org.apache.commons.lang3.StringUtils
import tool.Pojo.MyMessage

/**
 * Created by Administrator on 2019/12/20
 */
class SampleFileValidTool(lines: List[List[String]]) {

  val headers = lines.head.map(_.toLowerCase)
  val hasHeaders = List("batch", "file name", "sample type")
  val repeatColumns = List("file name")
  val factorMap = Map(
    "sample type" -> List("standard", "analyte")
  )
  val fileInfo = "sample info config file "

  def validHeadersRepeat = {
    val repeatHeaders = headers.diff(headers.distinct)
    val valid = repeatHeaders.nonEmpty
    Validated.cond(!valid, true, s"${fileInfo} header ${repeatHeaders.head} is repeated!")
  }

  def validHeadersSize = {
    val valid = headers.size < 4
    Validated.cond(!valid, true, s"The column number of ${fileInfo} must be greater than or equal four!")
  }

  def validHeadersExist = {
    val noExistHeaders = hasHeaders.diff(headers)
    val valid = noExistHeaders.isEmpty
    Validated.cond(valid, true, s"${fileInfo} header ${noExistHeaders.head} is not exist!")
  }

  def validColumnsRepeat = {
    val info = repeatColumns.map { header =>
      val totalColumns = lines.selectOneColumn(header)
      val repeatValues = totalColumns.diff(totalColumns.distinct)
      val inValid = repeatValues.isEmpty
      val inMessage = if (!inValid) {
        val repeatValue = repeatValues.head
        val j = headers.indexOf(header)
        val i = totalColumns.lastIndexOf(repeatValue)
        s"${fileInfo} value is repeated in ${i + 2}th row,${j + 1}th column!"
      } else ""
      (inValid, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validFactorColumn = {
    val factorHeaders = factorMap.keySet
    val info = factorHeaders.map { header =>
      val totalColumns = lines.selectOneColumn(header)
      val op = totalColumns.filter { column =>
        !factorMap(header).contains(column)
      }.headOption
      val inValid = op.isEmpty
      val inMessage = if (!inValid) {
        val value = op.get
        val j = headers.indexOf(header)
        val i = totalColumns.lastIndexOf(value)
        s"${fileInfo} value must be one of ${factorMap(header).mkString("、")} in ${i + 2}th row,${j + 1}th column!"
      } else ""
      (inValid, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validColumnNum = {
    val info = lines.drop(1).zipWithIndex.map { case (tmpColumns, i) =>
      val columns = tmpColumns
      val inValid = columns.size > headers.size
      val inMessage = if (inValid) {
        s"${fileInfo} column number is overmuch in ${i + 2}th row!"
      } else ""
      (!inValid, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validFileNameColumn(fileNames: List[String]) = {
    val header = "file name"
    val totalColumns = lines.selectOneColumn(header)
    val op = totalColumns.filter(column => !fileNames.contains(column)).headOption
    val inValid = op.isEmpty
    val inMessage = if (!inValid) {
      val value = op.get
      val j = headers.indexOf(header)
      val i = totalColumns.lastIndexOf(value)
      s"${fileInfo} value is not valid file name in ${i + 2}th row ${j + 1}th column!"
    } else ""
    Validated.cond(inValid, true, inMessage)
  }

  def validNonEmpty = {
    val info = lines.drop(1).zipWithIndex.map { case (columns, i) =>
      val lineMap = headers.zip(columns).toMap
      val op = hasHeaders.map(headers.indexOf(_)).map(columns(_)).filter { column =>
        StringUtils.isEmpty(column)
      }.headOption
      val inMessage = if (op.nonEmpty) {
        val j = columns.indexOf(op.get)
        s"${fileInfo} value is empty in ${i + 2}th row ${j + 1}th column!"
      } else ""
      (op.isEmpty, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validBatch = {
    case class BatchData(batch: String, kind: String)
    val batchs = lines.drop(1).map {
      columns =>
        val lineMap = headers.zip(columns).toMap
        BatchData(lineMap("batch"), lineMap("sample type"))
    }.groupBy(_.batch)
    val lineMap = lines.lineMap
    val header = "std"
    val info = batchs.map { case (batch, batchDatas) =>
      val standrads = batchDatas.filter(_.kind == "standard")
      val analytes = batchDatas.filter(_.kind == "analyte")
      val b1 = standrads.size < 2
      val b2 = analytes.size < 1
      val j = headers.indexOf(header)
      val inMessage = if (b1) {
        s"${fileInfo} batch ${batch} must have at least 2 standard samples!"
      } else if (b2) {
        s"${fileInfo} batch ${batch} must have at least 1 analyte sample!"
      } else ""
      (!b1 && !b2, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validRowNum = {
    val valid = lines.size <= 101
    val message = if (!valid) {
      s"Thank you for using. The academic version cannot process large dataset with more than 100 samples or compounds. Please contact us for more information!"
    } else ""
    Validated.cond(valid, true, message)
  }

}

object SampleFileValidTool {

  def valid(lines: List[List[String]], fileNames: List[String]) = {
    val sampleFileValidTool = new SampleFileValidTool(lines)
    import sampleFileValidTool._
    validHeadersSize.andThen { b =>
      validHeadersRepeat
    }.andThen { b =>
      validHeadersExist
    }.andThen { b =>
      validBatch
    }.andThen { b =>
      validColumnNum
    }.andThen { b =>
      validNonEmpty
    }.andThen { b =>
      validColumnsRepeat
    }.andThen { b =>
      validFactorColumn
    }.andThen { b =>
      validFileNameColumn(fileNames)
    }.andThen { b =>
      validRowNum
    }

  }

}
