package implicits

import scala.collection.SeqMap

/**
 * Created by Administrator on 2019/9/12
 */
trait MyVectorTool {

  implicit class MyVector[A](acc: Vector[A]) {

    def orderGroupBy[K](f: A => K) = {
      val tmpAcc = SeqMap[K, Vector[A]]()
      acc.foldLeft(tmpAcc) { (inMap, x) =>
        val key = f(x)
        val value = x
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
