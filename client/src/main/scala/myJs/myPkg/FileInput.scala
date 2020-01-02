package myJs.myPkg

import jsext._

import scala.scalajs.js
import scala.language.implicitConversions
import scala.scalajs.js.JSConverters._
import jquery.JQuery


/**
 * Created by yz on 2019/3/14
 */
trait FileInputJQuery extends js.Object {

  def fileinput(options: FileInputOptions): JQuery = scalajs.js.native

  def fileinput(method: String): JQuery = scalajs.js.native

}

object FileInputOptions extends FileInputOptionsBuilder(noOpts)

class FileInputOptionsBuilder(val dict: OptMap) extends JSOptionBuilder[FileInputOptions, FileInputOptionsBuilder](new FileInputOptionsBuilder(_)) {

  def showPreview(v: Boolean) = jsOpt("showPreview", v)

  def browseLabel(v: String) = jsOpt("browseLabel", v)

  def removeLabel(v: String) = jsOpt("removeLabel", v)

  def language(v: String) = jsOpt("language", v)

}

trait FileInputOptions extends js.Object {

}

trait FileInputJQueryImplicits {
  implicit def implicitFileInputJQuery(jq: JQuery): FileInputJQuery = {
    jq.asInstanceOf[FileInputJQuery]
  }
}
