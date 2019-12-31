package myJs.myPkg

import jsext._

import scala.scalajs.js
import myJs.Utils._

/**
 * Created by yz on 2019/3/14
 */
package object Swal {

  def swal(options: SwalOptions) = g.swal(options)

  def swal(options: SwalOptions, f: js.Function) = g.swal(options, f)

}

object SwalOptions extends SwalOptionsBuilder(noOpts)

class SwalOptionsBuilder(val dict: OptMap) extends JSOptionBuilder[SwalOptions, SwalOptionsBuilder](new SwalOptionsBuilder(_)) {

  def text(v: js.Any) = jsOpt("text", v)

  def title(v: String) = jsOpt("title", v)

  def `type`(v: String) = jsOpt("type", v)

  def showCancelButton(v: Boolean) = jsOpt("showCancelButton", v)

  def showConfirmButton(v: Boolean) = jsOpt("showConfirmButton", v)

  def confirmButtonClass(v: String) = jsOpt("confirmButtonClass", v)

  def confirmButtonText(v: String) = jsOpt("confirmButtonText", v)

  def closeOnConfirm(v: Boolean) = jsOpt("closeOnConfirm", v)

  def cancelButtonText(v: String) = jsOpt("cancelButtonText", v)

  def showLoaderOnConfirm(v: Boolean) = jsOpt("showLoaderOnConfirm", v)

}

trait SwalOptions extends js.Object {

}