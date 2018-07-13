import java.time.ZonedDateTime

import cats.data.Validated
import cats.implicits._
import henkan.optional.all._


sealed trait PaymentDocument

case class PaymentDocumentValid(id: Int, documentInfo: DocumentInfo, documentSum: BigDecimal) extends PaymentDocument
case class PaymentDocumentDraft(id: Int, documentInfo: Option[DocumentInfo], documentSum: Option[BigDecimal])
  extends PaymentDocument

case class DocumentInfo(attaches: Seq[String], number: String, date: ZonedDateTime)
case class DocumentInfoD(attaches: Seq[String], number: Option[String], date: Option[ZonedDateTime])

validate(DocumentInfoD(Seq.empty, Some("123"), Some(ZonedDateTime.now()))).to[DocumentInfo]

validate(DocumentInfoD(Seq.empty, None, Some(ZonedDateTime.now()))).to[DocumentInfo]

validate(PaymentDocumentDraft(
  1,
  //DocumentInfoD(Seq.empty, None, Some(ZonedDateTime.now())),
  Some(DocumentInfo(Seq.empty, "123", ZonedDateTime.now())),
  Some(200)
)).to[PaymentDocumentValid]