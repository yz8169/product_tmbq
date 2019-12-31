package myJs.myPkg

import jsext._
import org.scalajs.jquery.JQuery

import scala.scalajs.js
import scala.language.implicitConversions
import scala.scalajs.js.JSConverters._
import scala.scalajs.js.annotation.JSImport


/**
  * Created by yz on 2019/3/14
  */
@js.native
trait TypeaheadJquery extends js.Object {

  def typeahead(options: TypeaheadOptions): JQuery = scalajs.js.native

  def typeahead(method: String): JQuery = scalajs.js.native

}

@js.native
@JSImport("Typeahead", JSImport.Namespace)
abstract class Typeahead extends js.Object {

  def query: String = js.native

  def $element: JQuery = js.native

}

object TypeaheadOptions extends TypeaheadOptionsBuilder(noOpts)

class TypeaheadOptionsBuilder(val dict: OptMap) extends JSOptionBuilder[TypeaheadOptions, TypeaheadOptionsBuilder](new TypeaheadOptionsBuilder(_)) {

  def source(v: js.Array[String]) = jsOpt("source", v)

  def updater(v: js.ThisFunction1[Typeahead, String, String]) = jsOpt("updater", v)

  def highlighter(v: js.ThisFunction1[Typeahead, String, String]) = jsOpt("highlighter", v)

  def matcher(v: js.ThisFunction1[Typeahead, String, AnyVal]) = jsOpt("matcher", v)

}

trait TypeaheadOptions extends js.Object {

}


trait TypeaheadJQueryImplicits {
  implicit def implicitTypeaheadJQuery(jq: JQuery) = {
    jq.asInstanceOf[TypeaheadJquery]
  }
}
