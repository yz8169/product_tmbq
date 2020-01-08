package actors

import java.io.File

import akka.actor.{Actor, ActorSystem, PoisonPill}
import akka.stream.Materializer
import command.CommandExec
import implicits.Implicits._
import javax.inject.Inject
import models.Tables._
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import tool.Pojo._
import tool.Tool.getConfigFile
import tool._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * Created by Administrator on 2019/10/24
 */
class RtMissionExecActor @Inject()(mission: RtMissionRow)(implicit val system: ActorSystem,
                                                          implicit val materializer: Materializer,
                                                          implicit val dao: MyDao
) extends Actor {
  val missionDao = dao.rtMissionDao
  implicit val configDao = dao.configDao
  import RtMissionTool._

  override def receive: Receive = {
    case "run" =>
      val missionId = mission.id
      val workspaceDir = getMissionWorkspaceDir(mission)
      val logFile =Tool.getLogFile(workspaceDir.getParentFile)
      val newMision = mission.copy(state = "running")
      missionDao.update(newMision).map { x =>
        val missionIdDir = getMissionIdDir(mission)
        val resultDir = getMissionResultDir(mission)
        val tmpCompoundConfigFile = Tool.getSimpleCompoundFile(workspaceDir)
        val dbCompoundConfigFile = new File(workspaceDir, "db_compound.xlsx")
        val compoundConfigFile = Tool.productCompoundFile(workspaceDir, tmpCompoundConfigFile, dbCompoundConfigFile)

        val commandExec = CommandExec().exec { b =>
          Tool.rtCorrect(workspaceDir)
        }.map { b =>
          val compoundFile = Tool.getCompoundFile(workspaceDir)
          compoundFile.xlsxLines().toXlsxFile(compoundFile)
          compoundFile.xlsxLines().selectColumns(List("compound", "rt")).toXlsxFile(new File(resultDir, "RT_Corrected.xlsx"))
        }

        val state = if (commandExec.isSuccess) {
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
