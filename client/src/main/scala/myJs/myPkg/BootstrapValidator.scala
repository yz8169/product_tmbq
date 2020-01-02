package myJs.myPkg

import jsext._

import scala.scalajs.js
import scala.language.implicitConversions
import scala.scalajs.js.JSConverters._
import jquery.JQuery


/**
 * Created by yz on 2019/3/14
 */
trait BootstrapValidatorJQuery extends js.Object {

  def bootstrapValidator(method: String,fieldName:String): JQuery = scalajs.js.native

}

object BootstrapValidatorOptions extends BootstrapValidatorOptionsBuilder(noOpts)

class BootstrapValidatorOptionsBuilder(val dict: OptMap) extends JSOptionBuilder[BootstrapValidatorOptions, BootstrapValidatorOptionsBuilder](new BootstrapValidatorOptionsBuilder(_)) {

  def showPreview(v: Boolean) = jsOpt("showPreview", v)

  def browseLabel(v: String) = jsOpt("browseLabel", v)

  def removeLabel(v: String) = jsOpt("removeLabel", v)

  def language(v: String) = jsOpt("language", v)

}

trait BootstrapValidatorOptions extends js.Object {

}

trait BootstrapValidatorJQueryImplicits {
  implicit def implicitBootstrapValidatorJQuery(jq: JQuery): BootstrapValidatorJQuery = {
    jq.asInstanceOf[BootstrapValidatorJQuery]
  }
}
