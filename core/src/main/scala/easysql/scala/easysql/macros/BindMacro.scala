package easysql.macros

import scala.compiletime.erasedValue
import scala.deriving.*
import scala.language.unsafeNulls
import java.util.Date

inline def entityOffset[T]: Int =
    ${ entityOffsetMacro[T] }

inline def bindEntity[T](inline nextIndex: Int, data: Array[Any]): Option[T] =
    ${ bindEntityMacro[T]('nextIndex, 'data) }

inline def bindOffset[T]: Int = inline erasedValue[T] match {
    case _: Product => entityOffset[T]
    case _ => 1
}

inline def bindSingleton[T](nextIndex: Int, data: Array[Any]): Option[T] = {
    val bindValue = inline erasedValue[T] match {
        case _: BigDecimal =>
            if data(nextIndex) == null then None else Some(BigDecimal(data(nextIndex).toString()))
        case _: Product =>
            bindEntity[T](nextIndex, data)
        case _ => 
            if data(nextIndex) == null then None else Some(data(nextIndex))
    }
    bindValue.asInstanceOf[Option[T]]
}

inline def bind[T](nextIndex: Int, data: Array[Any]): T = {
    val bindValue = inline erasedValue[T] match {
        case _: *:[Option[x], t] => {
            bindSingleton[x](nextIndex, data) *: bind[t](nextIndex + bindOffset[x], data)
        }
        case _: EmptyTuple => EmptyTuple
        case _: Option[t] => bindSingleton[t](nextIndex, data)
    }
    bindValue.asInstanceOf[T]
}