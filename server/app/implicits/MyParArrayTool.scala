package implicits

import java.io.File
import java.util.concurrent.ForkJoinPool

import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import utils.Utils

import scala.collection.parallel.ForkJoinTaskSupport
import scala.collection.parallel.immutable.ParSeq
import scala.util.Try

/**
 * Created by Administrator on 2019/9/12
 */
trait MyParArrayTool {

  implicit class MyParArray[T](pars: ParSeq[T]) {

    def threadNum(t: Int) = {
      pars.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(t))
      pars

    }

  }


}
