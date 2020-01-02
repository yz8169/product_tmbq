package controllers

import java.io.File

import dao.{AccountDao, KitDao, ModeDao, UserDao}
import javax.inject.Inject
import models.Tables.KitRow
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter
import tool.{FileTool, FormTool, Tool, WebTool}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import implicits.Implicits._
import utils.Utils

/**
 * Created by Administrator on 2019/7/2
 */
class KitController @Inject()(cc: ControllerComponents, formTool: FormTool,
                              kitDao: KitDao)(implicit val modeDao: ModeDao) extends AbstractController(cc) {

  def kitManageBefore = Action { implicit request =>
    Ok(views.html.admin.kitManage())
  }

  def kitNameCheck = Action.async { implicit request =>
    val data = formTool.nameForm.bindFromRequest.get
    kitDao.selectByName(data.name).map { xOp =>
      xOp match {
        case Some(y) => Ok(Json.obj("valid" -> false))
        case None =>
          Ok(Json.obj("valid" -> true))
      }
    }
  }

  def addKit = Action.async(parse.multipartFormData) { implicit request =>
    val data = formTool.nameForm.bindFromRequest().get
    val tmpDir = Tool.createTempDirectory("tmpDir")
    val myTmpDir = Tool.getAdminDataDir(tmpDir)
    val myMessage = FileTool.adminFileCheck(myTmpDir)
    if (myMessage.valid) {
      val row = KitRow(0, data.name, new DateTime())
      kitDao.insertAndReturnId(row).map { x =>
        val destFile = new File(Tool.kitDir, s"${x}.xlsx")
        myTmpDir.compoundConfigFile.copyTo(destFile)
        Ok(Json.obj("valid" -> myMessage.valid, "message" -> myMessage.message))
      }
    } else {
      Tool.deleteDirectory(myTmpDir.tmpDir)
      Future.successful(Ok(Json.obj("valid" -> myMessage.valid, "message" -> myMessage.message)))
    }
  }

  def getAllKit = Action.async { implicit request =>
    kitDao.selectAll.map { x =>
      val array = Utils.getArrayByTs(x)
      Ok(Json.toJson(array))
    }
  }

  def deleteKitById = Action.async { implicit request =>
    val data = formTool.idForm.bindFromRequest().get
    kitDao.deleteById(data.id).map { x =>
      Future {
        val kitFile = Tool.getKitFile(data.id)
        kitFile.deleteQuietly
      }
      Ok("success")
    }
  }

  def viewKitData = Action.async { implicit request =>
    val data = formTool.idForm.bindFromRequest().get
    kitDao.selectById(data.id).map { x =>
      val kitFile = Tool.getKitFile(data.id)
      Ok.sendFile(kitFile).withHeaders(
        CONTENT_DISPOSITION -> s"attachment; filename=${
          x.name
        }.xlsx"
      )
    }
  }


}
