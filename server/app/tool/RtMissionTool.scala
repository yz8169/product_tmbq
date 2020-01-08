package tool

import java.io.File

import play.api.libs.Files.TemporaryFile
import play.api.mvc.{MultipartFormData, Request, RequestHeader}
import tool.Pojo.RtCorrectDataDir
import implicits.Implicits._
import models.Tables._

/**
 * Created by Administrator on 2020/1/8
 */
object RtMissionTool extends MissionToolT {

  def getUserMissionDir(userId: Int) = {
    val userIdDir = Tool.getUserIdDir(userId)
    new File(userIdDir, "rt_correct_mission")
  }

  def getMissionIdDir(mission: RtMissionRow) = {
    val userMissionFile = getUserMissionDir(mission.userId)
    new File(userMissionFile, mission.id.toString)
  }

  def getMissionWorkspaceDir(mission: RtMissionRow) = {
    val missionIdDir = getMissionIdDir(mission)
    new File(missionIdDir, "workspace")
  }

  def getMissionResultDir(mission: RtMissionRow) = {
    val missionIdDir = getMissionIdDir(mission)
    new File(missionIdDir, "result")
  }


}
