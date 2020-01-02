package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import tool.Pojo.UserData

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by yz on 2018/7/17
  */
class UserDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends
  HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def selectAll: Future[Seq[UserRow]] = db.run(User.result)

  def selectByName(name: String): Future[Option[UserRow]] = db.run(User.
    filter(_.name === name).result.headOption)

  def selectByUserData(user: UserData): Future[Option[UserRow]] = db.run(User.
    filter(_.name === user.name).filter(_.password === user.password).result.headOption)

  def insert(row: UserRow): Future[Unit] = db.run(User += row).map(_ => ())

  def deleteById(id: Int): Future[Unit] = db.run(User.filter(_.id === id).delete).map(_ => ())

  def selectById(id: Int): Future[UserRow] = db.run(User.
    filter(_.id === id).result.head)

  def update(row: UserData): Future[Unit] = db.run(User.filter(_.name === row.name).map(_.password).update(row.password)).
    map(_ => ())


}
