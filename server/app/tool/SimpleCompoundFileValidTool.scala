package tool

import cats.data.Validated
import implicits.Implicits._
import org.apache.commons.lang3.StringUtils

/**
 * Created by Administrator on 2019/12/20
 */
class SimpleCompoundFileValidTool(lines: List[List[String]]) {

  val headers = lines.head.map(_.toLowerCase)
  val hasHeaders = List("compound", "rt")
  val fileInfo = "物质信息配置文件"

  def validHeadersRepeat = {
    val repeatHeaders = headers.diff(headers.distinct)
    val valid = repeatHeaders.isEmpty
    Validated.cond(valid, true, s"${fileInfo}表头 ${repeatHeaders.head} 重复!")
  }

  def validHeadersExist = {
    val noExistHeaders = hasHeaders.diff(headers)
    val valid = noExistHeaders.isEmpty
    Validated.cond(valid, true, s"${fileInfo}表头 ${noExistHeaders.head} 不存在!")
  }

  def validColumnsRepeat = {
    val repeatColumns = List("compound")
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

  def validDoubleColumn = {
    val doubleColumns = List("rt")
    val info = doubleColumns.map { header =>
      val totalColumns = lines.selectOneColumn(header)
      val op = totalColumns.filter { column =>
        !column.isDouble
      }.headOption
      val inValid = op.isEmpty
      val inMessage = if (!inValid) {
        val value = op.get
        val j = headers.indexOf(header)
        val i = totalColumns.lastIndexOf(value)
        s"${fileInfo}第${i + 2}行第${j + 1}列必须为实数!"
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
      val inValid = columns.size <= headers.size
      val inMessage = if (!inValid) {
        s"${fileInfo}第${i + 2}行列数不正确,存在多余列!"
      } else ""
      (inValid, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validCompoundColumn = {
    val header = "compound"
    val totalColumns = lines.selectOneColumn(header)
    val ILLEGAL_CHARACTERS = Array('/', '\n', '\r', '\t', '\u0000', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
    val op = totalColumns.filter(x => x.exists(ILLEGAL_CHARACTERS.contains(_))).headOption
    val inValid = op.isEmpty
    val inMessage = if (!inValid) {
      val value = op.get
      val j = headers.indexOf(header)
      val i = totalColumns.lastIndexOf(value)
      s"${fileInfo}第${i + 2}行第${j + 1}列出现特殊字符!"
    } else ""
    Validated.cond(inValid, true, inMessage)
  }

  def validCompoundColumnSame(dbCompounds: Set[String]) = {
    val header = "compound"
    val totalColumns = lines.selectOneColumn(header).toSet
    val inValid = totalColumns == dbCompounds
    val inMessage = if (!inValid) {
      s"${fileInfo}的化合物与选中试剂盒中的化合物不一致!"
    } else ""
    Validated.cond(inValid, true, inMessage)
  }

  def validNonEmpty = {
    val info = lines.drop(1).zipWithIndex.map { case (columns, i) =>
      val lineMap = headers.zip(columns).toMap
      val op = columns.filter { column =>
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

}

object SimpleCompoundFileValidTool {

  def valid(lines: List[List[String]], sampleHeaders: List[String], dbCompounds: Set[String]) = {
    val fileValidTool = new SimpleCompoundFileValidTool(lines)
    import fileValidTool._
    validHeadersRepeat.andThen { b =>
      validHeadersExist
    }.andThen { b =>
      validHeadersExist
    }.andThen { b =>
      validColumnNum
    }.andThen { b =>
      validNonEmpty
    }.andThen { b =>
      validColumnsRepeat
    }.andThen { b =>
      validCompoundColumn
    }.andThen { b =>
      validCompoundColumnSame(dbCompounds)
    }.andThen { b =>
      validDoubleColumn
    }

  }

  def valid(lines: List[List[String]], dbCompounds: Set[String]) = {
    val fileValidTool = new SimpleCompoundFileValidTool(lines)
    import fileValidTool._
    validHeadersRepeat.andThen { b =>
      validHeadersExist
    }.andThen { b =>
      validHeadersExist
    }.andThen { b =>
      validColumnNum
    }.andThen { b =>
      validNonEmpty
    }.andThen { b =>
      validColumnsRepeat
    }.andThen { b =>
      validCompoundColumn
    }.andThen { b =>
      validCompoundColumnSame(dbCompounds)
    }.andThen { b =>
      validDoubleColumn
    }

  }

}
