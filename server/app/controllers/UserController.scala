package controllers

import dao.{AccountDao, UserDao}
import javax.inject.Inject
import models.Tables.UserRow
import play.api.mvc._
import tool.FormTool

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by yz on 2018/7/17
  */
class UserController @Inject()(cc: ControllerComponents, formTool: FormTool, userDao: UserDao,
                               accountDao: AccountDao) extends AbstractController(cc) {

  def missionManageBefore = Action { implicit request =>
    Ok(views.html.user.missionManage())
  }

  def logout = Action { implicit request =>
    Redirect(routes.AppController.loginBefore()).flashing("info" -> "退出登录成功!").removingFromSession("user")
  }


}
