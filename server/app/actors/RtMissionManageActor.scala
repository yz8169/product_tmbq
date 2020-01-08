package actors

import akka.actor.{Actor, ActorSystem, Props, Timers}
import akka.stream.Materializer
import javax.inject.{Inject, Singleton}
import models.Tables._
import org.joda.time.DateTime
import tool.Pojo._
import tool._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by Administrator on 2019/10/24
 */
@Singleton
class RtMissionManageActor @Inject()()(implicit val system: ActorSystem,
                                       implicit val materializer: Materializer,
                                       implicit val dao: MyDao

) extends Actor with Timers {

  timers.startPeriodicTimer("timer", "ask", 1 seconds)
  implicit val configDao = dao.configDao

  val availCpu = Tool.availCpu
  val missionDao = dao.rtMissionDao

  missionDao.selectAll("running").map { missions =>
    missions.foreach { mission =>
      self ! mission
    }
  }


  override def receive: Receive = {
    case "ask" =>
      missionDao.selectAll("running").map { missions =>
        missionDao.selectAll("wait").map { totalMissions =>
          val canRunMissions = totalMissions.sortBy(_.startTime.getMillis)
          if (!canRunMissions.isEmpty) {
            val mission = canRunMissions(0)
            val missionActor = context.actorOf(
              Props(new RtMissionExecActor(mission))
            )
            missionActor ! "run"
          }
        }

      }
    case mission: RtMissionRow =>
      val newMission = mission.copy(state = "wait")
      missionDao.update(newMission)


  }
}
