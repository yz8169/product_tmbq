package mission

import java.io.File

import tool.Pojo.MissionDirData
import implicits.Implicits._

/**
 * Created by yz on 2018/6/13
 */
object MissionUtils {

  def getMissionDir(missionId: Int, outDir: File) = {
    val missionIdDir = new File(outDir, missionId.toString).createDirectoryWhenNoExist
    val workspaceDir = new File(missionIdDir, "workspace").createDirectoryWhenNoExist
    val resultDir = new File(missionIdDir, "result").createDirectoryWhenNoExist
    val logFile = new File(missionIdDir, "log.txt")
    MissionDirData(workspaceDir, resultDir, logFile)
  }


}
