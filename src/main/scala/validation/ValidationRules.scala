package validation

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZonedDateTime}

import cats.Id
import cats.data._
import cats.implicits._
import validation.ValidationContext

import scala.language.higherKinds

class ValidationRules[Wrapped[_] : ValidationContext] {

  import ValidationContext._
  import ValidationRules._

/*  def all(doc: PaymentDocument[Wrapped, Status[Wrapped]]): Result[PaymentDocument[Id, Valid]] =
    (
      doc.id.valid,
      doc.documentInfo.valid,
      doc.documentSum.withinContext(_.valid, "documentSum")
    ).mapN(PaymentDocument.asValid _)*/

  object Common {

    object Number {
      def all(doc: PaymentDocument[Wrapped, Status[Wrapped]]): Result[String] =
        numberExists(doc.documentInfo.number)
          .andThen(numberLength)
          .andThen(numberContent)
          .andThen(numberStartSymbols)
          .andThen(numberZeroesOnly)
          .andThen(numberContainsSpaces)

      def numberExists(num: Wrapped[String]): Result[String] = num.withinContext(_.valid, "1.1.1")
      def numberLength(num: String): Result[String] =
        if (num.length < 7) num.valid else BusinessError("1.1.2", List(num)).invalidNel
      def numberContent(num: String): Result[String] =
        if (num.matches("-?[0-9]+")) num.valid else BusinessError("1.1.3").invalidNel
      def numberStartSymbols(num: String): Result[String] =
        if (!num.startsWith("0")) num.valid else BusinessError("1.1.4").invalidNel
      def numberZeroesOnly(num: String): Result[String] =
        if (!num.matches("0+")) num.valid else BusinessError("1.1.5").invalidNel
      def numberContainsSpaces(num: String): Result[String] =
        if (!num.contains(" ")) num.valid else BusinessError("1.1.6").invalidNel
    }

    object PaymentPriority {
    }
  }

  object Budget {
    val all = List.empty
  }

}

object ValidationRules {

  type Result[T] = ValidatedNel[DomainError, T]

  sealed trait DomainError
  case class BusinessError(checkId: String, params: List[String] = List.empty) extends DomainError

  val df = DateTimeFormatter.ofPattern("dd.MM.yyyy")
  implicit def zdfToLocal(zdf: ZonedDateTime): LocalDate = zdf.toLocalDate

}
