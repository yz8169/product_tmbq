package controllers

import dao.{AccountDao, UserDao}
import javax.inject.Inject
import play.api.mvc.{AbstractController, ControllerComponents}
import play.api.routing.JavaScriptReverseRouter
import tool.FormTool
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Administrator on 2019/7/2
 */
class AppController @Inject()(cc: ControllerComponents, formTool: FormTool, accountDao: AccountDao,
                              userDao: UserDao) extends AbstractController(cc) {

  def loginBefore = Action { implicit request =>
    Ok(views.html.login())
  }

  def login = Action.async { implicit request =>
    val data = formTool.userForm.bindFromRequest().get
    accountDao.selectById1.zip(userDao.selectByUserData(data)).map { case (account, optionUser) =>
      if (data.name == account.account && data.password == account.password) {
        Redirect(routes.AdminController.userManageBefore()).addingToSession("admin" -> data.name)
      } else if (optionUser.isDefined) {
        val user = optionUser.get
        Redirect(routes.UserController.missionManageBefore()).addingToSession("user" -> data.name,
          "id" -> user.id.toString)
      } else {
        Redirect(routes.AppController.loginBefore()).flashing("info" -> "用户名或密码错误!")
      }
    }
  }


  def javascriptRoutes = Action { implicit request =>
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
        controllers.routes.javascript.AdminController.getAllUser,
        controllers.routes.javascript.AdminController.userNameCheck,
        controllers.routes.javascript.AdminController.deleteUserById,
        controllers.routes.javascript.AdminController.addUser,
        controllers.routes.javascript.AdminController.getUserById,
        controllers.routes.javascript.AdminController.updateUser,


      )
    ).as("text/javascript")

  }


}
