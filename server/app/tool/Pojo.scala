package tool

import java.io.File

import dao.{ConfigDao, MissionDao}
import play.api.mvc.RequestHeader


/**
 * Created by Administrator on 2019/8/7
 */
object Pojo {

  case class MissionDirData(workspaceDir: File, resultDir: File, logFile: File)

  case class UserData(name: String, password: String)

  case class UnitData(unit: String)

  case class LoginUserData(name: String, password: String)

  case class ConfigNameData(configName: String)

  case class IdOpData(id: Option[Int])

  case class MissionIdData(missionId: Int)

  case class KeyData(key: String)

  case class ConfigFormData(missionName: String, configName: String)

  case class CommandData(workspace: File, command: List[String])

  case class IndexData(index: String, compoundName: String)

  trait ConfigFileT extends MyDir {
    val configFile: File
  }

  case class MyInputFile(rawDataFile: File, dataFile: File, groupFile: File, sampleColorFile: File, compareFile: File,
                         corDir: File, confounderFile: File, metaColorFile: File, configFile: File, solutionFile: Option[File]) extends MyDir with ConfigFileT

  case class MyInInput(groupFile: File, sampleColorFile: File, configFile: File, corDir: File, confounderFile: File,
                       dataFile: File, metaColorFile: File, scaleDataFile: File, mustScaleFile: File) extends MyDir with mustScaleFileT

  trait InDirTreatT extends MyDir {
    val inDirTreat: InDirTreat
  }

  trait mustScaleFileT extends MyDir {
    val mustScaleFile: File
  }

  trait InDirTreatCs extends MyDir {
    val csDir: File
  }

  trait InDirTreatSms extends MyDir {
    val smsDir: File
  }

  case class InDirTreat(csDir: File, smsDir: File) extends InDirTreatCs with InDirTreatSms

  case class MyResultDir(preDir: File, treatDir: File, inDirTreat: InDirTreat, qcDir: File, qcInDir: InDir) extends InDirTreatT

  case class MyWorkspaceDir(qcDir: File, qcInDir: InDir, treatDir: File, inDirTreat: InDirTreat) extends InDirTreatT

  case class MyInWorkspaceDir(dir0: File, basicDir: File, basicInDir: InDir, pcaDir: File, pcaInDir: InDir, plsdaDir: File,
                              oplsdaDir: File, uniDir: File, potentialDir: File, corDir: File, corInDir: InDir,
                              pathwayDir: File, pathwayInDir: InDir, diagnoseDir: File, diagnoseInDir: InDirDignose)

  case class MyInResultDir(basicDir: File, basicInDir: InDir, pcaDir: File, pcaInDir: InDir, plsdaDir: File, oplsdaDir: File,
                           uniDir: File, potentialDir: File, corDir: File, corInDir: InDir, pathwayDir: File, pathwayInDir: InDir,
                           diagnoseDir: File, diagnoseInDir: InDirDignose)

  case class MyDataDir(tmpDir: File, tmpDataDir: File, dataFile: File, sampleConfigExcelFile: File, compoundConfigFile: File)

  case class AdminMyDataDir(tmpDir: File, compoundConfigFile: File)

  case class DataFileInfo(sampleIds: List[String])

  case class MissionNameData(missionName: String)

  case class MissionData(missionName: String, kitId: Int, rtCorrect: Boolean)

  case class StdData(std7File: File, std8File: File, compoundFile: File,outFile:File)

  case class MyDao(missionDao: MissionDao, configDao: ConfigDao)

  trait InDir {
    val dir01: File = new File(".")
    val dir02: File = new File(".")
    val dir03: File = new File(".")
    val dir04: File = new File(".")
    val inDir04: InDir = null
    val dir05: File = new File(".")

  }

  trait MyDir

  case class InDir01(override val dir01: File) extends InDir

  case class InDir02(override val dir01: File, override val dir02: File) extends InDir

  case class InDir03(override val dir01: File, override val dir02: File, override val dir03: File) extends InDir

  case class InDir04(override val dir01: File, override val dir02: File,
                     override val dir03: File, override val dir04: File) extends InDir

  case class InDir05(override val dir01: File, override val dir02: File,
                     override val dir03: File, override val dir04: File, override val dir05: File) extends InDir

  trait rocDir extends MyDir {
    val rocDir: File
  }

  case class InDirMg(gbDir: File, lrDir: File, rfDIr: File, rocDir: File) extends MyDir with rocDir

  case class InDirDignose(rfDir: File, svmDir: File, borutaDir: File, mgDir: File, inDirMg: InDirMg)

  case class MyInDir04(override val dir01: File, override val dir02: File,
                       override val dir03: File, override val dir04: File, override val inDir04: InDir) extends InDir

  case class MyMessage(valid: Boolean, message: String)

  case class GroupFileInfo(metGroupMap: Map[String, String], pairIdMap: Map[String, String])

  case class CompareFileInfo(map: Map[List[String], Boolean])

  case class FileInfo(groupFileInfo: GroupFileInfo, dataFileInfo: DataFileInfo, compareFileInfo: CompareFileInfo)

  trait MyStep {
    val order: Int
  }

  case object StepBasic extends MyStep {
    override val order: Int = 1
  }

  case object StepPca extends MyStep {
    override val order: Int = 2
  }

  case object Step0 extends MyStep {
    override val order: Int = 0
  }

  case object StepPlsda extends MyStep {
    override val order: Int = 3
  }

  case class StepOplsda(isTest: Boolean = false) extends MyStep {
    override val order = 4
  }

  case class StepUni(isTest: Boolean = false) extends MyStep {
    override val order: Int = 5
  }

  case class StepPotential(isTest: Boolean = false) extends MyStep {
    override val order: Int = 6
  }

  case object StepCor extends MyStep {
    override val order: Int = 7
  }

  case class StepPathway(isTest: Boolean = false) extends MyStep {
    override val order: Int = 8
  }

  case object StepDignose extends MyStep {
    override val order: Int = 9
  }


  trait MyConfig {

  }

  case class DiffMethodData(method: String, fcMethod: String, mulMethod: String, pValue: String, fdr: String, log2FC: String, kwP: String,
                            kwFdr: String) extends MyConfig

  case class PathwayData(isIPathExec: String, isEnrichExec: String, libTypes: Seq[String], isPathwayExec: String,
                         method: String, nodeImp: String) extends MyConfig

  case class OplsdaData(vip: String, q2Y: String, q2YB: String) extends MyConfig

  case class CorData(isCorExec: String, isParCorExec: String, coe: String, p: String, fdr: String) extends MyConfig

  case class ReportHomeData(isTargetTest: String, client: Option[String], affiliation: Option[String],
                            email: Option[String], projectCode: Option[String], salesRep: Option[String],
                            testOrderName: Option[String], testOrderId: Option[String],
                            sampleType: Option[String]) extends MyConfig

  case class PreDealData(replaceMethod: String, knn: String, min: String) extends MyConfig

  case class ConfigData(threadNum: Int, isInter: String, rfTop: String,
                        svmTop: String, cor: CorData, pathway: PathwayData, species: String, isSmp: String, anaKind: String,
                        diagnoseIsExec: String, coef: String, isLoess: String, isNormal: String, preDeal: PreDealData, diffMethod: DiffMethodData,
                        oplsda: OplsdaData, borutaIsInter: String, borutaBeforeTopN: String, borutaAfterTopN: String,
                        reportHome: ReportHomeData,
                        colorSolution: Option[String]
                       )

  case class KeggInfoData(name: String, accession: String, keggid: String, superclass: String, cleanName: String)


}
