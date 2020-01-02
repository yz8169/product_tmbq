package models
// AUTO-GENERATED Slick data model
/** Stand-alone Slick data model for immediate use */
object Tables extends {
  val profile = slick.jdbc.MySQLProfile
} with Tables

/** Slick data model trait for extension, choice of backend or usage in the cake pattern. (Make sure to initialize this late.) */
trait Tables {
  val profile: slick.jdbc.JdbcProfile
  import profile.api._
  import com.github.tototoshi.slick.MySQLJodaSupport._
  import org.joda.time.DateTime
  import slick.model.ForeignKeyAction
  // NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
  import slick.jdbc.{GetResult => GR}

  /** DDL for all tables. Call .create to execute. */
  lazy val schema: profile.SchemaDescription = Array(Account.schema, AdjustMission.schema, Kit.schema, Mission.schema, Mode.schema, User.schema).reduceLeft(_ ++ _)
  @deprecated("Use .schema instead of .ddl", "3.0")
  def ddl = schema

  /** Entity class storing rows of table Account
   *  @param id Database column id SqlType(INT), PrimaryKey
   *  @param account Database column account SqlType(VARCHAR), Length(255,true)
   *  @param password Database column password SqlType(VARCHAR), Length(255,true) */
  case class AccountRow(id: Int, account: String, password: String)
  /** GetResult implicit for fetching AccountRow objects using plain SQL queries */
  implicit def GetResultAccountRow(implicit e0: GR[Int], e1: GR[String]): GR[AccountRow] = GR{
    prs => import prs._
    AccountRow.tupled((<<[Int], <<[String], <<[String]))
  }
  /** Table description of table account. Objects of this class serve as prototypes for rows in queries. */
  class Account(_tableTag: Tag) extends profile.api.Table[AccountRow](_tableTag, Some("product_tmbq"), "account") {
    def * = (id, account, password) <> (AccountRow.tupled, AccountRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(account), Rep.Some(password))).shaped.<>({r=>import r._; _1.map(_=> AccountRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.PrimaryKey)
    /** Database column account SqlType(VARCHAR), Length(255,true) */
    val account: Rep[String] = column[String]("account", O.Length(255,varying=true))
    /** Database column password SqlType(VARCHAR), Length(255,true) */
    val password: Rep[String] = column[String]("password", O.Length(255,varying=true))
  }
  /** Collection-like TableQuery object for table Account */
  lazy val Account = new TableQuery(tag => new Account(tag))

  /** Entity class storing rows of table AdjustMission
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param fromMission Database column from_mission SqlType(VARCHAR), Length(255,true)
   *  @param compoundName Database column compound_name SqlType(VARCHAR), Length(255,true)
   *  @param args Database column args SqlType(VARCHAR), Length(255,true)
   *  @param missionName Database column mission_name SqlType(VARCHAR), Length(255,true)
   *  @param userId Database column user_id SqlType(INT)
   *  @param startTime Database column start_time SqlType(DATETIME)
   *  @param endTime Database column end_time SqlType(DATETIME), Default(None)
   *  @param state Database column state SqlType(VARCHAR), Length(255,true) */
  case class AdjustMissionRow(id: Int, fromMission: String, compoundName: String, args: String, missionName: String, userId: Int, startTime: DateTime, endTime: Option[DateTime] = None, state: String)
  /** GetResult implicit for fetching AdjustMissionRow objects using plain SQL queries */
  implicit def GetResultAdjustMissionRow(implicit e0: GR[Int], e1: GR[String], e2: GR[DateTime], e3: GR[Option[DateTime]]): GR[AdjustMissionRow] = GR{
    prs => import prs._
    AdjustMissionRow.tupled((<<[Int], <<[String], <<[String], <<[String], <<[String], <<[Int], <<[DateTime], <<?[DateTime], <<[String]))
  }
  /** Table description of table adjust_mission. Objects of this class serve as prototypes for rows in queries. */
  class AdjustMission(_tableTag: Tag) extends profile.api.Table[AdjustMissionRow](_tableTag, Some("product_tmbq"), "adjust_mission") {
    def * = (id, fromMission, compoundName, args, missionName, userId, startTime, endTime, state) <> (AdjustMissionRow.tupled, AdjustMissionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(fromMission), Rep.Some(compoundName), Rep.Some(args), Rep.Some(missionName), Rep.Some(userId), Rep.Some(startTime), endTime, Rep.Some(state))).shaped.<>({r=>import r._; _1.map(_=> AdjustMissionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get, _6.get, _7.get, _8, _9.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column from_mission SqlType(VARCHAR), Length(255,true) */
    val fromMission: Rep[String] = column[String]("from_mission", O.Length(255,varying=true))
    /** Database column compound_name SqlType(VARCHAR), Length(255,true) */
    val compoundName: Rep[String] = column[String]("compound_name", O.Length(255,varying=true))
    /** Database column args SqlType(VARCHAR), Length(255,true) */
    val args: Rep[String] = column[String]("args", O.Length(255,varying=true))
    /** Database column mission_name SqlType(VARCHAR), Length(255,true) */
    val missionName: Rep[String] = column[String]("mission_name", O.Length(255,varying=true))
    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column start_time SqlType(DATETIME) */
    val startTime: Rep[DateTime] = column[DateTime]("start_time")
    /** Database column end_time SqlType(DATETIME), Default(None) */
    val endTime: Rep[Option[DateTime]] = column[Option[DateTime]]("end_time", O.Default(None))
    /** Database column state SqlType(VARCHAR), Length(255,true) */
    val state: Rep[String] = column[String]("state", O.Length(255,varying=true))
  }
  /** Collection-like TableQuery object for table AdjustMission */
  lazy val AdjustMission = new TableQuery(tag => new AdjustMission(tag))

  /** Entity class storing rows of table Kit
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(255,true)
   *  @param createTime Database column create_time SqlType(DATETIME) */
  case class KitRow(id: Int, name: String, createTime: DateTime)
  /** GetResult implicit for fetching KitRow objects using plain SQL queries */
  implicit def GetResultKitRow(implicit e0: GR[Int], e1: GR[String], e2: GR[DateTime]): GR[KitRow] = GR{
    prs => import prs._
    KitRow.tupled((<<[Int], <<[String], <<[DateTime]))
  }
  /** Table description of table kit. Objects of this class serve as prototypes for rows in queries. */
  class Kit(_tableTag: Tag) extends profile.api.Table[KitRow](_tableTag, Some("product_tmbq"), "kit") {
    def * = (id, name, createTime) <> (KitRow.tupled, KitRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(createTime))).shaped.<>({r=>import r._; _1.map(_=> KitRow.tupled((_1.get, _2.get, _3.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(255,true) */
    val name: Rep[String] = column[String]("name", O.Length(255,varying=true))
    /** Database column create_time SqlType(DATETIME) */
    val createTime: Rep[DateTime] = column[DateTime]("create_time")
  }
  /** Collection-like TableQuery object for table Kit */
  lazy val Kit = new TableQuery(tag => new Kit(tag))

  /** Entity class storing rows of table Mission
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param missionName Database column mission_name SqlType(VARCHAR), Length(255,true)
   *  @param userId Database column user_id SqlType(INT)
   *  @param startTime Database column start_time SqlType(DATETIME)
   *  @param endTime Database column end_time SqlType(DATETIME), Default(None)
   *  @param state Database column state SqlType(VARCHAR), Length(255,true) */
  case class MissionRow(id: Int, missionName: String, userId: Int, startTime: DateTime, endTime: Option[DateTime] = None, state: String)
  /** GetResult implicit for fetching MissionRow objects using plain SQL queries */
  implicit def GetResultMissionRow(implicit e0: GR[Int], e1: GR[String], e2: GR[DateTime], e3: GR[Option[DateTime]]): GR[MissionRow] = GR{
    prs => import prs._
    MissionRow.tupled((<<[Int], <<[String], <<[Int], <<[DateTime], <<?[DateTime], <<[String]))
  }
  /** Table description of table mission. Objects of this class serve as prototypes for rows in queries. */
  class Mission(_tableTag: Tag) extends profile.api.Table[MissionRow](_tableTag, Some("product_tmbq"), "mission") {
    def * = (id, missionName, userId, startTime, endTime, state) <> (MissionRow.tupled, MissionRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(missionName), Rep.Some(userId), Rep.Some(startTime), endTime, Rep.Some(state))).shaped.<>({r=>import r._; _1.map(_=> MissionRow.tupled((_1.get, _2.get, _3.get, _4.get, _5, _6.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column mission_name SqlType(VARCHAR), Length(255,true) */
    val missionName: Rep[String] = column[String]("mission_name", O.Length(255,varying=true))
    /** Database column user_id SqlType(INT) */
    val userId: Rep[Int] = column[Int]("user_id")
    /** Database column start_time SqlType(DATETIME) */
    val startTime: Rep[DateTime] = column[DateTime]("start_time")
    /** Database column end_time SqlType(DATETIME), Default(None) */
    val endTime: Rep[Option[DateTime]] = column[Option[DateTime]]("end_time", O.Default(None))
    /** Database column state SqlType(VARCHAR), Length(255,true) */
    val state: Rep[String] = column[String]("state", O.Length(255,varying=true))
  }
  /** Collection-like TableQuery object for table Mission */
  lazy val Mission = new TableQuery(tag => new Mission(tag))

  /** Entity class storing rows of table Mode
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param test Database column test SqlType(VARCHAR), Length(255,true) */
  case class ModeRow(id: Int, test: String)
  /** GetResult implicit for fetching ModeRow objects using plain SQL queries */
  implicit def GetResultModeRow(implicit e0: GR[Int], e1: GR[String]): GR[ModeRow] = GR{
    prs => import prs._
    ModeRow.tupled((<<[Int], <<[String]))
  }
  /** Table description of table mode. Objects of this class serve as prototypes for rows in queries. */
  class Mode(_tableTag: Tag) extends profile.api.Table[ModeRow](_tableTag, Some("product_tmbq"), "mode") {
    def * = (id, test) <> (ModeRow.tupled, ModeRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(test))).shaped.<>({r=>import r._; _1.map(_=> ModeRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column test SqlType(VARCHAR), Length(255,true) */
    val test: Rep[String] = column[String]("test", O.Length(255,varying=true))
  }
  /** Collection-like TableQuery object for table Mode */
  lazy val Mode = new TableQuery(tag => new Mode(tag))

  /** Entity class storing rows of table User
   *  @param id Database column id SqlType(INT), AutoInc, PrimaryKey
   *  @param name Database column name SqlType(VARCHAR), Length(255,true)
   *  @param password Database column password SqlType(VARCHAR), Length(255,true)
   *  @param createTime Database column create_time SqlType(DATETIME) */
  case class UserRow(id: Int, name: String, password: String, createTime: DateTime)
  /** GetResult implicit for fetching UserRow objects using plain SQL queries */
  implicit def GetResultUserRow(implicit e0: GR[Int], e1: GR[String], e2: GR[DateTime]): GR[UserRow] = GR{
    prs => import prs._
    UserRow.tupled((<<[Int], <<[String], <<[String], <<[DateTime]))
  }
  /** Table description of table user. Objects of this class serve as prototypes for rows in queries. */
  class User(_tableTag: Tag) extends profile.api.Table[UserRow](_tableTag, Some("product_tmbq"), "user") {
    def * = (id, name, password, createTime) <> (UserRow.tupled, UserRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = ((Rep.Some(id), Rep.Some(name), Rep.Some(password), Rep.Some(createTime))).shaped.<>({r=>import r._; _1.map(_=> UserRow.tupled((_1.get, _2.get, _3.get, _4.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(INT), AutoInc, PrimaryKey */
    val id: Rep[Int] = column[Int]("id", O.AutoInc, O.PrimaryKey)
    /** Database column name SqlType(VARCHAR), Length(255,true) */
    val name: Rep[String] = column[String]("name", O.Length(255,varying=true))
    /** Database column password SqlType(VARCHAR), Length(255,true) */
    val password: Rep[String] = column[String]("password", O.Length(255,varying=true))
    /** Database column create_time SqlType(DATETIME) */
    val createTime: Rep[DateTime] = column[DateTime]("create_time")

    /** Uniqueness Index over (name) (database name name_uniq) */
    val index1 = index("name_uniq", name, unique=true)
  }
  /** Collection-like TableQuery object for table User */
  lazy val User = new TableQuery(tag => new User(tag))
}
