package implicits

/**
 * Created by Administrator on 2019/11/13
 */
trait MyListTool {

  implicit class MyList[T](list: List[T]) {

    def distinctBy[B](f: T => B) = {
      list.map { x =>
        (f(x), x)
      }.toMap.values.toList
    }

    def distinctByKeepHead[B](f: T => B): List[T] = {

      def loop(list: List[T], acc: (List[T], Set[B])): (List[T], Set[B]) = {
        list match {
          case Nil => acc
          case x :: xs =>
            val key = f(x)
            val (list, set) = acc
            if (!set.contains(key)) {
              loop(xs, (list ::: List(x), set + (key)))
            } else loop(xs, acc)
        }
      }

      loop(list, (List[T](), Set[B]()))._1
    }

  }


}
