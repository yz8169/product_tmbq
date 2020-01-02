package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by yz on 2018/7/17
 */
class KitDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends
  HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def selectAll= db.run(Kit.result)

  def selectByName(name: String) = db.run(Kit.
    filter(_.name === name).result.headOption)

  def insert(row: KitRow): Future[Unit] = db.run(Kit += row).map(_ => ())

  def insertAndReturnId(row: KitRow) = db.run(Kit.returning(Kit.map(_.id)) += row)

  def deleteById(id: Int): Future[Unit] = db.run(Kit.filter(_.id === id).delete).map(_ => ())

  def selectById(id: Int) = db.run(Kit.
    filter(_.id === id).result.head)


}
