package myJs.implicits

import org.scalajs.dom
import org.scalajs.dom.{Element, Event, window}
import myJs.myPkg.jquery._

/**
 * Created by Administrator on 2019/12/17
 */
trait MyWindowTool {

  implicit class MyWindow(window: dom.Window) {

    def openNewWindow(url: String) = {
      //      val tempWindow = window.open(target = "_blank")
      //      tempWindow.location.href = url
      $("#hideA").attr("href",url)
      $("#hideA").on("click", (y: Element, e: Event) => {
        e.preventDefault()
        val tempWindow = window.open(target = "_blank")
        tempWindow.location.href = url
        false
      })
      $(s"#hideA")(0).click()
    }

    def redirect(url: String) = {
      window.location.href = url
    }

  }

}
