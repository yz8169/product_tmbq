package test

import java.io.File

import implicits.Implicits._

/**
 * Created by Administrator on 2020/1/3
 */
object Test {

  def main(args: Array[String]): Unit = {
    val parent = new File("D:\\product_tmbq_database\\user\\10\\mission\\1273\\workspace")
    val file = new File(parent, "db_compound.xlsx")
    val outFile = new File(parent, "test.xlsx")
    file.xlsxLines().selectRemove("rt").toXlsxFile(outFile)
    val finalFile = new File(parent, "final.xlsx")
    val simpleFile = new File(parent, "compound_config.xlsx")
    simpleFile.xlsxLines().leftJoin(outFile.xlsxLines(), by = "compound").toXlsxFile(finalFile)

  }

}
