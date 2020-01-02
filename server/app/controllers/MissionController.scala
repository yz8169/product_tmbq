package controllers

import java.io.File
import java.net.URLEncoder

import actors.MissionActor
import akka.actor.{Actor, ActorSystem, PoisonPill, Props}
import akka.stream.Materializer
import dao._
import javax.inject.Inject
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, ControllerComponents, WebSocket}
import tool.{FileTool, FormTool, Tool, WebTool}

import scala.collection.JavaConverters._
import models.Tables._
import org.joda.time.DateTime
import org.zeroturnaround.zip.ZipUtil
import play.api.libs.streams.ActorFlow

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.collection.parallel.ForkJoinTaskSupport
import implicits.Implicits._
import mission.MissionUtils
import tool.Pojo.MyDao
import utils.Utils

import scala.language.postfixOps


/**
 * Created by yz on 2018/9/18
 */
class MissionController @Inject()(cc: ControllerComponents, formTool: FormTool, userDao: UserDao,
                                  accountDao: AccountDao,
                                  missionDao: MissionDao)(implicit val system: ActorSystem,
                                                          implicit val materializer: Materializer,
                                                          implicit val configDao: ConfigDao,
                                                          implicit val modeDao: ModeDao) extends AbstractController(cc) {

  implicit val dao = MyDao(missionDao, configDao)

  val missionActor = system.actorOf(
    Props(new MissionActor())
  )
  missionActor ! "start"

  def getAllMission = Action.async { implicit request =>
    val userId = Tool.getUserId
    missionDao.selectAll(userId).map {
      x =>
        Future {
          val missionIds = x.map(_.id.toString)
          val missionDir = Tool.getUserMissionDir
          missionDir.listFiles().filter { dir =>
            !missionIds.contains(dir.getName)
          }.foreach(_.deleteQuietly)
        }
        val array = Utils.getArrayByTs(x)
        Ok(Json.toJson(array))
    }
  }

  def newMissionBefore = Action { implicit request =>
    val missionName = s"project_${Tool.generateMissionName}"
    Ok(views.html.user.newMission(missionName))
  }

  def missionNameCheck = Action.async { implicit request =>
    val data = formTool.missionNameForm.bindFromRequest.get
    val userId = Tool.getUserId
    missionDao.selectOptionByMissionName(userId, data.missionName).map { mission =>
      mission match {
        case Some(y) => Ok(Json.obj("valid" -> false))
        case None =>
          Ok(Json.obj("valid" -> true))
      }
    }
  }

  def newMission = Action.async(parse.multipartFormData) { implicit request =>
    val threadNum = Tool.getMissionDefaultThreadNum
    val data = formTool.missionNameForm.bindFromRequest().get
    val userId = Tool.getUserId
    val tmpDir = Tool.createTempDirectory("tmpDir")
    val myTmpDir = Tool.getDataDir(tmpDir)
    val myMessage = FileTool.fileCheck(myTmpDir)
    if (myMessage.valid) {
      val row = MissionRow(0, s"${data.missionName}", userId, new DateTime(), None, "preparing", threadNum)
      missionDao.insert(row).flatMap(_ => missionDao.selectByMissionName(userId, row.missionName)).flatMap { mission =>
        val outDir = Tool.getUserMissionDir
        val missionDir = MissionUtils.getMissionDir(mission.id, outDir)
        val (workspaceDir, resultDir) = (missionDir.workspaceDir, missionDir.resultDir)
        FileUtils.copyDirectory(myTmpDir.tmpDir, workspaceDir)
        Tool.deleteDirectory(myTmpDir.tmpDir)
        val newMission = mission.copy(state = "wait")
        missionDao.update(newMission).map { x =>
          Ok(Json.obj("valid" -> true))
        }
      }
    } else {
      Tool.deleteDirectory(myTmpDir.tmpDir)
      Future.successful(Ok(Json.obj("valid" -> myMessage.valid, "message" -> myMessage.message)))
    }
  }

  def updateMissionSocket = WebSocket.accept[JsValue, JsValue] {
    implicit request =>
      val userId = Tool.getUserId
      case class MissionAction(beforeMissions: Seq[MissionRow], action: String)
      ActorFlow.actorRef(out => Props(new Actor {
        override def receive: Receive = {
          case msg: JsValue if (msg \ "info").as[String] == "start" =>
            val beforeMissions = Utils.execFuture(missionDao.selectAll(userId))
            out ! WebTool.getJsonByTs(beforeMissions)
            system.scheduler.scheduleOnce(3 seconds, self, MissionAction(beforeMissions, "update"))
          case MissionAction(beforeMissions, action) =>
            missionDao.selectAll(userId).map {
              missions =>
                val currentMissions = missions
                if (currentMissions.size != beforeMissions.size) {
                  out ! WebTool.getJsonByTs(currentMissions)
                } else {
                  val b = currentMissions.zip(beforeMissions).forall {
                    case (currentMission, beforeMission) =>
                      currentMission.id == beforeMission.id && currentMission.state == beforeMission.state
                  }
                  if (!b) {
                    out ! WebTool.getJsonByTs(currentMissions)
                  }
                }
                system.scheduler.scheduleOnce(3 seconds, self, MissionAction(currentMissions, "update"))
            }
          case _ =>
            self ! PoisonPill
        }

        override def postStop(): Unit = {
          self ! PoisonPill
        }
      }))

  }

  def downloadResult = Action.async {
    implicit request =>
      val userId = Tool.getUserId
      val data = formTool.missionIdForm.bindFromRequest().get
      val missionId = data.missionId
      missionDao.selectByMissionId(userId, missionId).map {
        mission =>
          val missionIdDir = Tool.getMissionIdDirById(missionId)
          val resultDir = new File(missionIdDir, "result")
          val resultFile = new File(missionIdDir, s"result.zip")
          if (!resultFile.exists()) ZipUtil.pack(resultDir, resultFile)
          Ok.sendFile(resultFile).withHeaders(
            CONTENT_DISPOSITION -> Tool.getContentDisposition(s"${mission.missionName}_result.zip"),
            CONTENT_TYPE -> "application/x-download"
          )
      }
  }

  def downloadData = Action.async {
    implicit request =>
      val userId = Tool.getUserId
      val data = formTool.missionIdForm.bindFromRequest().get
      val missionId = data.missionId
      missionDao.selectByMissionId(userId, missionId).map {
        mission =>
          val missionIdDir = Tool.getMissionIdDirById(missionId)
          val dataDir = new File(missionIdDir, "data")
          val dataFile = new File(missionIdDir, s"data.zip")
          if (!dataFile.exists()) ZipUtil.pack(dataDir, dataFile)
          Ok.sendFile(dataFile).withHeaders(
            CONTENT_DISPOSITION -> Tool.getContentDisposition(s"${mission.missionName}_data.zip"),
            CONTENT_TYPE -> "application/x-download"
          )
      }
  }

  def getLogContent = Action.async {
    implicit request =>
      val userId = Tool.getUserId
      val data = formTool.missionIdForm.bindFromRequest().get
      missionDao.selectByMissionId(userId, data.missionId).map {
        mission =>
          val missionIdDir = Tool.getMissionIdDirById(data.missionId)
          val logFile = new File(missionIdDir, s"log.txt")
          val logStr = FileUtils.readFileToString(logFile, "UTF-8")
          Ok(Json.toJson(logStr))
      }
  }

  def deleteMissionById = Action.async {
    implicit request =>
      val data = formTool.missionIdForm.bindFromRequest().get
      missionDao.deleteById(data.missionId).map {
        x =>
          val missionIdDir = Tool.getMissionIdDirById(data.missionId)
          Utils.deleteDirectory(missionIdDir)
          Redirect(routes.MissionController.getAllMission())
      }
  }




}
