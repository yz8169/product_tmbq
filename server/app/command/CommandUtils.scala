package command

import java.io.File
import java.nio.file.Files

import org.apache.commons.io.FileUtils

import scala.collection.JavaConverters._
import implicits.Implicits._
import utils.Utils

/**
 * Created by Administrator on 2019/12/5
 */
object CommandUtils {

  def callLinuxScript(tmpDir: File, shBuffer: List[String], shPrefix: String = "") = {
    val execCommand = new ExecCommand
    val runFile = new File(tmpDir, s"${shPrefix}run.sh")
    FileUtils.writeLines(runFile, shBuffer.asJava)
    val dos2Unix = s"${("dos2unix").wsl} ${runFile.unixPath} "
    val shCommand = s"${("sh").wsl} ${runFile.unixPath}"
    execCommand.exec(dos2Unix, shCommand, tmpDir)
    execCommand
  }

  def killPid(pidDir: File) = {
    val tmpDir = Files.createTempDirectory("tmpDir").toFile

    def loop(acc: List[String], files: List[File]): List[String] = {
      files match {
        case Nil => acc
        case x :: xs if x.isFile && x.getName == "pid.txt" => val pid = x.str.trim
          loop(pid :: acc, xs)
        case x :: xs if x.isDirectory =>
          loop(acc, xs ::: x.listFiles().toList)
        case x :: xs =>
          loop(acc, xs)
      }
    }

    val pids = loop(List(), List(pidDir))

    val commands = pids.map { pid =>
      s"""
         |kill -s 9 ${pid}
         |""".stripMargin
    }
    val execComand = callLinuxScript(tmpDir, commands)
    Utils.deleteDirectory(tmpDir)
  }

  def orderCallLinuxScript(tmpDir: File, shBuffer: List[String]): ExecCommand = {
    val runFile = new File(tmpDir, s"run.sh")
    val pidFile = new File(tmpDir, "pid.txt")
    val pidCommand =
      s"""
         |echo $$$$ > ${pidFile.unixPath}
         |""".stripMargin
    val deletePidCommand =
      s"""
         |rm ${pidFile.unixPath}
         |""".stripMargin
    val newBuffer = List(pidCommand) ::: shBuffer ::: List(deletePidCommand)
    val shStr = newBuffer.flatMap { line =>
      line.split("\r\n")
    }.notEmptyLines.notAnnoLines.mkString(" &&\\\n")
    (shStr + "\n").toFile(runFile)
    val dos2Unix = s"${("dos2unix").wsl} ${runFile.unixPath} "
    val shCommand = s"${("sh").wsl} ${runFile.unixPath}"
    (ExecCommand()).exec(dos2Unix, shCommand, tmpDir)
  }

  def callWindowsScript(tmpDir: File, shBuffer: List[String], shPrefix: String = "") = {
    val runFile = new File(tmpDir, s"${shPrefix}run.bat")
    FileUtils.writeLines(runFile, shBuffer.asJava)
    val command = s"${runFile.getAbsolutePath}"
    (ExecCommand()).exec(command, tmpDir)
  }

  def pdf2png(pdfFile: File, pngFile: File) = {
    val tmpDir = Files.createTempDirectory("tmpDir").toFile
    val command =
      s"""
         |convert  -density 300 ${pdfFile.getAbsoluteFile.unixPath} ${pngFile.getAbsoluteFile.unixPath}
         |""".stripMargin
    CommandUtils.callLinuxScript(tmpDir, List(command))
    Utils.deleteDirectory(tmpDir)
  }

  def pdf2pngs(ts: List[(File, File)]) = {
    val tmpDir = Files.createTempDirectory("tmpDir").toFile
    val commmands = ts.map { case (pdfFile, pngFile) =>
      s"""
         |convert  -density 300 ${pdfFile.getAbsoluteFile.unixPath} ${pngFile.getAbsoluteFile.unixPath}
         |""".stripMargin
    }
    CommandUtils.callLinuxScript(tmpDir, commmands)
    Utils.deleteDirectory(tmpDir)
  }

  def deletePidFile(pidDir: File) = {
    def loop(file: File): Unit = {
      for (file <- file.listFiles()) {
        if (file.isDirectory) {
          loop(file)
        } else {
          if (file.getName == "pid.txt") {
            FileUtils.deleteQuietly(file)
          }

        }
      }
    }

    loop(pidDir)
  }


}
