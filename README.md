# scala-commons
Common utilities we use in our Scala projects

# FutureOr
## Motivation
We have a lot of experience building [Reactive](https://www.lightbend.com/community/core-projects/scala) applications in Scala, so we work with `Future`s a lot, specifically when interacting with third-party APIs and non-blocking web services.  We also try to gracefully handle success and failure, and we've found that [Scalactic's Or](http://www.scalactic.org/user_guide/OrAndEvery) does a great job in that regard.  Consequently, we often have to execute and compose several calls where the return type is `Future[Good Or Bad]`.  Out of the box, this doesn't compose very gracefully.  So we made a `FutureOr`.

`FutureOr` is a simple wrapper over the type `Future[T Or One[E]]`.  It provides some basic monadic functions and helpers to make working with `Future[T Or One[E]]` much easier.  Scalactic provides a nice type alias `ErrorMessage` to use for `E` to indicate errors, but we usually end up constructing our own error objects to make it easier to manage internationalization and API error statuses. 

## Examples
### Simple composition of services returning a `Future[T Or One[E]]`
    for {
      int1 <- FutureOr(service1.call())
      int2 <- FutureOr(service2.call())
    } yield {
      int1 + int2
    }

### From a sequence of `Future[T Or One[E]]`, create a future sequence and accumulate all of the `Bad` paths
    val a: Future[Int Or One[E]] = serviceA.call();
    val b: Future[Int Or One[E]] = serviceB.call();
    val c: Future[Int Or One[E]] = serviceC.call();

    val ints: Seq[Future[Int Or Every[E]]] = FutureOr.sequence(Seq(a, b, c))

