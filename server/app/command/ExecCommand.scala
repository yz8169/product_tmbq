package command

import java.io.File

import scala.sys.process
import scala.sys.process._

case class ExecCommand(isSuccess: Boolean = true,err:StringBuilder=new StringBuilder) {
  val out = new StringBuilder
  val log = ProcessLogger(out append _ append "\n", err append _ append "\n")

  def exec(command: String, outFile: File, workspace: File) = {
    val process = Process(command, workspace).#>(outFile)
    val exitCode = process ! log
    if (exitCode != 0) this.copy(isSuccess = false) else this
  }

  def exec(command: String, workspace: File) = {
    val process = Process(command, workspace)
    val exitCode = process ! log
    if (exitCode != 0) this.copy(isSuccess = false) else this
  }

  def exec(command1: String, command2: String, workspace: File) = {
    val exitCode = Process(command1, workspace).#&&(Process(command2, workspace)) ! log
    if (exitCode != 0) this.copy(isSuccess = false) else this
  }

  def exec(command1: String, command2: String, command3: String, workspace: File) = {
    val exitCode = Process(command1, workspace).#&&(Process(command2, workspace)).#&&(Process(command3, workspace)) ! log
    if (exitCode != 0) this.copy(isSuccess = false) else this
  }


  def getErrStr = err.toString()


  def getOutStr = out.toString()

  def getLogStr = out.toString() + "\n" + err.toString()


}

object ExecCommand{

  def apply(isSuccess:Boolean=false,err:StringBuilder=new StringBuilder)={
    new ExecCommand(isSuccess,err)
  }

}
