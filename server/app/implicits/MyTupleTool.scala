package implicits

import java.io.{File, FileInputStream}
import java.text.SimpleDateFormat

import implicits.Implicits._
import org.apache.commons.lang3.StringUtils
import org.apache.poi.ss.usermodel.{Cell, DateUtil}
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import scala.collection.SeqMap

/**
 * Created by Administrator on 2019/11/11
 */

trait MyTupleTool {

  implicit class MyTuple[A, B](t: List[(A, B)]) {

    def toSeqMap = {
      val tmpAcc = SeqMap[A, B]()
      t.foldLeft(tmpAcc) { (inMap, t) =>
        val key = t._1
        val value = t._2
        if (inMap.contains(key)) {
          val values = inMap(key)
          inMap.updated(key, value)
        } else {
          inMap.updated(key, value)
        }
      }
    }


  }


}


