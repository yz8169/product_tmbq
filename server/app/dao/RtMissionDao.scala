package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by yz on 2018/4/27
 */
class RtMissionDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends
  HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(row: RtMissionRow) = db.run(RtMission += row).map(_ => ())

  def selectByMissionName(userId: Int, missionName: String) = db.run(RtMission.
    filter(_.userId === userId).filter(_.missionName === missionName).result.head)

  def selectOptionByMissionName(userId: Int, missionName: String) = db.run(RtMission.
    filter(_.userId === userId).filter(_.missionName === missionName).result.headOption)

  def selectByMissionId(userId: Int, missionId: Int) = db.run(RtMission.
    filter(_.userId === userId).filter(_.id === missionId).result.head)

  def update(row: RtMissionRow): Future[Unit] = db.run(RtMission.filter(_.id === row.id).update(row)).map(_ => ())

  def selectAll(userId: Int) = db.run(RtMission.filter(_.userId === userId).sortBy(_.id.desc).result)

  def selectAll(state: String) = db.run(RtMission.filter(_.state === state).sortBy(_.id.desc).result)

  def selectAll(userId: Int, state: String) = db.run(RtMission.
    filter(x => x.userId === userId && x.state === state).sortBy(_.id.desc).result)

  def deleteById(id: Int): Future[Unit] = db.run(RtMission.filter(_.id === id).delete).map(_ => ())

  def deleteByUserId(userId: Int): Future[Unit] = db.run(RtMission.filter(_.userId === userId).delete).map(_ => ())


}
