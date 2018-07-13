package validation2

import java.time.ZonedDateTime

sealed trait PaymentDocument

case class PaymentDocumentValid(id: Int, documentInfo: DocumentInfo, documentSum: BigDecimal) extends PaymentDocument
case class PaymentDocumentDraft(id: Int, documentInfo: DocumentInfo, documentSum: Option[BigDecimal])
  extends PaymentDocument

case class DocumentInfo(attaches: Seq[String], number: String, date: ZonedDateTime)
case class DocumentInfoD(attaches: Seq[String], number: Option[String], date: Option[ZonedDateTime])

