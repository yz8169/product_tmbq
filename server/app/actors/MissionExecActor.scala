package actors

import java.io.File
import java.nio.file.Files

import akka.actor.{Actor, ActorSystem, PoisonPill}
import akka.stream.Materializer
import command.CommandExecutor
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
        val commandExecutor = CommandExecutor(logFile = logFile)
        val file = new File(workspaceDir, "data.zip")
        tmpDataDir.allFiles.foreach { file =>
          val destFile = new File(dataDir, file.getName.toLowerCase)
          FileUtils.copyFile(file, destFile)
        }
        val threadNum = mission.cpu
        val compoundConfigFile = new File(workspaceDir, "compound_config.xlsx")
        Tool.productDtaFiles(workspaceDir, compoundConfigFile, dataDir, threadNum)

        val rBaseFile = new File(Tool.rPath, "base.R")
        FileUtils.copyFileToDirectory(rBaseFile, workspaceDir)

        val compoundLines = compoundConfigFile.xlsxLines()
        val indexDatas = compoundLines.lineMap.map { map =>
          IndexData(map("index"), map("compound"))
        }
        val isIndexs = indexDatas.filter(x => x.index.startWithsIgnoreCase("is"))

        commandExecutor.exec { () =>
          //is find peak
          Tool.isFindPeak(workspaceDir, isIndexs, threadNum)
        }.exec { () =>
          //is merge
          Tool.isMerge(workspaceDir, isIndexs)
        }.exec { () =>
          //compound find peak
          Tool.cFindPeak(workspaceDir, indexDatas, threadNum)
        }.exec { () =>
          //intensity merge
          Tool.intensityMerge(workspaceDir)
        }.exec { () =>
          //regress
          Tool.eachRegress(workspaceDir, threadNum)
        }.exec { () =>
          //all merge
          Tool.allMerge(workspaceDir)
        }
        val state = if (commandExecutor.isSuccess) {
          val intensityTxtFile = new File(workspaceDir, "intensity.txt")
          val intensityExcelFile = new File(resultDir, "intensity.xlsx")
          intensityTxtFile.toXlsxFile(intensityExcelFile)
          val regressTxtFile = new File(workspaceDir, "regress.txt")
          val regressColorFile = new File(workspaceDir, "color.txt")
          val regressExcelFile = new File(resultDir, "concentration.xlsx")
          Tool.dye(regressTxtFile, regressColorFile, regressExcelFile)

          FileUtils.copyDirectoryToDirectory(new File(workspaceDir, "plot_peaks"), resultDir)
          FileUtils.copyDirectoryToDirectory(new File(workspaceDir, "plot_regress"), resultDir)
          val originalDataDir = new File(resultDir.getParent, "data").createDirectoryWhenNoExist
          val sampleConfigExcelFile = new File(workspaceDir, "sample_config.xlsx")
          FileUtils.copyFileToDirectory(sampleConfigExcelFile, originalDataDir)
          FileUtils.copyFileToDirectory(compoundConfigFile, originalDataDir)
          FileUtils.copyFileToDirectory(file, originalDataDir)
          "success"
        } else {
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
