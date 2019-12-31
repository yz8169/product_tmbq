package controllers

import dao.{AccountDao, UserDao}
import javax.inject.Inject
import models.Tables._
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc._
import tool.{FormTool, Tool}
import utils.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by yz on 2018/7/17
  */
class AdminController @Inject()(cc: ControllerComponents, formTool: FormTool, accountDao: AccountDao,
                                userDao: UserDao,tool:Tool) extends AbstractController(cc) {

  def userManageBefore = Action { implicit request =>
    Ok(views.html.admin.userManage())
  }

}
