package validation

import cats.Id

import scala.language.higherKinds

sealed trait Status[Wrapped[_]]
sealed trait Draft extends Status[Option]
sealed trait Valid extends Status[Id]
sealed trait Invalid extends Status[Id]

trait PaymentDocument[Wrapped[_], +S <: Status[Wrapped]] {
  def id: Int
  def documentInfo: DocumentInfo[Wrapped, S]
  def documentSum: Wrapped[BigDecimal]
}

case class DocumentInfo[Wrapped[_], +S <: Status[Wrapped]] (
  attaches: Wrapped[Seq[String]],
  attachExists: Wrapped[Boolean],
  createdByBank: Wrapped[Boolean],
  number: Wrapped[String]
)

object DocumentInfo {
  def asValid(
    attaches: Seq[String],
    attachExists: Boolean,
    createdByBank: Boolean,
    number: String
  ): DocumentInfo[Id, Valid] = apply[Id, Valid](attaches, attachExists, createdByBank, number)

  def asDraft(
    attaches: Option[Seq[String]],
    attachExists: Option[Boolean],
    createdByBank: Option[Boolean],
    number: Option[String]
  ): DocumentInfo[Option, Draft] = apply[Option, Draft](attaches, attachExists, createdByBank, number)
}

object PaymentDocument {
  private def apply[Wrapped[_], S <: Status[Wrapped]](
    _id: Int,
    _documentInfo: DocumentInfo[Wrapped, S],
    _documentSum: Wrapped[BigDecimal]
  ): PaymentDocument[Wrapped, S] = new PaymentDocument[Wrapped, S]() {
    def id: Int = _id
    def documentInfo: DocumentInfo[Wrapped, S] = _documentInfo
    def documentSum: Wrapped[BigDecimal] = _documentSum
  }

  def asValid(
    id: Int,
    documentInfo: DocumentInfo[Id, Valid],
    documentSum: BigDecimal
  ): PaymentDocument[Id, Valid] =
    apply[Id, Valid](
      id,
      documentInfo,
      documentSum
    )

  def asDraft(
    id: Int,
    documentInfo: DocumentInfo[Option, Draft],
    documentSum: Option[BigDecimal]
  ): PaymentDocument[Option, Draft] =
    apply[Option, Draft](
      id,
      documentInfo,
      documentSum
    )
}