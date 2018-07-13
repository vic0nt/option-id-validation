package fsm

import akka.actor._
import akka.persistence.fsm.PersistentFSM
import akka.persistence.fsm.PersistentFSM._
import akka.persistence.journal.{Tagged, WriteEventAdapter}
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import Transaction._
import TransactionProtocol.StartTransaction

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.control.NonFatal

class TransactionTaggingEventAdapter extends WriteEventAdapter {
  def manifest(event: Any): String = ""

  def toJournal(event: Any): Any = event match {
    case Transaction.TransactionCompleted(invoice, _) =>
      Tagged(event, Set("transaction", s"user-${invoice.from}", s"user-${invoice.to}"))
    case _ => event
  }
}

object Transaction {

  case class Invoice(from: String, to: String, amount: Long, currency: String)

  sealed trait DomainEvent
  case class TransactionStarted(invoice: Invoice) extends DomainEvent
  case class TransactionCompleted(invoice: Invoice, completedAt: Long) extends DomainEvent
  case class TransactionRejected(reason: String) extends DomainEvent
  case object TransactionRestarted extends DomainEvent

  sealed trait TransactionPayload
  case class EmptyTransaction() extends TransactionPayload
  case class RunningTransaction(invoice: Invoice) extends TransactionPayload
  case class CompletedTransaction(invoice: Invoice) extends TransactionPayload
  case class RejectedTransaction(invoice: Invoice, reason: String, failedAt: Long) extends TransactionPayload

  sealed trait TransactionState extends FSMState

  case object New extends TransactionState { def identifier: String = "new" }
  case object Running extends TransactionState { def identifier: String = "running" }
  case object Done extends TransactionState { def identifier: String = "done" }
  case object Failed extends TransactionState { def identifier: String = "failed" }

  def props(id: String) = Props(new Transaction(id))
}

object TransactionProtocol {

  sealed trait Response
  case object Ok
  case object Error

  sealed trait Command
  case class StartTransaction(from: String, to: String, amount: Long, currency: String) extends Command
  case object CompleteTransaction extends Command
  case class RejectTransaction(reason: String) extends Command
  case object RestartTransaction extends Command

}

class Transaction(id: String) extends PersistentFSM[TransactionState, TransactionPayload, DomainEvent] {

  import TransactionProtocol._

  implicit def domainEventClassTag: ClassTag[DomainEvent] = ClassTag(classOf[DomainEvent])

  implicit val ec = context.system.dispatcher

  def persistenceId: String = s"Transaction-$id"

  startWith(New, EmptyTransaction())

  when(New) {
    case Event(c: StartTransaction, _) =>
      goto(Running) applying TransactionStarted(Invoice(c.from, c.to, c.amount, c.currency)) andThen {
        case RunningTransaction(invoice) =>
          pay(invoice.from, invoice.to, invoice.amount, invoice.currency)
            .map { _ =>
              self ! CompleteTransaction
              sender ! Ok
            }
            .recover {
              case NonFatal(cause) =>
                log.error("[Transaction #{}] Cannot complete transaction.", id)
                self ! RejectTransaction
                sender ! Error
            }
      }
  }

  when(Running) {
    case Event(CompleteTransaction, RunningTransaction(invoice)) =>
      goto(Done) applying TransactionCompleted(invoice, time) andThen (_ => stop())
    case Event(r: RejectTransaction, _) => goto(Failed) applying TransactionRejected(r.reason)
  }

  when(Done)(FSM.NullFunction)

  when(Failed) {
    case Event(RestartTransaction, _) => goto(Running) applying TransactionRestarted
  }

  def applyEvent(event: DomainEvent, oldData: TransactionPayload): TransactionPayload = {
    val newData = (event, oldData) match {
      case (event: TransactionStarted, _) => RunningTransaction(event.invoice)
      case (TransactionCompleted(invoice, _), state: RunningTransaction) => CompletedTransaction(invoice)
      case (event: TransactionRejected, state: RunningTransaction) =>
        RejectedTransaction(state.invoice, event.reason, time)
      case (TransactionRestarted, state: RejectedTransaction) => RunningTransaction(state.invoice)
    }

    log.info("[Transaction #{}]Event received.\nEvent: {}\nState: {}\nNew state: {}", id, event, oldData, newData)

    newData
  }

  private def time = System.currentTimeMillis()

  private def pay(from: String, to: String, amount: Long, currency: String): Future[Unit] = Future {
    log.info("Sending payment #{} to external system.", id)
    Thread.sleep(1000)
    if (id.hashCode % 100 == 0) {
      throw new Exception("Payment service unavailable")
    }
    log.info("Payment #{} complete.", id)
  }
}

object Main extends App {
  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val readJournal = PersistenceQuery(system).readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)
  readJournal
    .eventsByTag("transaction")
    .map(_.event)
    .collect {
      case Transaction.TransactionCompleted(invoice, _) => invoice
    }
    .map { invoice =>
      println(s"Invoice ${invoice.amount} ${invoice.currency} finished")
    }
    .runWith(Sink.ignore)

  val txs = for {
    id <- -100 to 0
  } yield system.actorOf(Transaction.props(id.toString))

  txs.foreach(_ ! StartTransaction("A", "B", 100, "RUB"))
}
