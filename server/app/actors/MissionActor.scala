package actors

import akka.actor.{Actor, ActorSystem, Props, Timers}
import akka.stream.Materializer
import implicits.Implicits._
import javax.inject.{Inject, Singleton}
import org.joda.time.{DateTime, Days}
import tool.Pojo._
import tool._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Created by Administrator on 2019/10/24
 */
@Singleton
class MissionActor @Inject()()(implicit val system: ActorSystem,
                               implicit val materializer: Materializer,
                               implicit val dao: MyDao

) extends Actor {

  override def receive: Receive = {
    case "start" =>
      val missionManageActor = system.actorOf(
        Props(new MissionManageActor())
      )
      missionManageActor ! "ask"

  }
}
