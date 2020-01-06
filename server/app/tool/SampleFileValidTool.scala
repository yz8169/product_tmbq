package tool

import cats.data.Validated
import cats.data.Validated.Valid
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
  val fileInfo = "样品信息配置文件"

  def validHeadersRepeat = {
    val repeatHeaders = headers.diff(headers.distinct)
    val valid = repeatHeaders.nonEmpty
    Validated.cond(!valid, true, s"${fileInfo}表头 ${repeatHeaders.head} 重复!")
  }

  def validHeadersSize = {
    val valid = headers.size < 4
    Validated.cond(!valid, true, s"${fileInfo}列数必须大于等于4!")
  }

  def validHeadersExist = {
    val noExistHeaders = hasHeaders.diff(headers)
    val valid = noExistHeaders.isEmpty
    Validated.cond(valid, true, s"${fileInfo}表头 ${noExistHeaders.head} 不存在!")
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
        s"${fileInfo}第${i + 2}行第${j + 1}列重复!"
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
        s"${fileInfo}第${i + 2}行第${j + 1}列只能为(${factorMap(header).mkString("、")})中的一个!"
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
        s"${fileInfo}第${i + 2}行列数不正确,存在多余列!"
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
      s"${fileInfo}第${i + 2}行第${j + 1}列文件名不存在!"
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
        s"${fileInfo}第${i + 2}行第${j + 1}列为空!"
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
    val info = batchs.map { case (batch, batchDatas) =>
      val standrads = batchDatas.filter(_.kind == "standard")
      val analytes = batchDatas.filter(_.kind == "analyte")
      val b1 = standrads.size < 2
      val b2 = analytes.size < 1
      val inMessage = if (b1) {
        s"${fileInfo}batch${batch}标样不够(至少两个)!"
      } else if (b2) {
        s"${fileInfo}batch${batch}待测样不够(至少1个)!"
      } else ""
      (!b1 && !b2, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validBatchFileName(isRtCorrect: Boolean): Validated[String, Boolean] = {
    if (isRtCorrect) {
      validBatchFileName
    } else Valid(true)
  }

  def validBatchFileName: Validated[String, Boolean] = {
    val batchs = lines.drop(1).map {
      columns =>
        val lineMap = headers.zip(columns).toMap
        (lineMap("batch"), lineMap("file name"))
    }.groupMap(_._1)(_._2)
    val info = batchs.map { case (batch, fileNames) =>
      List("std_7", "std_8").forall(x => fileNames.toLowerCase.exists(y => y.endsWith(x)))
    }.toList
    val valid = info.exists(x => x)
    val messages = s"${fileInfo}所有batch file name列皆不存在std_7和std_8文件!"
    Validated.cond(valid, true, messages)
  }

}

object SampleFileValidTool {

  def valid(lines: List[List[String]], fileNames: List[String], isRtCorrect: Boolean) = {
    val sampleFileValidTool = new SampleFileValidTool(lines)
    import sampleFileValidTool._
    validHeadersSize.andThen { b =>
      validHeadersRepeat
    }.andThen { b =>
      validHeadersExist
    }.andThen { b =>
      validBatch
    }.andThen { b =>
      validBatchFileName(isRtCorrect)
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
    }

  }

}
