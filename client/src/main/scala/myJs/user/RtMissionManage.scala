package myJs.user

import myJs.Tool
import myJs.Utils._
import myJs.myPkg.Implicits._
import myJs.myPkg.jquery._
import myJs.myPkg.{LayerOptions, Swal, SwalOptions}
import org.scalajs.dom._
import scalatags.Text.{TypedTag, _}
import scalatags.Text.all._

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


/**
 * Created by yz on 2019/4/25
 */
@JSExportTopLevel("RtMissionManage")
object RtMissionManage {

  @JSExport("init")
  def init = {

    $("#missionTable").bootstrapTable()
    refreshMission()
    updateMissionSocket

  }


  @JSExport("refreshMission")
  def refreshMission(f: () => Any = () => ()) = {
    val url = g.jsRoutes.controllers.RtMissionController.getAllMission().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?").contentType("application/json").
      `type`("get").success { (data, status, e) =>
      $("#missionTable").bootstrapTable("load", data)
      f()
    }
    $.ajax(ajaxSettings)

  }

  @JSExport("stateFmt")
  def stateFmt: js.Function = {
    (v: js.Any, row: js.Dictionary[js.Any]) =>
      val state1 = if (List("success").contains(row("state"))) {
        span(cls := "label label-success", "成功！")
      } else if (List("running").contains(row("state"))) {
        span("正在运行 ",
          img(cls := "runningImage", src := "/assets/images/running2.gif", width := 30, height := 20))
      } else if (List("wait", "preparing").contains(row("state"))) {
        span(
          span(cls := "label label-default", "排队中 "),
          all.raw("&nbsp;"),
          img(cls := "runningImage", src := "/assets/images/running2.gif", width := 30, height := 20))
      } else {
        span(cls := "label label-danger", "错误！")
      }

      state1.toString()

  }

  def updateMissionSocket = {
    val url = g.jsRoutes.controllers.RtMissionController.updateMissionSocket().url.toString
    val wsUri = s"ws://${window.location.host}${url}"
    webSocket(wsUri)
  }

  def webSocket(wsUri: String) = {
    val websocket = new WebSocket(wsUri)
    websocket.onopen = (evt) =>
      websocket.send(JSON.stringify(js.Dictionary("info" -> "start")))
    websocket.onclose = (evt) =>
      println(s"ERROR:${evt.code},${evt.reason},${evt.wasClean}")
    websocket.onmessage = (evt) => {
      val message = evt.data
      val data = JSON.parse(message.toString).asInstanceOf[js.Dictionary[String]]
      $("#missionTable").bootstrapTable("load", data)
    }
    websocket.onerror = (evt) => {
      updateByHand
      println(s"ERROR:${evt.toString}")
    }
  }

  def updateByHand = {
    js.timers.setInterval(3000) {
      refreshMission()
    }
  }


  @JSExport("deleteData")
  def deleteData(id: String) = {
    val options = SwalOptions.title("").text("确定要删除此数据吗？").`type`("warning").showCancelButton(true).
      showConfirmButton(true).confirmButtonClass("btn-danger").confirmButtonText("确定").closeOnConfirm(false).
      cancelButtonText("取消").showLoaderOnConfirm(true)
    Swal.swal(options, () => {
      val url = g.jsRoutes.controllers.RtMissionController.deleteMissionById().url.toString
      val ajaxSettings = JQueryAjaxSettings.url(s"${url}?missionId=${id}").
        `type`("get").contentType("application/json").success { (data: js.Any, status: String, e: JQueryXHR) =>
        refreshMission { () =>
          Swal.swal(SwalOptions.title("成功").text("删除成功").`type`("success"))
        }
      }.error { (data: JQueryXHR, status: String, e: String) =>
        Swal.swal(SwalOptions.title("错误").text("删除失败").`type`("error"))
      }
      $.ajax(ajaxSettings)

    })
  }


  @JSExport("operateFmt")
  def operateFmt: js.Function = {
    (v: js.Any, row: js.Dictionary[js.Any]) =>
      val downloadUrl = g.jsRoutes.controllers.RtMissionController.downloadResult().url.toString
      val downloadStr = a(title := "下载结果", href := s"${downloadUrl}?missionId=${row("id")}", cursor.pointer,
        span(em(cls := "fa fa-download"))
      )

      val downloadDataUrl = g.jsRoutes.controllers.RtMissionController.downloadData().url.toString
      val downloadDataStr = a(title := "下载原始数据", href := s"${downloadDataUrl}?missionId=${row("id")}", cursor.pointer,
        span(em(cls := "fa fa-cloud-download"))
      )

      val deleteStr = a(
        title := "删除",
        cursor.pointer,
        onclick := s"RtMissionManage.deleteData('" + row("id") + "')",
        target := "_blank",
        span(
          em(cls := "fa fa-close")
        )
      )

      val viewStr = a(title := "日志", onclick := s"RtMissionManage.viewLog('${row("id")}')", cursor.pointer,
        span(em(cls := "fa fa-file-text"))
      )

      val state1 = if (List("success").contains(row("state"))) {
        List(downloadStr, downloadDataStr)
      } else List[TypedTag[String]]()
      val state2 = if (!List("running").contains(row("state"))) {
        List(viewStr)
      } else List[TypedTag[String]]()
      val rs = state1 ::: state2 ::: List(deleteStr)
      rs.mkString("&nbsp;")

  }

  @JSExport("viewLog")
  def viewLog(id: String) = {
    val url = g.jsRoutes.controllers.RtMissionController.getLogContent().url.toString
    val ajaxSettings = JQueryAjaxSettings.url(s"${url}?missionId=${id}").
      `type`("get").contentType("application/json").success { (data: js.Any, status: String, e: JQueryXHR) =>
      val options = LayerOptions.`type`(1).title("<h4>运行信息</h4>").area(js.Array("900px", "600px")).
        skin("layui-layer-demo").closeBtn(1).anim(2).shadeClose(true).maxmin(true).
        content(s"<pre style='word-wrap: break-word' class='genome-pre'>${data}</pre>")
      layer.open(options)
    }.error { (data: JQueryXHR, status: String, e: String) =>
      Swal.swal(SwalOptions.title("错误").text("失败").`type`("error"))
    }
    $.ajax(ajaxSettings)

  }

}
