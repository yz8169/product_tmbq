package command

import java.io.File

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
case class CommandExecutor(isSuccess: Boolean = true, logFile: File) {

  init

  def init = {
    "Run successed!".toFile(logFile)
  }

  def map(f: () => Unit) = {
    if (isSuccess) {
      f()
    }
    this

  }

  def mapThis(f: CommandExecutor => Boolean) = {
    if (isSuccess) {
      if (f(this)) this else this.copy(isSuccess = false)
    } else this
  }

  def exec(commandDatas: ParSeq[CommandData]): CommandExecutor = {
    this.mapThis { commandExecutor =>
      commandDatas.forall { commandData =>
        val execCommand = CommandUtils.callLinuxScript(commandData.workspace, commandData.command)
        printLog(execCommand)
        execCommand.isSuccess
      }
    }
  }

  def exec(f: () => ParSeq[CommandData]): CommandExecutor = {
    if (isSuccess) {
      val commandDatas = f()
      exec(commandDatas)
    }
    this

  }

  def exec(commandData: CommandData): CommandExecutor = {
    this.mapThis { x =>
      val execCommand = CommandUtils.orderCallLinuxScript(commandData.workspace, commandData.command)
      printLog(execCommand)
      execCommand.isSuccess
    }
  }

  def exec[X: ClassTag](f: () => CommandData): CommandExecutor = {
    if (isSuccess) {
      val commandData = f()
      exec(commandData)
    }
    this

  }

  def flatMap[T](f: () => Future[T]) = {
    if (isSuccess) {
      Utils.execFuture(f())
    }
    this

  }


  def printLog(execCommand: ExecCommand) = {
    if (!execCommand.isSuccess) {
      execCommand.getErrStr.toFile(logFile)
    }

  }


}
