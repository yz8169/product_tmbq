package tool

import play.api.data._
import play.api.data.Forms._
import tool.Pojo.{MissionData, MissionNameData, UserData}

/**
 * Created by yz on 2018/7/17
 */

class FormTool {

  case class AccountData(account: String, password: String)

  val accountForm = Form(
    mapping(
      "account" -> text,
      "password" -> text
    )(AccountData.apply)(AccountData.unapply)
  )

  case class userNameData(name: String)

  val nameForm = Form(
    mapping(
      "name" -> text
    )(userNameData.apply)(userNameData.unapply)
  )

  val userForm = Form(
    mapping(
      "name" -> text,
      "password" -> text
    )(UserData.apply)(UserData.unapply)
  )

  case class IdData(id: Int)

  val idForm = Form(
    mapping(
      "id" -> number
    )(IdData.apply)(IdData.unapply)
  )

  case class ChangePasswordData(password: String, newPassword: String)

  val changePasswordForm = Form(
    mapping(
      "password" -> text,
      "newPassword" -> text
    )(ChangePasswordData.apply)(ChangePasswordData.unapply)
  )

  val missionNameForm = Form(
    mapping(
      "missionName" -> text
    )(MissionNameData.apply)(MissionNameData.unapply)
  )

  val missionForm = Form(
    mapping(
      "missionName" -> text,
      "kitId" -> number,
      "rtCorrect" -> boolean,
    )(MissionData.apply)(MissionData.unapply)
  )

  case class AdjustMissionData(mission: String)

  val adjustMissionForm = Form(
    mapping(
      "mission" -> text,
    )(AdjustMissionData.apply)(AdjustMissionData.unapply)
  )

  case class MissionIdData(missionId: Int)

  val missionIdForm = Form(
    mapping(
      "missionId" -> number
    )(MissionIdData.apply)(MissionIdData.unapply)
  )

  case class MissionIdOptionData(missionId: Option[Int])

  val missionIdOptionForm = Form(
    mapping(
      "missionId" -> optional(number)
    )(MissionIdOptionData.apply)(MissionIdOptionData.unapply)
  )

  case class ArgsData(missionId: Int, compoundName: String)

  val argsForm = Form(
    mapping(
      "missionId" -> number,
      "compoundName" -> text,
    )(ArgsData.apply)(ArgsData.unapply)
  )

  case class ParamAdjustData(missionName: String, missionId: Int, compoundName: String, flMin: String, flMax: String, step: String, nups: String,
                             ndowns: String, snr: String, iteration: String, bLine: String, rtlw: String, rtrw: String,
                             rt: String, peakLocation: String)

  val paramAdjustForm = Form(
    mapping(
      "missionName" -> text,
      "missionId" -> number,
      "compoundName" -> text,
      "flMin" -> text,
      "flMax" -> text,
      "step" -> text,
      "nups" -> text,
      "ndowns" -> text,
      "snr" -> text,
      "iteration" -> text,
      "bLine" -> text,
      "rtlw" -> text,
      "rtrw" -> text,
      "rt" -> text,
      "peakLocation" -> text
    )(ParamAdjustData.apply)(ParamAdjustData.unapply)
  )

  case class UnitConversionData(unit: String, toUnit: String, fromC: Int)

  val unitConversionForm = Form(
    mapping(
      "unit" -> text,
      "toUnit" -> text,
      "fromC" -> number
    )(UnitConversionData.apply)(UnitConversionData.unapply)
  )

  case class FileNameData(fileName: String)

  val fileNameForm = Form(
    mapping(
      "fileName" -> text
    )(FileNameData.apply)(FileNameData.unapply)
  )

  case class FileNamesData(fileNames: Seq[String])

  val fileNamesForm = Form(
    mapping(
      "fileNames" -> seq(text)
    )(FileNamesData.apply)(FileNamesData.unapply)
  )

}
