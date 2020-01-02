package tool

import java.io.File
import java.lang.reflect.Field
import java.net.URLEncoder

import org.apache.commons.io.FileUtils
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.Json
import play.api.mvc.{MultipartFormData, Request, RequestHeader}
import tool.Pojo.MyDataDir
import utils.Utils
import utils.Utils.{getArrayByTs, getValue}

import scala.collection.JavaConverters._

/**
 * Created by Administrator on 2019/12/6
 */
object WebTool {

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

  def getDataJson(file: File) = {
    val lines = FileUtils.readLines(file).asScala
    val sampleNames = lines.head.split("\t").drop(1)
    val array = lines.drop(1).map { line =>
      val columns = line.split("\t")
      val map = Map(("geneId" -> columns(0)))
      val otherMap = sampleNames.zip(columns.drop(1)).map { case (sampleName, data) =>
        (sampleName -> data)
      }.toMap
      map ++ otherMap
    }
    Json.obj("array" -> array, "sampleNames" -> sampleNames)
  }

  def fileMoveDirOp(fieldName: String, dir: File)(implicit request: Request[MultipartFormData[TemporaryFile]]) = {
    val tempFile = request.body.file(fieldName).get
    if (tempFile.ref.path.toFile.length() > 0) {
      val destFile = new File(dir, tempFile.filename)
      tempFile.ref.moveTo(destFile, replace = true)
      Some(destFile)
    } else None
  }

  def fileMove(fieldName: String, destFile: File)(implicit request: Request[MultipartFormData[TemporaryFile]]) = {
    val tempFile = request.body.file(fieldName).get
    tempFile.ref.moveFileTo(destFile, replace = true)
    Some(destFile)
  }

  def fileMoveDir(fieldName: String, dir: File)(implicit request: Request[MultipartFormData[TemporaryFile]]) = {
    fileMoveDirOp(fieldName, dir).get
  }

  def getContentDisposition(url: String) = {
    val encodeUrl = urlEncode(url)
    s"attachment; filename*=utf-8''${encodeUrl}"
  }

  def urlEncode(url: String) = {
    URLEncoder.encode(url, "UTF-8")
  }


}
