package validation

import validation.ValidationRules.Result

import scala.language.higherKinds

trait ValidationContext[F[_]] {
  def withinContext[T](a: F[T])(f: T => Result[T], checkId: String): Result[T]
}

object ValidationContext {
  implicit class ValidationOps[F[_],  T](val v: F[T]) extends AnyVal {
    def withinContext(f: T => Result[T], checkId: String)(implicit ctx: ValidationContext[F]): Result[T] =
      ctx.withinContext[T](v)(f, checkId)
  }
}
