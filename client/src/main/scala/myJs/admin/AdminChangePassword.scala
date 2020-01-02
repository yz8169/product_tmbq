package myJs.admin

import myJs.Tool
import myJs.Tool.{layerOptions, _}
import myJs.Utils.{g, layer}
import myJs.myPkg.Implicits._
import myJs.myPkg.bootstrap.Bootstrap.default._
import myJs.myPkg.jquery._
import myJs.myPkg.{Swal, SwalOptions}
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("AdminChangePassword")
object AdminChangePassword {

  @JSExport("init")
  def init = {
    bootStrapValidator

  }

  def bootStrapValidator = {

    val dict = js.Dictionary(
      "feedbackIcons" -> js.Dictionary(
        "valid" -> "glyphicon glyphicon-ok",
        "invalid" -> "glyphicon glyphicon-remove",
        "validating" -> "glyphicon glyphicon-refresh",
      ),
      "fields" -> js.Dictionary(
        "password" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "密码不能为空！"
            ),
          )
        ),
        "newPassword" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "新密码不能为空！"
            ),
            "identical" -> js.Dictionary(
              "field" -> "newPasswordAgain",
              "message" -> "两次密码不一样！"
            ),
          )
        ),
        "newPasswordAgain" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "确认密码不能为空！"
            ),
            "identical" -> js.Dictionary(
              "field" -> "newPassword",
              "message" -> "两次密码不一样！"
            ),
          )
        ),
      )
    )
    g.$("#form").bootstrapValidator(dict)
  }

}
