package implicits

import cats.data.Validated
import tool.Pojo.MyMessage

import scala.collection.SeqMap

/**
 * Created by Administrator on 2019/9/12
 */
trait MyValidatedTool {

  implicit class MyValidated(validated: Validated[String, Boolean]) {

    def toMyMessage = {
      val message = validated.swap.getOrElse("")
      MyMessage(validated.isValid, message)
    }

  }

}
