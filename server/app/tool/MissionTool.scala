package tool

import java.io.File

import play.api.mvc.RequestHeader
import models.Tables._

/**
 * Created by Administrator on 2020/1/8
 */
object MissionTool extends MissionToolT {

  def getUserMissionDir(userId: Int) = {
    val userIdDir = Tool.getUserIdDir(userId)
    new File(userIdDir, "mission")
  }

  def getMissionIdDir(mission: MissionRow) = {
    val userMissionFile = getUserMissionDir(mission.userId)
    new File(userMissionFile, mission.id.toString)
  }

  def getMissionResultDir(mission: MissionRow) = {
    val missionIdDir = getMissionIdDir(mission)
    new File(missionIdDir, "result")
  }

  def getMissionWorkspaceDir(mission: MissionRow) = {
    val missionIdDir = getMissionIdDir(mission)
    new File(missionIdDir, "workspace")
  }


}
