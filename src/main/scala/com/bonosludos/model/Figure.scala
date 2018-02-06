package com.bonosludos.model

import com.bonosludos.CustomPostgresProfile.api._
import org.json4s.JValue
import slick.dbio.Effect
import slick.lifted.Tag

case class Figure(id: Option[Int], descriptor: JValue)

class Figures(tag: Tag) extends Table[Figure](tag, "figures") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def descriptor = column[JValue]("descriptor")

  def * = (id.?, descriptor) <> (Figure.tupled, Figure.unapply)
}

object Figures extends TableQuery(new Figures(_)) {
  val findById = this.findBy(_.id)
  val createTable: DBIOAction[Unit, NoStream, Effect.Schema] = this.schema.create
}