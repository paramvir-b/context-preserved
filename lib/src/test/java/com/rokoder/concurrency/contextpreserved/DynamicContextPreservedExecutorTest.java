package com.rokoder.concurrency.contextpreserved;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DynamicContextPreservedExecutorTest {
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Test
  void testBasic() throws InterruptedException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Executor wrappedExecutor =
        DynamicContextPreservedExecutor.wrap(executorService,
            coordinator);
    submitAndWait(wrappedExecutor, contextCaptorTask);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-context-1")));
  }

  @Test
  void testContextDoChangeLater() throws InterruptedException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    Executor wrappedExecutor =
        DynamicContextPreservedExecutor.wrap(executorService, coordinator);
    submitAndWait(wrappedExecutor, contextCaptorTask);

    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    contextCaptorTask.reset();
    submitAndWait(wrappedExecutor, contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(equalTo(prefixContext + "new-context-2")));
  }

  @Test
  void testPreviousContextRestoration() throws InterruptedException {
    String prefixContext = "testCapturedContextAtCreation";
    Runnable mockRunnable = Mockito.mock(Runnable.class);
    ContextCoordinator mockCoordinator = Mockito.mock(ContextCoordinator.class);
    Mockito.when(mockCoordinator.get())
        .thenReturn(prefixContext + "new-context", prefixContext + "previous-context");

    ExecutorService es = Executors.newSingleThreadExecutor();
    Executor wrappedExecutor = DynamicContextPreservedExecutor.wrap(es, mockCoordinator);
    wrappedExecutor.execute(mockRunnable);
    es.awaitTermination(1, TimeUnit.SECONDS);

    Mockito.verify(mockCoordinator, Mockito.times(2)).get();
    Mockito.verify(mockCoordinator).set(prefixContext + "new-context");
    Mockito.verify(mockCoordinator).set(prefixContext + "previous-context");
    Mockito.verify(mockRunnable).run();
  }

  @Test
  void testPreviousContextRestorationWithException() throws InterruptedException {
    String prefixContext = "testCapturedContextAtCreation";
    Runnable mockRunnable = Mockito.mock(Runnable.class);
    Mockito.doThrow(new IllegalStateException("test")).when(mockRunnable).run();

    ContextCoordinator mockCoordinator = Mockito.mock(ContextCoordinator.class);
    Mockito.when(mockCoordinator.get())
        .thenReturn(prefixContext + "new-context", prefixContext + "previous-context");

    ExecutorService es = Executors.newSingleThreadExecutor();
    Executor wrappedExecutor = DynamicContextPreservedExecutor.wrap(es, mockCoordinator);
    wrappedExecutor.execute(mockRunnable);
    es.awaitTermination(1, TimeUnit.SECONDS);

    Mockito.verify(mockCoordinator, Mockito.times(2)).get();
    Mockito.verify(mockCoordinator).set(prefixContext + "new-context");
    Mockito.verify(mockCoordinator).set(prefixContext + "previous-context");
    Mockito.verify(mockRunnable).run();
  }

  private void submitAndWait(Executor executor, TestStringContextCaptor task)
      throws InterruptedException {
    executor.execute(task);
    task.awaitCompletion(1, TimeUnit.SECONDS);
  }
}
