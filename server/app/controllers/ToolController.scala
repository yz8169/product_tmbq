package controllers

import java.io.File

import javax.inject.Inject
import org.apache.commons.lang3.StringUtils
import org.zeroturnaround.zip.ZipUtil
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import tool.{FormTool, Tool}
import utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by yz on 2018/10/22
 */
class ToolController @Inject()(cc: ControllerComponents, formTool: FormTool) extends AbstractController(cc) {

  def downloadExampleData = Action {
    implicit request =>
      val data = formTool.fileNameForm.bindFromRequest().get
      val exampleDir = Tool.exampleDir
      val resultFile = new File(exampleDir, data.fileName)
      Ok.sendFile(resultFile).withHeaders(
        CACHE_CONTROL -> "max-age=3600",
        CONTENT_DISPOSITION -> s"attachment; filename=${
          resultFile.getName
        }",
        CONTENT_TYPE -> "application/x-download"
      )
  }

}
