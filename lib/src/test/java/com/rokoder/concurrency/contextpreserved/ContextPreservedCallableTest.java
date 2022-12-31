package com.rokoder.concurrency.contextpreserved;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * We are using function name in context to make each test case isolated from each other. It also
 * isolates tests when we are running them in parallel.
 */
final class ContextPreservedCallableTest {
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Test
  void testCapturedContextAtCreation()
      throws ExecutionException, InterruptedException, TimeoutException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Callable<String> wrapTask1 = ContextPreservedCallable.wrap(contextCaptorTask, coordinator);
    submitAndWait(wrapTask1);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-context-1")));
  }

  @Test
  void testPassedContextAtCreation()
      throws ExecutionException, InterruptedException, TimeoutException {
    String prefixContext = "testPassedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Callable<String> wrapTask1 =
        ContextPreservedCallable.wrap(contextCaptorTask, coordinator,
            prefixContext + "new-passed-context");
    submitAndWait(wrapTask1);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-passed-context")));
  }

  @Test
  void testCapturedContextDontChangeAfterCapture()
      throws ExecutionException, InterruptedException, TimeoutException {
    String prefixContext = "testCapturedContextDontChangeAfterCapture";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Callable<String> wrapTask1 = ContextPreservedCallable.wrap(contextCaptorTask, coordinator);
    submitAndWait(wrapTask1);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    contextCaptorTask.reset();
    submitAndWait(wrapTask1);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-context-1")));
  }

  @Test
  void testPassedContextDontChangeLater()
      throws ExecutionException, InterruptedException, TimeoutException {
    String prefixContext = "testPassedContextDontChangeLater";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Callable<String> wrapTask1 =
        ContextPreservedCallable.wrap(contextCaptorTask, coordinator,
            prefixContext + "new-passed-context");
    submitAndWait(wrapTask1);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-passed-context")));

    coordinator.set("new-context-2");
    contextCaptorTask.reset();
    submitAndWait(wrapTask1);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-passed-context")));
  }

  @Test
  void testPreviousContextRestoration() throws Exception {
    String prefixContext = "testPreviousContextRestoration";
    Callable mockCallable = Mockito.mock(Callable.class);
    ContextCoordinator mockCoordinator = Mockito.mock(ContextCoordinator.class);
    Mockito.when(mockCoordinator.get()).thenReturn(prefixContext + "previous-context");

    Callable<String> wrap =
        ContextPreservedCallable.wrap(mockCallable, mockCoordinator, prefixContext + "new-context");
    wrap.call();

    Mockito.verify(mockCoordinator).set(prefixContext + "new-context");
    Mockito.verify(mockCoordinator).set(prefixContext + "previous-context");
    Mockito.verify(mockCallable).call();
  }

  @Test
  void testPreviousContextRestorationWithException() throws Exception {
    String prefixContext = "testPreviousContextRestorationWithException";
    Callable mockCallable = Mockito.mock(Callable.class);
    Mockito.doThrow(new IllegalStateException("test")).when(mockCallable).call();

    ContextCoordinator mockCoordinator = Mockito.mock(ContextCoordinator.class);
    Mockito.when(mockCoordinator.get()).thenReturn(prefixContext + "previous-context");

    Callable wrap =
        ContextPreservedCallable.wrap(mockCallable, mockCoordinator, prefixContext + "new-context");
    assertThrows(IllegalStateException.class, wrap::call);

    Mockito.verify(mockCoordinator).set(prefixContext + "new-context");
    Mockito.verify(mockCoordinator).set(prefixContext + "previous-context");
    Mockito.verify(mockCallable).call();
  }

  private void submitAndWait(Callable<String> callable)
      throws ExecutionException, InterruptedException, TimeoutException {
    Future<?> future = executorService.submit(callable);
    future.get(1, TimeUnit.SECONDS);
  }
}
