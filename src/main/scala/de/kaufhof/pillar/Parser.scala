package de.kaufhof.pillar

import java.io.InputStream
import java.util.Date

import scala.collection.mutable
import scala.io.Source

object Parser {
  def apply(): Parser = new Parser

  private val MatchAttribute = """^-- (authoredAt|description|up|down|stage):(.*)$""".r
}

class PartialMigration {
  var description: String = ""
  var authoredAt: String = ""

  var upStages: mutable.Buffer[String] = mutable.Buffer.empty[String]
  var downStages : Option[mutable.Buffer[String]] = None

  var currentUp: mutable.Buffer[String] = mutable.Buffer.empty[String]
  var currentDown: Option[mutable.Buffer[String]] = None

  def rotateUp(): Unit = {
    upStages += currentUp.mkString("\n")
    upStages = upStages.filterNot(line => line.isEmpty)
    currentUp = mutable.Buffer.empty[String]
  }

  def rotateDown(): Unit = {

    currentDown match {
      case Some(currentDownLines) =>
        downStages match {
          case None => downStages = Some(mutable.Buffer.empty[String])
          case Some(_) => //do nothing
        }

        downStages = Some(downStages.get += currentDownLines.mkString("\n"))

      case None => //do nothing
    }

    currentDown = None
  }

  def validate: Option[Map[String, String]] = {

    rotateUp()
    rotateDown()

    val errors = mutable.Map[String, String]()

    if (description.isEmpty) errors("description") = "must be present"
    if (authoredAt.isEmpty) errors("authoredAt") = "must be present"
    if (!authoredAt.isEmpty && authoredAtAsLong < 1) errors("authoredAt") = "must be a number greater than zero"
    if (upStages.isEmpty) errors("up") = "must be present"

    if (errors.nonEmpty) Some(errors.toMap) else None
  }

  def authoredAtAsLong: Long = {
    try {
      authoredAt.toLong
    } catch {
      case _:NumberFormatException => -1
    }
  }

}

class Parser {

  import Parser.MatchAttribute

  trait ParserState

  case object ParsingAttributes extends ParserState

  case object ParsingUp extends ParserState

  case object ParsingDown extends ParserState

  case object ParsingUpStage extends ParserState

  case object ParsingDownStage extends ParserState

  def parse(resource: InputStream): Migration = {
    val inProgress = new PartialMigration
    var state: ParserState = ParsingAttributes
    Source.fromInputStream(resource).getLines().foreach {
      case MatchAttribute("authoredAt", authoredAt)   =>
        inProgress.authoredAt = authoredAt.trim
      case MatchAttribute("description", description) =>
        inProgress.description = description.trim
      case MatchAttribute("up", _)                    =>
        state = ParsingUp
      case MatchAttribute("down", _)                  =>
        inProgress.rotateUp()
        inProgress.currentDown = Some(mutable.Buffer.empty[String])
        state = ParsingDown
      case MatchAttribute("stage", number@_)          =>
        state match {
          case ParsingUp        => state = ParsingUpStage
          case ParsingUpStage   => inProgress.rotateUp()
          case ParsingDown      => state = ParsingDownStage
          case ParsingDownStage => inProgress.rotateDown(); inProgress.currentDown = Some(mutable.Buffer.empty[String])
        }
      case cql                                        =>
        if (!cql.isEmpty) {

          state match {
            case ParsingUp | ParsingUpStage     => inProgress.currentUp += cql
            case ParsingDown | ParsingDownStage => inProgress.currentDown.get += cql
            case other@_                        => // ignored
          }
        }
    }
    inProgress.validate match {
      case Some(errors) => throw new InvalidMigrationException(errors)
      case None =>

        inProgress.downStages match {
          case Some(downLines) =>
            if (downLines.forall(_.isEmpty)) {
              Migration(inProgress.description, new Date(inProgress.authoredAtAsLong), inProgress.upStages.toSeq, None)
            } else {
              Migration(inProgress.description, new Date(inProgress.authoredAtAsLong), inProgress.upStages.toSeq, Some(downLines.toSeq))
            }
          case None => Migration(inProgress.description, new Date(inProgress.authoredAtAsLong), inProgress.upStages.toSeq)
        }
    }
  }
}
