package com.chariotsolutions.commons

import org.scalactic._
import Accumulation._

import scala.collection.GenTraversableOnce
import scala.collection.generic.CanBuildFrom
import scala.concurrent.{ExecutionContext, Future}

import scala.languageFeature.higherKinds

case class FutureOr[+A, +E](future: Future[A Or E]) extends AnyVal {

  def flatMap[B, F >: E](f: A => FutureOr[B, F])(implicit ec: ExecutionContext): FutureOr[B, F] = {
    val newFuture = future.flatMap{
      case Good(a) => f(a).future
      case Bad(err) => Future.successful(Bad(err))
    }
    FutureOr(newFuture)
  }

  def map[B](f: A => B)(implicit ec: ExecutionContext): FutureOr[B, E] = FutureOr(future.map(or => or map f))

  def filter[F >: E](f: A => Validation[F])(implicit ec: ExecutionContext): FutureOr[A, F] = FutureOr {
    future.map { or =>
      or.filter(f)
    }
  }

  final def withFilter[F >: E](p: A => Validation[F])(implicit ec: ExecutionContext): FutureOr[A, F] = filter(p)(ec)
}

object FutureOr {

  def successful[A, E](or: A Or E): FutureOr[A, E] = FutureOr(Future.successful(or))

  def good[A, E](a: A): FutureOr[A, E] = FutureOr(Future.successful(Good(a)))

  def bad[A, E](e: E): FutureOr[A, E] = FutureOr(Future.successful(Bad(e)))

  def sequence[A, E, M[+X] <: TraversableOnce[X] with GenTraversableOnce[X]](in: M[Future[A Or Every[E]]])
  (implicit ec: ExecutionContext,
  cbfForFutureSequence: CanBuildFrom[M[Future[A Or Every[E]]], A Or Every[E], M[A Or Every[E]]],
  cbfForCombinable: CanBuildFrom[M[A Or Every[E]], A, M[A]]) = {

    val seq: Future[M[A Or Every[E]]] = Future.sequence(in)
    val combined: Future[M[A] Or Every[E]] = seq.map(_.combined)
    FutureOr(combined)
  }
}