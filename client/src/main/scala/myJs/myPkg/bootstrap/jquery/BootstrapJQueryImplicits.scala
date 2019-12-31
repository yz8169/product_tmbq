package myJs.myPkg.bootstrap.jquery

import org.scalajs.jquery.JQuery

/**
 * Created by Administrator on 2019/12/31
 */

trait BootstrapJQueryImplicits {
  implicit def implicitBootstrapJQuery(jq: JQuery): BootstrapJQuery = {
    jq.asInstanceOf[BootstrapJQuery]
  }
}

object BootstrapJQueryImplicits extends BootstrapJQueryImplicits
