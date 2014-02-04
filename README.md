# vertx-call-orchestration
Set of classes aimed at making the handling of asynchronous calls on the vert.x event bus easier to implement and maintain.

## No promises...
One way of handling callbacks is through the use of promises, which is a fine idea for languages with function objects
[like JavaScript](http://blog.mediumequalsmessage.com/promise-deferred-objects-in-javascript-pt1-theory-and-semantics).

Nevertheless, "[promises are not *about* callback aggregation](http://domenic.me/2012/10/14/youre-missing-the-point-of-promises/)".
So to that extent, having a fully functional promises API is not required to cope with callback hell. And possibly even **undesirable** in Java,
as the language doesn't support functions as first-class citizens.

## No RxJava...
Another often mentioned approach to dealing with *callback hell* is use of the RxJava library. Although the idea of expanding the Observer and Iterator patterns is a great idea,
the focus of the library is on "[composing flows and sequences of asynchronous data](https://github.com/Netflix/RxJava/wiki)".

If your application's intent *is* the use of streams, this may be a good fit. For "simpler" usages (and possibly this goes for many applications,
mainly using "simple" calls with single return values), the added complexity/overhead seems unnecessary.

## ...then what?
The solution used in this project is inspired by (basic) [Workflow Control-Flow Patterns](http://www.workflowpatterns.com/patterns/control/index.php).

The general idea is that each call to another verticle can be reduced to answering three questions:

  * Which address should the call be made to?
  * What is the message that should be sent?
  * How should the (possibly exceptional) result be treated?

Each call can be encapsulated as a separate object, which answers all of the (applicable) questions.
These objects then can be orchestrated following the [control-flow patterns](./patterns.md).

To make the API as user-friendly as possible, it was made fluent and includes a number of static methods as part of a Builder pattern.

To see how this works, please have a look at the [unit test](./src/test/java/nl/dechateau/vertx/orchestration/OrchestrationTest.java).

## Are all workflow patterns accounted for?
No, not by far, just the basic ones. It is not an objective of this project to be a complete or (strict) implementation of all control-flow patterns - just the ones that have been encountered so far in the (real-life) projects using it.
Furthermore, some scenarios were required to be supported that are not part of the workflow patterns.

If someone encounters a case in which a pattern or scenario applies which is not yet covered here,
please create a pull request (preferred) or raise an issue.
