package myJs.admin

import myJs.Tool
import myJs.Tool.{layerOptions, _}
import myJs.Utils.{g, layer}
import myJs.myPkg.Implicits._
import myJs.myPkg.bootstrap.Bootstrap.default._
import myJs.myPkg.jquery._
import myJs.myPkg.{FileInputOptions, LayerOptions, Swal, SwalOptions}
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import myJs.Utils._
import org.scalajs.dom.{FormData, XMLHttpRequest, document}
import org.scalajs.dom.raw.HTMLFormElement


/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("KitManage")
object KitManage {

  @JSExport("init")
  def init = {
    $("#table").bootstrapTable()
    refreshTable()
    bootStrapValidator

  }

  @JSExport("refreshUser")
  def refreshTable(f: () => js.Any = () => ()) = {
    val url = g.jsRoutes.controllers.KitController.getAllKit().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?").contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      $("#table").bootstrapTable("load", data)
      f()
    }
    $.ajax(ajaxSettings)

  }

  @JSExport("operateFmt")
  def operateFmt: js.Function = {
    (v: js.Any, row: js.Dictionary[js.Any]) =>
      val viewUrl = g.jsRoutes.controllers.KitController.viewKitData().url.toString
      val viewStr = a(
        title := "查看",
        href := s"${viewUrl}?id=${row("id")}",
        cursor.pointer,
        span(
          em(cls := "fa fa-eye")
        )
      )
      val deleteStr = a(
        title := "删除",
        cursor.pointer,
        onclick := s"KitManage.deleteData('" + row("id") + "')",
        target := "_blank",
        span(
          em(cls := "fa fa-close")
        )
      )
      Array(viewStr, deleteStr).mkString("&nbsp;")
  }

  @JSExport("add")
  def add = {
    val bv = jQuery("#form").data("bootstrapValidator")
    bv.validate()
    val valid = bv.isValid().asInstanceOf[Boolean]
    if (valid) {
      val formData = new FormData(document.getElementById("form").asInstanceOf[HTMLFormElement])
      val index = layer.alert(myElement, layerOptions)
      val url = g.jsRoutes.controllers.KitController.addKit().url.toString
      val xhr = new XMLHttpRequest
      xhr.open("post", url)
      xhr.upload.onprogress = progressHandlingFunction
      xhr.onreadystatechange = (e) => {
        if (xhr.readyState == XMLHttpRequest.DONE) {
          val data = xhr.response
          val rs = JSON.parse(data.toString).asInstanceOf[js.Dictionary[String]]
          if (rs("valid").asInstanceOf[Boolean]) {
            refreshTable { () =>
              layer.close(index)
              jQuery("#addModal").modal("hide")
              bv.resetForm(true)
              Swal.swal(SwalOptions.title("成功").text("新增成功!").`type`("success"))
            }
          } else {
            layer.close(index)
            layer.msg(rs("message"), LayerOptions.icon(5).time(5000))
          }
        }
      }
      xhr.send(formData)
    }

  }

  @JSExport("addShow")
  def addShow = {
    jQuery("#addModal").modal("show")
  }

  def bootStrapValidator = {
    val url = g.jsRoutes.controllers.KitController.kitNameCheck().url.toString
    val dict = js.Dictionary(
      "feedbackIcons" -> js.Dictionary(
        "valid" -> "glyphicon glyphicon-ok",
        "invalid" -> "glyphicon glyphicon-remove",
        "validating" -> "glyphicon glyphicon-refresh",
      ),
      "fields" -> js.Dictionary(
        "name" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "试剂盒名称不能为空！"
            ),
            "remote" -> js.Dictionary(
              "message" -> "试剂盒名称已存在！",
              "url" -> url,
              "delay" -> 1000,
              "type" -> "POST",
            ),
          )
        ),
        "compoundConfigFile" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "物质配置文件不能为空！"
            ),
            "file" -> js.Dictionary(
              "message" -> "物质配置文件格式不正确！",
              "extension" -> "xlsx",
            ),
          )
        ),
      )
    )
    g.$("#form").bootstrapValidator(dict)
  }

  @JSExport("deleteData")
  def deleteData(id: String) = {
    val options = SwalOptions.title("").text("确定要删除此数据吗？").`type`("warning").showCancelButton(true).
      showConfirmButton(true).confirmButtonClass("btn-danger").confirmButtonText("确定").closeOnConfirm(false).
      cancelButtonText("取消").showLoaderOnConfirm(true)
    Swal.swal(options, () => {
      val url = g.jsRoutes.controllers.KitController.deleteKitById().url.toString
      val ajaxSettings = JQueryAjaxSettings.url(s"${url}?id=${id}").
        `type`("get").contentType("application/json").success { (data: js.Any, status: String, e: JQueryXHR) =>
        refreshTable { () =>
          Swal.swal(SwalOptions.title("成功").text("删除成功").`type`("success"))
        }
      }.error { (data: JQueryXHR, status: String, e: String) =>
        Swal.swal(SwalOptions.title("错误").text("删除失败").`type`("error"))
      }
      $.ajax(ajaxSettings)

    })
  }


  def updateFormBootStrapValidator = {
    val url = g.jsRoutes.controllers.AdminController.userNameCheck().url.toString
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
      )
    )
    g.$("#updateForm").bootstrapValidator(dict)
  }


}
