package actors

import java.io.File
import java.nio.file.Files

import akka.actor.{Actor, ActorSystem, PoisonPill}
import akka.stream.Materializer
import command.CommandExec
import dao._
import javax.inject.Inject
import models.Tables._
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.RequestHeader
import tool.Pojo._
import tool._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import implicits.Implicits._
import org.zeroturnaround.zip.ZipUtil
import tool.Tool.getConfigFile

/**
 * Created by Administrator on 2019/10/24
 */
class MissionExecActor @Inject()(mission: MissionRow)(implicit val system: ActorSystem,
                                                      implicit val materializer: Materializer,
                                                      implicit val dao: MyDao
) extends Actor {
  val missionDao = dao.missionDao
  implicit val configDao = dao.configDao

  override def receive: Receive = {
    case "run" =>
      val missionId = mission.id
      val workspaceDir = Tool.getMissionWorkspaceDir(mission)
      val logFile = new File(workspaceDir.getParent, "log.txt")
      val newMision = mission.copy(state = "running")
      missionDao.update(newMision).map { x =>
        val missionIdDir = Tool.getMissionIdDir(mission)
        val resultDir = Tool.getMissionResultDir(mission)
        val tmpDataDir = new File(workspaceDir, "tmpData")
        val dataDir = new File(workspaceDir, "data").createDirectoryWhenNoExist
        val file = new File(workspaceDir, "data.zip")
        tmpDataDir.allFiles.foreach { file =>
          val destFile = new File(dataDir, file.getName.toLowerCase)
          FileUtils.copyFile(file, destFile)
        }
        val threadNum = mission.cpu
        val tmpCompoundConfigFile = Tool.getSimpleCompoundFile(workspaceDir)
        val dbCompoundConfigFile = new File(workspaceDir, "db_compound.xlsx")
        val compoundConfigFile = Tool.productCompoundFile(workspaceDir, tmpCompoundConfigFile, dbCompoundConfigFile)
        Tool.productDtaFiles(workspaceDir, compoundConfigFile, dataDir, threadNum)

        val rBaseFile = new File(Tool.rPath, "base.R")
        FileUtils.copyFileToDirectory(rBaseFile, workspaceDir)

        val compoundLines = compoundConfigFile.xlsxLines()
        val indexDatas = compoundLines.lineMap.map { map =>
          IndexData(map("index"), map("compound"))
        }
        val isIndexs = indexDatas.filter(x => x.index.startWithsIgnoreCase("is"))

        val commandExec = CommandExec().exec { b =>
          Tool.rtCorrect(workspaceDir)
        }.map { b =>
          val configFile = getConfigFile(workspaceDir)
          val configMap = configFile.txtLines.map { columns =>
            (columns(0) -> columns(1))
          }.toMap
          val isRtCorrect = configMap("rtCorrect")
          if (isRtCorrect.toBoolean) {
            val compoundFile = Tool.getCompoundFile(workspaceDir)
            compoundFile.xlsxLines().toXlsxFile(compoundFile)
            val correctLines = compoundFile.xlsxLines().selectColumns(List("compound", "rt")).rename("rt" -> "Corrected_RT")
            val correctedFile = new File(resultDir, "Corrected_RT.xlsx")
            tmpCompoundConfigFile.xlsxLines().leftJoin(correctLines, "compound").toXlsxFile(correctedFile)
          }
        }.parExec { b =>
          Tool.isFindPeak(workspaceDir, isIndexs, threadNum)
        }.exec { b =>
          Tool.isMerge(workspaceDir, isIndexs)
        }.parExec { b =>
          Tool.cFindPeak(workspaceDir, indexDatas, threadNum)
        }.exec { b =>
          //intensity merge
          Tool.intensityMerge(workspaceDir)
        }.parExec { b =>
          //regress
          Tool.eachRegress(workspaceDir, threadNum)
        }.exec { b =>
          //all merge
          Tool.allMerge(workspaceDir)
        }

        val state = if (commandExec.isSuccess) {
          val intensityTxtFile = new File(workspaceDir, "intensity.txt")
          val intensityExcelFile = new File(resultDir, "intensity.xlsx")
          intensityTxtFile.toXlsxFile(intensityExcelFile)
          val regressTxtFile = new File(workspaceDir, "regress.txt")
          val regressColorFile = new File(workspaceDir, "color.txt")
          val regressExcelFile = new File(resultDir, "concentration.xlsx")
          Tool.dye(regressTxtFile, regressColorFile, regressExcelFile)

          FileUtils.copyDirectoryToDirectory(new File(workspaceDir, "plot_peaks"), resultDir)
          FileUtils.copyDirectoryToDirectory(new File(workspaceDir, "plot_regress"), resultDir)
          "success"
        } else {
          commandExec.errorInfo.toFile(logFile)
          "error"
        }
        val newMission = mission.copy(state = state, endTime = Some(new DateTime()))
        missionDao.update(newMission).map { x =>
        }
      }.onComplete {
        case Failure(exception) =>
          exception.printStackTrace()
          exception.toString.toFile(logFile)
          val newMission = mission.copy(state = "error", endTime = Some(new DateTime()))
          missionDao.update(newMission).map { x =>
            self ! "stop"
          }
        case Success(x) => self ! "stop"
      }

    case "stop" =>
      self ! PoisonPill

  }

}
