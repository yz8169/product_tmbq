package myJs.myPkg.bootstrap.jquery

import org.scalajs.jquery.JQueryStatic

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
 * Created by Administrator on 2019/12/31
 */

trait JQueryContext {
  def jQuery = JQueryContext.jQuery
}

object JQueryContext {
  val jQuery: JQueryStatic = org.scalajs.jquery.jQuery

  object imports {
    @js.native
    @JSImport("jquery", JSImport.Namespace)
    object jQuery extends JQueryStatic
  }
}
