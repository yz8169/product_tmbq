package myJs.myPkg

import jsext._

import scala.scalajs.js

/**
  * Created by yz on 2019/3/14
  */
trait Layer extends js.Object {

  def alert(element: String, options: LayerOptions):Int = js.native

  def close(index: Int):Unit = js.native

  def confirm(element: String, options: LayerOptions,yes:js.Function,cancel:js.Function):Int = js.native

  def msg(element: String, options: LayerOptions):Int = js.native

  def open(options: LayerOptions):Int = js.native


}

object LayerOptions extends LayerOptionsBuilder(noOpts)

class LayerOptionsBuilder(val dict: OptMap) extends JSOptionBuilder[LayerOptions, LayerOptionsBuilder](new LayerOptionsBuilder(_)) {

  def title(v: String) = jsOpt("title", v)

  def closeBtn(v: Int) = jsOpt("closeBtn", v)

  def skin(v: String) = jsOpt("skin", v)

  def btn[T](v: js.Array[T]) = jsOpt("btn", v)

  def icon(v: Int) = jsOpt("icon", v)

  def time(v: Int) = jsOpt("time", v)

  def `type`(v: Int) = jsOpt("type", v)

  def area(v: String) = jsOpt("area", v)

  def area(v: js.Array[String]) = jsOpt("area", v)

  def anim(v: Int) = jsOpt("anim", v)

  def shadeClose(v: Boolean) = jsOpt("shadeClose", v)

  def maxmin(v: Boolean) = jsOpt("maxmin", v)

  def content(v: String) = jsOpt("content", v)

}

trait LayerOptions extends js.Object {

}