package com.bonosludos

import akka.actor.ActorSystem
import com.bonosludos.model._
import org.json4s.JsonDSL._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object Main {

  import actors._
  import slick.jdbc.PostgresProfile.api._

  def main(args: Array[String]): Unit = {
    val db: Database = Database.forConfig("slick")

    println(Await.result(db.run(initDatabase().andThen(insertFixtures())), Duration.Inf))

    val system = ActorSystem("Universe")

    system.actorOf(Server.props("0.0.0.0", 1333), "server")

    Await.result(system.whenTerminated, Duration.Inf)
  }

  def initDatabase() = {
    DBIO.seq(
      Users.createTable,
      Figures.createTable,
    )
  }

  def insertFixtures() = {
    DBIO.seq(
      Figures.insertOrUpdate(Figure(None, seq2jvalue(List(1, 2, 3)))),
    )
  }
}
