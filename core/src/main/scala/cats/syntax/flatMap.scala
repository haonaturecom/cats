package cats
package syntax

trait FlatMapSyntax extends FlatMap.ToFlatMapOps {

  implicit final def catsSyntaxFlatten[F[_]: FlatMap, A](ffa: F[F[A]]): FlattenOps[F, A] =
    new FlattenOps[F, A](ffa)

  implicit final def catsSyntaxIfM[F[_]: FlatMap](fa: F[Boolean]): IfMOps[F] =
    new IfMOps[F](fa)

  implicit final def catsSyntaxFlatMapIdOps[A](a: A): FlatMapIdOps[A] =
    new FlatMapIdOps[A](a)
}

final class FlattenOps[F[_], A](val ffa: F[F[A]]) extends AnyVal {

  /**
   * Flatten nested `F` values.
   *
   * Example:
   * {{{
   * scala> import cats.implicits._
   * scala> type ErrorOr[A] = Either[String, A]
   * scala> val x: ErrorOr[ErrorOr[Int]] = Right(Right(3))
   * scala> x.flatten
   * res0: ErrorOr[Int] = Right(3)
   * }}}
   */
  def flatten(implicit F: FlatMap[F]): F[A] = F.flatten(ffa)
}

final class IfMOps[F[_]](val fa: F[Boolean]) extends AnyVal {

  /**
   * A conditional lifted into the `F` context.
   *
   * Example:
   * {{{
   * scala> import cats.{Eval, Now}
   * scala> import cats.implicits._
   *
   * scala> val b1: Eval[Boolean] = Now(true)
   * scala> val asInt1: Eval[Int] = b1.ifM(Now(1), Now(0))
   * scala> asInt1.value
   * res0: Int = 1
   *
   * scala> val b2: Eval[Boolean] = Now(false)
   * scala> val asInt2: Eval[Int] = b2.ifM(Now(1), Now(0))
   * scala> asInt2.value
   * res1: Int = 0
   * }}}
   */
  def ifM[B](ifTrue: => F[B], ifFalse: => F[B])(implicit F: FlatMap[F]): F[B] = F.ifM(fa)(ifTrue, ifFalse)
}


final class FlatMapIdOps[A](val a: A) extends AnyVal {

  /**
   * Example:
   * {{{
   * scala> import cats.implicits._
   *
   * scala> val a: Int = 10
   * scala> a.tailRecM[Option,String](i => if (i == 20) Some(Right("done")) else Some(Left(i+1)))
   * res0: Option[String] = Some(done)
   *
   *}}}
   */
  def tailRecM[F[_], B](f: A => F[Either[A, B]])(implicit F: FlatMap[F]): F[B] = F.tailRecM(a)(f)
}
