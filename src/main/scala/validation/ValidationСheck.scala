package validation

import validation.ValidationСheck.SeverityLevel

case class ValidationСheck(checkId: String, fields: Seq[String], severityLevel: SeverityLevel, message: Option[String])

object ValidationСheck {

  sealed trait SeverityLevel

  object SeverityLevel {
    case object Block extends SeverityLevel
    case object Error extends SeverityLevel
    case object Warning extends SeverityLevel
  }


}