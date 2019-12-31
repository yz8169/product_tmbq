package myJs.myPkg.bootstrap

/**
 * Created by Administrator on 2019/12/31
 */

import myJs.myPkg.bootstrap.jquery.BootstrapJQueryContext

import scala.language.postfixOps

// JS components implementation
trait JSBootstrapBundle extends  BootstrapJQueryContext

object JSBootstrapBundle {
  def apply(): JSBootstrapBundle = {
    new JSBootstrapBundle {
    }
  }
}