package myJs.myPkg

import jsext._

import scala.scalajs.js
import myJs.Utils._

/**
  * Created by yz on 2019/3/14
  */
package object Swal {

  def swal(options: SwalOptions)=g.swal(options)

}

object SwalOptions extends SwalOptionsBuilder(noOpts)

class SwalOptionsBuilder(val dict: OptMap) extends JSOptionBuilder[SwalOptions, SwalOptionsBuilder](new SwalOptionsBuilder(_)) {

  def text(v: js.Any) = jsOpt("text", v)

  def title(v: String) = jsOpt("title", v)

  def `type`(v: String) = jsOpt("type", v)

}

trait SwalOptions extends js.Object {

}