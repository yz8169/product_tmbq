package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

/**
 * Created by yz on 2018/5/29
 */
class ConfigDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends
  HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def selectHost = db.run(Config.filter(_.kind === "host").result.head)

  def selectThreadNum = db.run(Config.filter(_.kind === "thread").result.head)

  def updateThreadNum(threadNum: String) = db.run(Config.filter(_.kind === "thread").map(_.value).
    update(threadNum))

  def selectParalleNum = db.run(Config.filter(_.kind === "parallel").result.head)

  def selectSaveDay = db.run(Config.filter(_.kind === "save.day").result.head)


}
