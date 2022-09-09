package org.easysql.bind

import org.easysql.dsl.{TableSchema, PrimaryKeyColumnExpr, TableColumnExpr}

import java.util.Date
import java.sql.ResultSet
import scala.quoted.*

def bindEntityMacroImpl[T](using q: Quotes, tpe: Type[T]): Expr[Map[String, Any] => T] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    val tpr = TypeRepr.of[T]
    val ctor = tpr.typeSymbol.primaryConstructor
    val fields = sym.declaredFields
    val bindFunctionExprs = fields map { f =>
        var key = Expr(f.name)
        if (f.annotations.size > 0) {
            val annotation = f.annotations.head
            annotation match {
                case Apply(Select(New(TypeIdent(name)), _), args) =>
                    if (name == "PrimaryKey" || name == "IncrKey" || name == "Column") {
                        args match {
                            case Literal(v) :: Nil => key = Expr(v.value.toString())
                            case _ =>
                        }
                    }
            }
        }

        f.tree match {
            case vd: ValDef => {
                val vdt = vd.tpt.tpe.asType
                vdt match {
                    case '[Int] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[Int])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, 0).asInstanceOf[Int] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => Int]
                    }
                    case '[String] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[String])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, "").asInstanceOf[String] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => String]
                    }
                    case '[Long] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[Long])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, 0l).asInstanceOf[Long] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => Long]
                    }
                    case '[Float] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[Float])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, 0f).asInstanceOf[Float] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => Float]
                    }
                    case '[Double] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[Double])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, 0d).asInstanceOf[Double] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => Double]
                    }
                    case '[BigDecimal] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[BigDecimal])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, BigDecimal(0)).asInstanceOf[BigDecimal] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => BigDecimal]
                    }
                    case '[Boolean] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[Boolean])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, false).asInstanceOf[Boolean] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => Boolean]
                    }
                    case '[Date] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[Date])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, Date()).asInstanceOf[Date] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => Date]
                    }
                    case '[t] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Map[String, Any]]), _ => TypeRepr.of[t])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Map[String, Any]]
                            '{ $x.getOrElse($key, null).asInstanceOf[t] }.asTerm
                        }
                        Lambda(f, mtpe, rhsFn).asExprOf[Map[String, Any] => t]
                    }
                }
            }
        }
    }

    '{
        (map: Map[String, Any]) =>
            ${
                val terms = bindFunctionExprs.map { f => 
                    '{ $f.apply(map) }.asTerm 
                }
                New(Inferred(tpr)).select(ctor).appliedToArgs(terms).asExprOf[T]
            }
    }
}

def bindQueryMacroImpl[T](nextIndex: Expr[Int])(using q: Quotes, tpe: Type[T]): Expr[(Int, Array[Any] => T)] = {
    import q.reflect.*

    val sym = TypeTree.of[T].symbol
    val tpr = TypeRepr.of[T]
    val ctor = tpr.typeSymbol.primaryConstructor
    val fields = sym.declaredFields
    var i = 0
    val bindFunctionExprs = fields map { f =>
        f.tree match {
            case vd: ValDef => {
                val vdt = vd.tpt.tpe.asType
                vdt match {
                    case '[t] => {
                        val mtpe = MethodType(List("x"))(_ => List(TypeRepr.of[Array[Any]]), _ => TypeRepr.of[t])
                        def rhsFn(sym: Symbol, paramRefs: List[Tree]) = {
                            val x = paramRefs.head.asExprOf[Array[Any]]
                            val offset = Expr(i)
                            '{ $x($nextIndex + $offset).asInstanceOf[t] }.asTerm
                        }
                        val lambda = Lambda(f, mtpe, rhsFn).asExprOf[Array[Any] => t]
                        i = i + 1
                        lambda
                    }
                }
            }
        }
    }

    val next = Expr(i)

    '{
        val bindFunc = (result: Array[Any]) =>
            ${
                val terms = bindFunctionExprs.map { f => 
                    '{ $f.apply(result) }.asTerm 
                }
                New(Inferred(tpr)).select(ctor).appliedToArgs(terms).asExprOf[T]
            }

        $next -> bindFunc
    }
}