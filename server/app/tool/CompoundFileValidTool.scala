package tool

import org.apache.commons.lang3.StringUtils
import implicits.Implicits._
import cats.data.Validated
import cats.implicits._

/**
 * Created by Administrator on 2019/12/20
 */
class CompoundFileValidTool(lines: List[List[String]]) {

  val headers = lines.head.map(_.toLowerCase)
  val hasHeaders = List("index", "compound", "function", "mass", "rt", "rtlw", "rtrw", "peak_location",
    "response", "is_correction", "std", "polynomial_type", "origin", "ws4pp", "i4pp", "rs4rs", "mp4rs", "snr4pp",
    "lod", "loq", "nups4pp", "ndowns4pp", "ws4pa", "lp4e", "rp4e", "mp4e", "bline", "rmode", "rmis", "rmratio")
  val intHeaders = List("ws4pp", "i4pp", "mp4rs", "snr4pp", "nups4pp", "ndowns4pp", "lp4e", "rp4e", "mp4e",
    "function")
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
    val repeatColumns = List("index", "compound")
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

  def validOddColumn = {
    val oddColumns = List("ws4pp", "ws4pa")
    val info = oddColumns.map { header =>
      val totalColumns = lines.selectOneColumn(header)
      val op = totalColumns.filter { column =>
        !column.isInt || column.toInt < 0 || column.toInt % 2 == 0
      }.headOption
      val inValid = op.isEmpty
      val inMessage = if (!inValid) {
        val value = op.get
        val j = headers.indexOf(header)
        val i = totalColumns.lastIndexOf(value)
        s"${fileInfo}第${i + 2}行第${j + 1}列必须为奇数!"
      } else ""
      (inValid, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validIntColumn = {
    val info = intHeaders.map { header =>
      val totalColumns = lines.selectOneColumn(header)
      val op = totalColumns.filter { column =>
        !column.isInt || column.toInt < 0
      }.headOption
      val inValid = op.isEmpty
      val inMessage = if (!inValid) {
        val value = op.get
        val j = headers.indexOf(header)
        val i = totalColumns.lastIndexOf(value)
        s"${fileInfo}第${i + 2}行第${j + 1}列必须为自然数!"
      } else ""
      (inValid, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validDoubleColumn = {
    val doubleColumns = List("rt", "rtlw", "rtrw", "lod", "loq")
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

  def validFactorColumn = {
    val factorMap = Map("peak_location" -> List("nearest", "largest", "first", "all"),
      "response" -> List("height", "area"),
      "polynomial_type" -> List("linear", "quadratic"),
      "origin" -> List("exclude", "include"),
      "bline" -> List("yes", "no"),
      "rmode" -> List("yes", "no"))

    val columns = factorMap.keySet
    val info = columns.map { header =>
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

  def validMp4rsMoreMp4e = {
    val info = lines.drop(1).zipWithIndex.map { case (tmpColumns, i) =>
      val columns = tmpColumns.padTo(headers.size, "")
      val lineMap = headers.zip(columns).toMap
      val inValid = lineMap("mp4rs").toDouble > lineMap("mp4e").toDouble
      val inMessage = if (inValid) {
        s"${fileInfo}第${i + 2}行mp4e必须大于等于mp4rs!"
      } else ""
      (!inValid, inMessage)
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

  def validMassColumn = {
    val header = "mass"
    val totalColumns = lines.selectOneColumn(header)
    val op = totalColumns.filter { column =>
      val values = column.split(">")
      !values.forall(_.isDouble)
    }.headOption
    val inValid = op.isEmpty
    val inMessage = if (!inValid) {
      val value = op.get
      val j = headers.indexOf(header)
      val i = totalColumns.lastIndexOf(value)
      s"${fileInfo}第${i + 2}行第${j + 1}列必须为实数或以'>'分隔的两个实数!"
    } else ""
    Validated.cond(inValid, true, inMessage)
  }

  def validIsCorrectionColumn = {
    val indexs = lines.drop(1).map {
      columns =>
        val lineMap = headers.zip(columns).toMap
        lineMap("index")
    }.filter(_.startsWith("is"))
    val header = "is_correction"
    val totalColumns = lines.selectOneColumn(header)
    val op = totalColumns.filter { column =>
      !(column == "none" || indexs.contains(column))
    }.headOption
    val inValid = op.isEmpty
    val inMessage = if (!inValid) {
      val value = op.get
      val j = headers.indexOf(header)
      val i = totalColumns.lastIndexOf(value)
      s"""${fileInfo}第${i + 2}行第${j + 1}列必须为none或者某个存在的内标化合物的index列名称!"""
    } else ""
    Validated.cond(inValid, true, inMessage)
  }

  def validRs4rsColumn = {
    val header = "rs4rs"
    val totalColumns = lines.selectOneColumn(header)
    val op = totalColumns.filter { column =>
      !column.isDouble || column.toDouble > 1 || column.toDouble < 0
    }.headOption
    val inValid = op.isEmpty
    val inMessage = if (!inValid) {
      val value = op.get
      val j = headers.indexOf(header)
      val i = totalColumns.lastIndexOf(value)
      s"${fileInfo}第${i + 2}行第${j + 1}列必须为0-1之间的实数!"
    } else ""
    Validated.cond(inValid, true, inMessage)
  }

  def validRmisColumn = {
    val lineMap = lines.lineMap
    val header = "rmis"
    val indexs = lines.drop(1).map {
      columns =>
        val lineMap = headers.zip(columns).toMap
        lineMap("index")
    }.filter(_.startsWith("is"))
    val info = lines.drop(1).zipWithIndex.map { case (columns, i) =>
      val lineMap = headers.zip(columns).toMap
      val column = lineMap(header)
      val b = lineMap("rmode") == "yes" && !(indexs.contains(column))
      val inMessage = if (b) {
        val j = headers.indexOf(header)
        s"${fileInfo}第${i + 2}行第${j + 1}列必须为某个存在的内标化合物的index列名称!"
      } else ""
      (!b, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
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

  def validRmratioColumn = {
    val lineMap = lines.lineMap
    val header = "rmratio"
    val info = lines.drop(1).zipWithIndex.map { case (columns, i) =>
      val lineMap = headers.zip(columns).toMap
      val column = lineMap(header)
      val b = lineMap("rmode") == "yes" && !(column == "none" || column.isDouble)
      val inMessage = if (b) {
        val j = headers.indexOf(header)
        s"${fileInfo}第${i + 2}行第${j + 1}列必须为实数!"
      } else ""
      (!b, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

  def validStdColumn(sampleHeaders: List[String]) = {
    val lineMap = lines.lineMap
    val header = "std"
    val info = lines.drop(1).zipWithIndex.map { case (columns, i) =>
      val lineMap = headers.zip(columns).toMap
      val column = lineMap(header)
      val b1 = !lineMap("index").startsWith("is") && !sampleHeaders.contains(column)
      val b2 = (lineMap("index").startsWith("is") && !column.isDouble)
      val j = headers.indexOf(header)
      val inMessage = if (b1) {
        s"${fileInfo}第${i + 2}行第${j + 1}列浓度信息在样品信息配置表中不存在!"
      } else if (b2) {
        s"${fileInfo}第${i + 2}行第${j + 1}列，因为此样品为内标化合物，所以必须为实数!"
      } else ""
      (!b1 && !b2, inMessage)
    }
    val valid = info.forall(_._1)
    val messages = info.map(_._2)
    Validated.cond(valid, true, messages.head)
  }

}

object CompoundFileValidTool {

  def valid(lines: List[List[String]], sampleHeaders: List[String]) = {
    val compoundFileValidTool = new CompoundFileValidTool(lines)
    import compoundFileValidTool._
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
      validIntColumn
    }.andThen { b =>
      validMassColumn
    }.andThen { b =>
      validDoubleColumn
    }.andThen { b =>
      validFactorColumn
    }.andThen { b =>
      validOddColumn
    }.andThen { b =>
      validRmratioColumn
    }.andThen { b =>
      validIsCorrectionColumn
    }.andThen { b =>
      validRmisColumn
    }.andThen { b =>
      validStdColumn(sampleHeaders)
    }.andThen { b =>
      validRs4rsColumn
    }.andThen { b =>
      validMp4rsMoreMp4e
    }

  }

  def adminValid(lines: List[List[String]]) = {
    val compoundFileValidTool = new CompoundFileValidTool(lines)
    import compoundFileValidTool._
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
      validIntColumn
    }.andThen { b =>
      validMassColumn
    }.andThen { b =>
      validDoubleColumn
    }.andThen { b =>
      validFactorColumn
    }.andThen { b =>
      validOddColumn
    }.andThen { b =>
      validRmratioColumn
    }.andThen { b =>
      validIsCorrectionColumn
    }.andThen { b =>
      validRmisColumn
    }.andThen { b =>
      validRs4rsColumn
    }.andThen { b =>
      validMp4rsMoreMp4e
    }

  }

}
