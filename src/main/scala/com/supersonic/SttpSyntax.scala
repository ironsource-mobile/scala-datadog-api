package com.supersonic

import com.softwaremill.sttp.MonadError

/** Provides syntax for values that have an instance of [[com.softwaremill.sttp.MonadError]] */
object SttpSyntax {
  implicit class MonadSyntax[F[_], A](val fa: F[A]) extends AnyVal {
    def map[B](f: A => B)(implicit monad: MonadError[F]): F[B] = monad.map(fa)(f)
    def flatMap[B](f: A => F[B])(implicit monad: MonadError[F]): F[B] = monad.flatMap(fa)(f)
  }
}
