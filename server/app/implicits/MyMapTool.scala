package implicits

import java.io.File

import com.github.tototoshi.csv._

import scala.collection.SeqMap

/**
 * Created by Administrator on 2019/9/12
 */
trait MyMapTool {

  implicit class MyMap(map: Map[String, String]) {

    def keyList = {
      map.keys.toList
    }

    def reverses = {
      map.groupBy(_._2).mapValues(x => x.keyList)
    }

    def valuesList = {
      map.values.toList
    }


  }


}
