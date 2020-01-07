package controllers

import dao.{AccountDao, ConfigDao, UserDao}
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter
import tool.{FormTool, Tool}
import utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Administrator on 2019/7/2
 */
class ConfigController @Inject()(cc: ControllerComponents, formTool: FormTool, accountDao: AccountDao,
                                 userDao: UserDao, configDao: ConfigDao) extends AbstractController(cc) {

  def missionSetBefore = Action { implicit request =>
    Ok(views.html.admin.missionSet())
  }

  def getThreadNum = Action.async { implicit request =>
    configDao.selectThreadNum.map { x =>
      Ok(Json.toJson(x.value))
    }
  }

  def getCpuNum = Action { implicit request =>
    val cpuNum = Tool.availCpu
    Ok(Json.toJson(cpuNum))
  }

  def updateThreadNum = Action.async { implicit request =>
    val data = formTool.missionSetForm.bindFromRequest().get
    configDao.updateThreadNum(data.threadNum).map { x =>
      Ok(Json.toJson("success!"))
    }
  }


}
