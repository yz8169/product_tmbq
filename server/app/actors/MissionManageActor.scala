package actors

import java.io.File

import akka.actor.{Actor, ActorSystem, PoisonPill, Props, Timers}
import akka.stream.Materializer
import dao._
import javax.inject.{Inject, Singleton}
import models.Tables._
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import play.api.libs.ws.WSClient
import play.api.mvc.RequestHeader
import tool.Pojo._
import tool._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}
import scala.language.postfixOps

/**
 * Created by Administrator on 2019/10/24
 */
@Singleton
class MissionManageActor @Inject()()(implicit val system: ActorSystem,
                                     implicit val materializer: Materializer,
                                     implicit val dao: MyDao

) extends Actor with Timers {

  timers.startPeriodicTimer("timer", "ask", 10 seconds)
  implicit val configDao = dao.configDao

  val availCpu = Tool.availCpu
  val missionDao = dao.missionDao

  missionDao.selectAll("running").map { missions =>
    missions.foreach { mission =>
      self ! mission
    }
  }


  override def receive: Receive = {
    case "ask" =>
      missionDao.selectAll("running").map { missions =>
        val totalUseCpu = missions.map(_.cpu).sum
        val remainCpu = availCpu - totalUseCpu
        missionDao.selectAll("wait").map { totalMissions =>
          val canRunMissions = totalMissions.filter(_.cpu <= remainCpu).sortBy(_.startTime.getMillis)
          if (!canRunMissions.isEmpty ) {
            val mission = canRunMissions(0)
            val missionActor = context.actorOf(
              Props(new MissionExecActor(mission))
            )
            missionActor ! "run"
          }
        }

      }
    case mission: MissionRow =>
      val newMission = mission.copy(state = "wait")
      missionDao.update(newMission)


  }
}
