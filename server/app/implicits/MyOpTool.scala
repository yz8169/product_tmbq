package implicits

import java.io.File

import com.github.tototoshi.csv._

/**
 * Created by Administrator on 2019/9/12
 */
trait MyOpTool {

  implicit class MyOp[T](op: Option[T]) {

    def toMyString = {
      op.map(x => x.toString).getOrElse("NA")
    }

  }


}
