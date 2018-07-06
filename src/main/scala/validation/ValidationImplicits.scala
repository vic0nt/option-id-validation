package validation

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.{Id, Semigroup}
import validation.ValidationRules.{BusinessError, DomainError, Result}

object ValidationImplicits {

  implicit val optValidation: ValidationContext[Option] = new ValidationContext[Option] {
    override def withinContext[A](a: Option[A])(f: A => Result[A], checkId: String): Result[A] = a match {
      case Some(n) => f(n)
      case None => Validated.invalidNel[DomainError, A](BusinessError(checkId))
    }
  }

  implicit val idValidation: ValidationContext[Id] = new ValidationContext[Id] {
    override def withinContext[A](a: Id[A])(f: A => Result[A], checkId: String): Result[A] = f(a)
  }

  implicit class ValidatedOps[E : Semigroup, A](val v: Validated[E, A]) {

    /**
      * Takes two validated and returns first if both valid
      * combines them as semigroup instances otherwise
      */
    def ~(that: Validated[E, A]): Validated[E, A] =
      (v, that) match {
        case (Valid(a), Valid(_)) => Valid(a)
        case (Valid(_), i @ Invalid(_)) => i
        case (i @ Invalid(_), Valid(_)) => i
        case (Invalid(e1), Invalid(e2)) => Invalid(Semigroup[E].combine(e1, e2))
      }
  }

}
