package implicits

import java.io.File

import com.github.tototoshi.csv._
import implicits.Implicits._
import org.apache.commons.lang3.StringUtils

import scala.collection.SeqMap

/**
 * Created by Administrator on 2019/9/12
 */
trait AccVectorTool {

  implicit class AccVector[A, B, C](acc: (Vector[(A, B)], C)) {

    def accGroupMap = {
      val tmpAcc = SeqMap[A, Vector[B]]()
      acc._1.foldLeft(tmpAcc) { (inMap, t) =>
        val key = t._1
        val value = t._2
        if (inMap.contains(key)) {
          val values = inMap(key)
          inMap.updated(key, values :+ value)
        } else {
          inMap.updated(key, Vector(value))
        }
      }
    }

  }

}
