package jekytrum

import scala.reflect.runtime.universe._

object Util {
  def getType[T: TypeTag](obj: T) = typeOf[T]
}