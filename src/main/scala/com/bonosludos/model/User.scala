package com.bonosludos.model

import com.bonosludos.CustomPostgresProfile.api._
import slick.lifted.Tag

case class User(id: Option[Int], externalId: String, name: String)

class Users(tag: Tag) extends Table[User](tag, "users") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def externalId = column[String]("externalId")
  def name = column[String]("name")

  def * = (id.?, externalId, name) <> (User.tupled, User.unapply)
}

object Users extends TableQuery(new Users(_)) {
  val findByExternalId = this.findBy(_.externalId)
  val createTable = this.schema.create
}