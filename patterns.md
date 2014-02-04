# Control-flow patterns for orchestrating vert.x calls

 * [Sequence](#Sequence)
 * [Parallel Split](#Parallel_Split)
 * [Synchronization](#Synchronization)
 * [Exclusive Choice](#Exclusive_Choice)
 * [Simple Merge](#Simple_Merge)

# Other supported scenarios

 * [Decision with one call](#Decision_with_one_call)
 * [One-way call](#One_way_call)

### Sequence
This is the simplest use case, just make the next call when the previous one was completed:

![Sequence control flow](http://www.workflowpatterns.com/patterns/control/images/fig1.png)

Code snippet for three calls in sequence:

    class FirstCallHandler extends AbstractReturningCallHandler { ... }
    class SecondCallHandler extends AbstractReturningCallHandler { ... }
    class ThirdCallHandler extends AbstractReturningCallHandler { ... }


    ResponseListener listener = ...

    CallSequence sequence = createCallSequence(vertx)
                            .addCall(FirstCallHandler.class)
                            .addCall(SecondCallHandler.class)
                            .addCall(ThirdCallHandler.class)
                            .build();
    // Set context vars, timeout.
    sequence.execute(listener);

### Parallel_Split
This makes it possible to execute multiple calls in parallel:

![Parallel split control flow](http://www.workflowpatterns.com/patterns/control/images/fig2.png)

Code snippet for two consecutive times of two calls in parallel:

    ResponseListener listener = ...

    CallSequence sequence = createCallSequence(vertx)
                            .addParallelCalls(FirstCallHandler.class, SecondCallHandler.class)
                            .addParallelCalls(ThirdCallHandler.class, FourthCallHandler.class)
                            .build();
    // Set context vars, timeout.
    sequence.execute(listener);

### Synchronization
This actually is the implicit way-of-working for the parallel calls: the orchestration waits for all of them to complete before moving on:

![Synchronization control flow](http://www.workflowpatterns.com/patterns/control/images/fig3.png)

### Exclusive_Choice
In this case, there are two possible paths the execution can take; which one is decided at runtime:

![Exclusive choice control flow](http://www.workflowpatterns.com/patterns/control/images/fig4.png)

Code snippet for two calls, one of which is executed depending on the outcome of the decision:

    ResponseListener listener = ...

    CallSequence sequence = createCallSequence(vertx)
                            .addDecision(DecisionHandler.class,
                                 whenTrue(createCallSequence(vertx)
                                          .FirstCallHandler.class
                                          .build()),
                                 whenFalse(createCallSequence(vertx)
                                          .SecondCallHandler.class
                                          .build()))
                            .build();
    // Set context vars, timeout.
    sequence.execute(listener);

### Simple_Merge
As with the Synchronization, this is the implicit behaviour of the decision:

![Simple merge control flow](http://www.workflowpatterns.com/patterns/control/images/fig5.png)

### Decision_with_one_call
Here a decision is used to determine at runtime whether a sequence should be run at all.

Code snippet for a call that is executed depending on the outcome of the decision:

    ResponseListener listener = ...

    CallSequence sequence = createCallSequence(vertx)
                            .addDecision(DecisionHandler.class,
                                 whenTrue(createCallSequence(vertx)
                                          .FirstCallHandler.class
                                          .build()))
                            .build();
    // Set context vars, timeout.
    sequence.execute(listener);

### One_way_call
Not all calls have a return value, and in some cases the caller may not be interested in it even when there is one.
For such cases, a fire-and-forget possibility is built in.

Code snippet for a call that is executed depending on the outcome of the decision:

    class OneWayCallHandler extends AbstractOneWayCallHandler { ... }


    ResponseListener listener = ...

    CallSequence sequence = createCallSequence(vertx)
                            .addCall(OneWayCallHandler.class)
                            .build();
    // Set context vars, timeout.
    sequence.execute(listener);

