package com.bonosludos.actors

import akka.actor.Actor
import akka.event.LoggingAdapter
import com.bonosludos.Event
import com.bonosludos.Error
import com.bonosludos.Event.ExceptionRaised

trait ActorCustomLogging { this: Actor ⇒
  private var _log: LoggingAdapter = _

  private def log: LoggingAdapter = {
    // only used in Actor, i.e. thread safe
    if (_log eq null)
      _log = akka.event.Logging(context.system, this)
    _log
  }

  implicit val errorConverter: Throwable => Event = (error: Throwable) => ExceptionRaised(error)

  // these methods may be refactored in order to eliminate toString usage
  def debug(event: Event): Unit = log.debug(event.toString)
  def info(event: Event): Unit = log.info(event.toString)
  def warning(event: Event): Unit = log.warning(event.toString)
  def error(event: Event): Unit = log.error(event.toString)
}
