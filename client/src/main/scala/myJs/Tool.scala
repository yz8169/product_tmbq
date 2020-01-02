package myJs

import myJs.myPkg.{FileInputOptions, LayerOptions}

import scala.scalajs.js
import scalatags.Text.all._
import myPkg.jquery._

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import myJs.myPkg.Implicits._

/**
  * Created by yz on 2019/3/13
  */
@JSExportTopLevel("Tool")
object Tool {

  val zhInfo="信息"
  val layerOptions = LayerOptions.title(zhInfo).closeBtn(0).skin("layui-layer-molv").btn(js.Array())

  val zhRunning="正在运行"
  val element = div(id:="content",
    span(id:="info",zhRunning,
    span(id := "progress", "")), " ",
    img(src := "/assets/images/running2.gif", width := 30, height := 20,cls:="runningImage")
  ).render

  def element(info:String)={
    div(id:="content",
      span(id:="info",info,
        span(id := "progress", "")), " ",
      img(src := "/assets/images/running2.gif", width := 30, height := 20,cls:="runningImage")
    ).render
  }

  val loadingElement=element("加载数据")

  def emptyfy(value:String)={
    if(value=="") "" else value
  }

  val pattern="yyyy-mm-dd"

  def fillByName(rs: js.Dictionary[String], name: String) = {
    val valOp = rs.get(name)
    valOp.foreach { value =>
      $(s":input[name='${name}']").`val`(value)
    }
  }




  def fillByNames(rs: js.Dictionary[String], names: Seq[String]) = {
    names.foreach { name =>
      fillByName(rs, name)
    }
  }

  def fillByNames(rs: js.Dictionary[String]) = {
    val names = $(".fillByName").mapElems { y =>
      $(y).attr("name").toString
    }.toArray
    names.foreach { name =>
      fillByName(rs, name)
    }
  }

  def fillByNames(rs: js.Dictionary[String], formId: String) = {
    val names = $(s"#${formId} .fillByName").mapElems { y =>
      $(y).attr("name").toString
    }.toArray
    names.foreach { name =>
      fillByName(rs, name, formId)
    }
  }

  def fillByName(rs: js.Dictionary[String], name: String, formId: String) = {
    val value = rs(name)
    $(s"#${formId} :input[name='${name}']").`val`(value)
  }

  val myElement = div(id := "content")(
    span(id := "info")(zhRunning),
    " ",
    img(src := "/assets/images/running2.gif", width := 30, height := 20, cls := "runningImage")
  ).render

  @JSExport("fileInput")
  def fileInput = {
    val options = FileInputOptions.showPreview(false).browseLabel("选择...").removeLabel("删除文件").language("zh")
    $(".file").fileinput(options)
  }



}
