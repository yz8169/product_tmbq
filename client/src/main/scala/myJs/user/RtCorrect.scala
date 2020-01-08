package myJs.user

import myJs.Tool._
import myJs.Utils._
import myJs.implicits.Implicits._
import myJs.myPkg.Implicits._
import myJs.myPkg._
import myJs.myPkg.jquery._
import org.scalajs.dom.{FormData, document, _}
import org.scalajs.dom.raw.{HTMLFormElement, XMLHttpRequest}
import org.scalajs.jquery.jQuery
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation._

@JSExportTopLevel("RtCorrect")
object RtCorrect {

  @JSExport("init")
  def init = {
    refreshKit
    bootStrapValidator
    $("#form").bootstrapValidator("revalidateField", "missionName")

  }

  def refreshKit = {
    val url = g.jsRoutes.controllers.KitController.getAllKit().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?").contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      val rs = data.asInstanceOf[js.Array[js.Dictionary[String]]]
      val select2Data = rs.map { dict =>
        js.Dictionary("id" -> dict("id"), "text" -> dict("name"))
      }.toJSArray
      val options = Select2Options.dictData(select2Data)
      $(":input[name='kitId']").select2(options)
    }
    $.ajax(ajaxSettings)
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
      val url = g.jsRoutes.controllers.RtMissionController.rtCorrect().url.toString
      val xhr = new XMLHttpRequest
      xhr.open("post", url, async = true)
      xhr.upload.onprogress = progressHandlingFunction
      xhr.onreadystatechange = (e) => {
        if (xhr.readyState == XMLHttpRequest.DONE) {
          val data = xhr.response
          val rs = JSON.parse(data.toString).asInstanceOf[js.Dictionary[js.Any]]
          layer.close(index)
          val valid = rs("valid").asInstanceOf[Boolean]
          clearFile
          if (valid) {
            val url = s"${g.jsRoutes.controllers.RtMissionController.missionManageBefore().url.toString}"
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
    $(":input[name='std7File']").fileinput("clear")
    $(":input[name='std8File']").fileinput("clear")
    $("#form").bootstrapValidator("revalidateField", "compoundConfigFile")
    $("#form").bootstrapValidator("revalidateField", "std7File")
    $("#form").bootstrapValidator("revalidateField", "std8File")
  }

  def bootStrapValidator = {
    val url = g.jsRoutes.controllers.RtMissionController.missionNameCheck().url.toString
    val maxNumber = Double.MaxValue
    val dict = js.Dictionary(
      "feedbackIcons" -> js.Dictionary(
        "valid" -> "glyphicon glyphicon-ok",
        "invalid" -> "glyphicon glyphicon-remove",
        "validating" -> "glyphicon glyphicon-refresh",
      ),
      "fields" -> js.Dictionary(
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
        "std7File" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "STD7文件不能为空!"
            ),
            "file" -> js.Dictionary(
              "message" -> "STD7文件格式不正确!",
              "extension" -> "txt",
            ),
          )
        ),
        "std8File" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "STD8文件不能为空!"
            ),
            "file" -> js.Dictionary(
              "message" -> "STD8文件格式不正确!",
              "extension" -> "txt",
            ),
          )
        ),

      )
    )
    g.$("#form").bootstrapValidator(dict)

  }


}
