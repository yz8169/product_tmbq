package tool

import java.io.File

import play.api.mvc.RequestHeader

import scala.reflect.ClassTag

/**
 * Created by Administrator on 2020/1/8
 */
trait MissionToolT {

  def getUserMissionDir(userId: Int): File

  def getUserMissionDir()(implicit request: RequestHeader): File = {
    val userId = Tool.getUserId
    getUserMissionDir(userId)
  }

  def getMissionIdDirById(missionId: Int)(implicit request: RequestHeader) = {
    val userMissionFile = getUserMissionDir
    new File(userMissionFile, missionId.toString)
  }

  def getWorkspaceDirById(missionId: Int)(implicit request: RequestHeader) = {
    val missionIdDir = getMissionIdDirById(missionId)
    new File(missionIdDir, "workspace")
  }


}
