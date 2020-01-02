package myJs.user

import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.annotation._
import myJs.Utils._
import myJs.myPkg._
import myJs.myPkg.Implicits._
import org.scalajs.dom.FormData
import org.scalajs.dom.document
import org.scalajs.dom.raw.{HTMLFormElement, XMLHttpRequest}

import scala.scalajs.js.JSON
import scalajs.js.JSConverters._
import org.scalajs.dom._
import org.scalajs.jquery.jQuery
import org.scalajs.jquery.JQueryAjaxSettings
import myJs.myPkg.jquery._
import myJs.implicits.Implicits._
import myJs.Tool._

@JSExportTopLevel("NewMission")
object NewMission {

  @JSExport("init")
  def init = {

    bootStrapValidator
    $("#form").bootstrapValidator("revalidateField", "missionName")

  }

  @JSExport("myRun")
  def myRun = {
    val bv = jQuery("#form").data("bootstrapValidator")
    bv.validate()
    val valid = bv.isValid().asInstanceOf[Boolean]
    if (valid) {
      val formData = new FormData(document.getElementById("form").asInstanceOf[HTMLFormElement])
      jQuery(":disabled").attr("disabled", false)
      val element = div(id := "content",
        span(id := "info", "文件上传中",
          span(id := "progress", "。。。")), " ",
        img(src := "/assets/images/running2.gif", cls := "runningImage")
      ).render
      val layerOptions = LayerOptions.title(zhInfo).closeBtn(0).skin("layui-layer-molv").btn(js.Array())
      val index = layer.alert(element, layerOptions)
      val url = g.jsRoutes.controllers.MissionController.newMission().url.toString
      val xhr = new XMLHttpRequest
      xhr.open("post", url, async = true)
      xhr.upload.onprogress = progressHandlingFunction
      xhr.onreadystatechange = (e) => {
        if (xhr.readyState == XMLHttpRequest.DONE) {
          val data = xhr.response
          val rs = JSON.parse(data.toString).asInstanceOf[js.Dictionary[js.Any]]
          layer.close(index)
          val valid = rs("valid").asInstanceOf[Boolean]
          if (valid) {
            clearFile
            val url = s"${g.jsRoutes.controllers.UserController.missionManageBefore().url.toString}"
            window.redirect(url)
          } else {
            g.swal("Error", rs.myGet("message"), "error")
          }
        }
      }
      xhr.send(formData)
    }
  }

  def clearFile = {
    $(":input[name='compoundConfigFile']").fileinput("clear")
    $(":input[name='sampleConfigFile']").fileinput("clear")
    $(":input[name='dataFile']").fileinput("clear")
    $("#form").bootstrapValidator("revalidateField", "compoundConfigFile")
    $("#form").bootstrapValidator("revalidateField", "sampleConfigFile")
    $("#form").bootstrapValidator("revalidateField", "dataFile")
  }

  def bootStrapValidator = {
    val url = g.jsRoutes.controllers.MissionController.missionNameCheck().url.toString
    val maxNumber = Double.MaxValue
    val dict = js.Dictionary(
      "feedbackIcons" -> js.Dictionary(
        "valid" -> "glyphicon glyphicon-ok",
        "invalid" -> "glyphicon glyphicon-remove",
        "validating" -> "glyphicon glyphicon-refresh",
      ),
      "fields" -> js.Dictionary(
        "dataFile" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "数据文件不能为空!"
            ),
            "file" -> js.Dictionary(
              "message" -> "数据文件格式不正确!",
              "extension" -> "zip",
            ),
          )
        ),
        "missionName" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "任务名不能为空!"
            ),
            "remote" -> js.Dictionary(
              "message" -> "任务名已存在！",
              "url" -> url,
              "delay" -> 1000,
              "type" -> "POST",
            ),
          )
        ),
        "sampleConfigFile" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "样品配置文件不能为空!"
            ),
            "file" -> js.Dictionary(
              "message" -> "样品配置文件格式不正确!",
              "extension" -> "xlsx",
            ),
          )
        ),
        "compoundConfigFile" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "物质配置文件不能为空!"
            ),
            "file" -> js.Dictionary(
              "message" -> "物质配置文件格式不正确!",
              "extension" -> "xlsx",
            ),
          )
        ),

      )
    )
    g.$("#form").bootstrapValidator(dict)

  }


}
