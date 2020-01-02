package myJs.myPkg.bootstrap.jquery

import scala.scalajs.js
import scala.scalajs.js.annotation.JSImport

/**
 * Created by Administrator on 2019/12/31
 */

trait BootstrapJQueryContext extends JQueryContext with BootstrapJQueryImplicits

object BootstrapJQueryContext {
  object imports {
    @js.native
    @JSImport("bootstrap", JSImport.Namespace)
    object bootstrap extends js.Object
  }

}
