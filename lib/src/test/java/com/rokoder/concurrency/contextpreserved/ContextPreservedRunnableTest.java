package com.rokoder.concurrency.contextpreserved;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

final class ContextPreservedRunnableTest {
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Test
  void testCapturedContextAtCreation()
      throws ExecutionException, InterruptedException, TimeoutException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Runnable wrapTask1 = ContextPreservedRunnable.wrap(contextCaptorTask, coordinator);
    submitAndWait(wrapTask1);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-context-1")));
  }

  @Test
  void testPassedContextAtCreation()
      throws ExecutionException, InterruptedException, TimeoutException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Runnable wrapTask1 =
        ContextPreservedRunnable.wrap(contextCaptorTask, coordinator,
            prefixContext + "new-passed-context");
    submitAndWait(wrapTask1);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-passed-context")));
  }

  @Test
  void testCapturedContextDontChangeAfterCapture()
      throws ExecutionException, InterruptedException, TimeoutException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Runnable wrapTask1 = ContextPreservedRunnable.wrap(contextCaptorTask, coordinator);
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
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Runnable wrapTask1 =
        ContextPreservedRunnable.wrap(contextCaptorTask, coordinator,
            prefixContext + "new-passed-context");
    submitAndWait(wrapTask1);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-passed-context")));

    coordinator.set(prefixContext + "new-context-2");
    contextCaptorTask.reset();
    submitAndWait(wrapTask1);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-passed-context")));
  }

  @Test
  void testPreviousContextRestoration() {
    String prefixContext = "testCapturedContextAtCreation";
    Runnable mockRunnable = Mockito.mock(Runnable.class);
    ContextCoordinator mockCoordinator = Mockito.mock(ContextCoordinator.class);
    Mockito.when(mockCoordinator.get()).thenReturn(prefixContext + "previous-context");

    Runnable wrap =
        ContextPreservedRunnable.wrap(mockRunnable, mockCoordinator, prefixContext + "new-context");
    wrap.run();

    Mockito.verify(mockCoordinator).set(prefixContext + "new-context");
    Mockito.verify(mockCoordinator).set(prefixContext + "previous-context");
    Mockito.verify(mockRunnable).run();
  }

  @Test
  void testPreviousContextRestorationWithException() {
    String prefixContext = "testCapturedContextAtCreation";
    Runnable mockRunnable = Mockito.mock(Runnable.class);
    Mockito.doThrow(new IllegalStateException("test")).when(mockRunnable).run();

    ContextCoordinator mockCoordinator = Mockito.mock(ContextCoordinator.class);
    Mockito.when(mockCoordinator.get()).thenReturn(prefixContext + "previous-context");

    Runnable wrap =
        ContextPreservedRunnable.wrap(mockRunnable, mockCoordinator, prefixContext + "new-context");
    assertThrows(IllegalStateException.class, wrap::run);

    Mockito.verify(mockCoordinator).set(prefixContext + "new-context");
    Mockito.verify(mockCoordinator).set(prefixContext + "previous-context");
    Mockito.verify(mockRunnable).run();
  }

  private void submitAndWait(Runnable runnable)
      throws ExecutionException, InterruptedException, TimeoutException {
    Future<?> future = executorService.submit(runnable);
    future.get(1, TimeUnit.SECONDS);
  }
}
