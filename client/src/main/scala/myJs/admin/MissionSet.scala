package myJs.admin

import myJs.Tool.{layerOptions, _}
import myJs.Utils.{g, layer, _}
import myJs.myPkg.Implicits._
import myJs.myPkg.bootstrap.Bootstrap.default._
import myJs.myPkg.jquery._
import myJs.myPkg.{LayerOptions, Swal, SwalOptions}
import org.scalajs.dom.raw.HTMLFormElement
import org.scalajs.dom.{FormData, XMLHttpRequest, document}
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("MissionSet")
object MissionSet {

  @JSExport("init")
  def init = {
    refreshThreadNum()
    getCpuNum { cpuNum =>
      bootStrapValidator(cpuNum)
    }

  }

  def getCpuNum(f: Int => Unit) = {
    val url = g.jsRoutes.controllers.ConfigController.getCpuNum().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}").`type`("get").async(false).success { (data, status, e) =>
      val rs = data
      val cpuNum = rs.toString.toInt
      f(cpuNum)
    }
    $.ajax(ajaxSettings)
  }

  @JSExport("refreshThreadNum")
  def refreshThreadNum(f: () => js.Any = () => ()) = {
    val url = g.jsRoutes.controllers.ConfigController.getThreadNum().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?").contentType("application/json").
      `type`("get").success { (data: js.Any, status: String, e: JQueryXHR) =>
      $("#threadNum").text(data.toString)
      f()
    }
    $.ajax(ajaxSettings)

  }

  @JSExport("updateBefore")
  def updateBefore = {
    val formId = "updateForm"
    val url = g.jsRoutes.controllers.ConfigController.getThreadNum().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}").
      `type`("get").success { (data, status, e) =>
      $("#updateModal :input[name='originalThreadNum']").`val`(data.toString)
      jQuery("#updateModal").modal("show")
    }
    $.ajax(ajaxSettings)
  }

  @JSExport("update")
  def update = {
    val formId = "updateForm"
    val bv = jQuery(s"#${formId}").data("bootstrapValidator")
    bv.validate()
    val valid = bv.isValid().asInstanceOf[Boolean]
    if (valid) {
      val data = $(s"#${formId}").serialize()
      val index = layer.alert(myElement, layerOptions)
      val url = g.jsRoutes.controllers.ConfigController.updateThreadNum().url.toString
      val ajaxSettings = JQueryAjaxSettings.url(url).`type`("post").data(data).success { (data, status, e) =>
        refreshThreadNum { () =>
          layer.close(index)
          jQuery("#updateModal").modal("hide")
          Swal.swal(SwalOptions.title("成功").text("修改成功!").`type`("success"))
        }

      }
      $.ajax(ajaxSettings)
    }
  }

  def bootStrapValidator(cpuNum: Int) = {
    val dict = js.Dictionary(
      "feedbackIcons" -> js.Dictionary(
        "valid" -> "glyphicon glyphicon-ok",
        "invalid" -> "glyphicon glyphicon-remove",
        "validating" -> "glyphicon glyphicon-refresh",
      ),
      "fields" -> js.Dictionary(
        "threadNum" -> js.Dictionary(
          "validators" -> js.Dictionary(
            "notEmpty" -> js.Dictionary(
              "message" -> "线程数不能为空！"
            ),
            "integer" -> js.Dictionary(
              "message" -> "线程数必须为整数！",
            ),
            "between" -> js.Dictionary(
              "min" -> 1,
              "max" -> cpuNum,
              "message" -> s"线程数必须大于0且小于等于${cpuNum}(服务器可用最大cpu)！",
            ),

          )
        ),
      )
    )
    g.$("#updateForm").bootstrapValidator(dict)
  }


}
