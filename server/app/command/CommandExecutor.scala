package command

import java.io.File

import command.CommandExec.{CommandError, CommandSuccess}
import javax.inject.Inject
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import play.api.mvc.RequestHeader
import tool.Pojo.CommandData

import scala.collection.parallel.ParSeq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import implicits.Implicits._
import utils.Utils

import scala.reflect.ClassTag

/**
 * Created by yz on 2018/6/13
 */

sealed abstract class CommandExec[+E, +A] extends Product with Serializable {

  def andThen[EE >: E, B](f: A => CommandExec[EE, B]): CommandExec[EE, B] =
    this match {
      case CommandSuccess(a) => f(a)
      case i@CommandError(_) => i
    }

  def exec[X: ClassTag](f: A => CommandData): CommandExec[Any, Boolean] = {
    this.andThen { b =>
      val commandData = f(b)
      exec(commandData)
    }
  }

  def exec(commandData: CommandData): CommandExec[Any, Boolean] = {
    this.andThen { x =>
      val execCommand = CommandUtils.orderCallLinuxScript(commandData.workspace, commandData.command)
      execCommand.toCommandExec
    }
  }

  def map(f: A => Unit) = {
    this.andThen { b =>
      f(b)
      CommandSuccess(b)
    }
  }

  def parExec(commandDatas: ParSeq[CommandData]): CommandExec[Any, Boolean] = {
    this.andThen { commandExecutor =>
      val tmpAcc = CommandExec()
      commandDatas.aggregate(tmpAcc)((acc, commandData) => {
        acc.andThen { b =>
          val execCommand = CommandUtils.orderCallLinuxScript(commandData.workspace, commandData.command)
          execCommand.toCommandExec
        }
      },
        (acc, otherAcc) => {
          acc.andThen { b =>
            otherAcc
          }
        }
      )
    }
  }

  def parExec(f: A => ParSeq[CommandData]): CommandExec[Any, Boolean] = {
    this.andThen { b =>
      val commandDatas = f(b)
      parExec(commandDatas)
    }
  }

  def isSuccess: Boolean = this match {
    case CommandError(_) => false
    case _ => true
  }

  def errorInfo = {
    this match {
      case CommandSuccess(a) => ""
      case CommandError(e) => e.toString
    }
  }

  def flatMap[T](f: () => Future[T]) = {
    if (isSuccess) {
      Utils.execFuture(f())
    }
    this

  }


}

object CommandExec {

  final case class CommandSuccess[+A](a: A) extends CommandExec[Nothing, A]

  final case class CommandError[+E](e: E) extends CommandExec[E, Nothing]

  def apply[E, A](test: Boolean, a: => A, e: => E): CommandExec[E, A] =
    if (test) CommandSuccess(a) else CommandError(e)

  def apply(test: Boolean = true) =
    if (test) CommandSuccess(true) else CommandError("")

}
