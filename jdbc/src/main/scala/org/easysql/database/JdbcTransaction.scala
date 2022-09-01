package org.easysql.database

import org.easysql.query.ReviseQuery
import org.easysql.query.insert.Insert
import org.easysql.query.select.{Select, SelectQuery}
import org.easysql.jdbc.*
import org.easysql.dsl.{PK, TableSchema}

import java.sql.Connection
import scala.reflect.ClassTag

//class JdbcTransaction(db: DB, conn: Connection) extends DBTransaction(db) {
//    override def run(query: ReviseQuery): Int = jdbcExec(conn, query.sql(db))
//
//    override def runAndReturnKey(query: Insert[_, _]): List[Long] = jdbcExecReturnKey(conn, query.sql(db))
//
//    override def queryMap(query: SelectQuery[_]): List[Map[String, Any]] = jdbcQuery(conn, query.sql(db)).map(_.toMap)
//
//    override def queryTuple[T <: Tuple](query: SelectQuery[T]): List[T] = jdbcQuery(conn, query.sql(db)).map(it => Tuple.fromArray(it.map(_._2).toArray).asInstanceOf[T])
//
//    override def query[T <: TableEntity[_]](query: SelectQuery[_])(using t: TableSchema[T], ct: ClassTag[T]): List[T] = jdbcQuery(conn, query.sql(db)).map(it => bindData[T](it.toMap))
//
//    override def queryMap(sql: String): List[Map[String, Any]] = jdbcQuery(conn, sql).map(_.toMap)
//
//    override def findMap(query: Select[_]): Option[Map[String, Any]] = jdbcQuery(conn, query.sql(db)).headOption.map(_.toMap)
//
//    override def findTuple[T <: Tuple](query: Select[T]): Option[T] = jdbcQuery(conn, query.sql(db)).headOption.map(it => Tuple.fromArray(it.map(_._2).toArray).asInstanceOf[T])
//
//    override def find[T <: TableEntity[_]](pk: PK[T])(using t: TableSchema[T], ct: ClassTag[T]): Option[T] = jdbcQuery(conn, org.easysql.dsl.find[T](pk).sql(db)).headOption.map(it => bindData[T](it.toMap))
//
//    override def queryPageOfMap(query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[Map[String, Any]] =
//        page(query)(pageSize, pageNum, needCount)(it => it.toMap)
//
//    override def queryPageOfTuple[T <: Tuple](query: Select[T])(pageSize: Int, pageNum: Int, needCount: Boolean = true): Page[T] =
//        page(query)(pageSize, pageNum, needCount)(it => Tuple.fromArray(it.map(_._2).toArray).asInstanceOf[T])
//
//    inline def queryPage[T <: TableEntity[_]](query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean = true)(using t: TableSchema[T], ct: ClassTag[T]): Page[T] =
//        page(query)(pageSize, pageNum, needCount)(it => bindData[T](it.toMap))
//
//    override def fetchCount(query: Select[_]): Int = jdbcQueryCount(conn, query.countSql(db))
//
//    private def page[T](query: Select[_])(pageSize: Int, pageNum: Int, needCount: Boolean)(bind: List[(String, Any)] => T): Page[T] = {
//        val data = if (pageSize == 0) {
//            List[T]()
//        } else {
//            jdbcQuery(conn, query.pageSql(pageNum, pageNum)(db)).map(bind)
//        }
//
//        val count = if (needCount) {
//            fetchCount(query)
//        } else {
//            0
//        }
//
//        val totalPage = if (count == 0 || pageSize == 0) {
//            0
//        } else {
//            if (count % pageSize == 0) {
//                count / pageSize
//            } else {
//                count / pageSize + 1
//            }
//        }
//
//        new Page[T](totalPage, count, data)
//    }
//}
