package com.rokoder.concurrency.contextpreserved;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

final class DynamicContextPreservedExecutorServiceTest {
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  @Test
  void testAllDelegateApisAreCalled()
      throws ExecutionException, InterruptedException, TimeoutException {
    String prefixContext = "testCapturedContextAtCreation";
    ExecutorService mockExecutorService = Mockito.mock(ExecutorService.class);
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();

    ExecutorService wrappedExecutor =
        DynamicContextPreservedExecutorService.wrap(mockExecutorService,
            coordinator);

    Runnable mockRunnable = Mockito.mock(Runnable.class);
    wrappedExecutor.execute(mockRunnable);
    Mockito.verify(mockExecutorService).execute(Mockito.any(Runnable.class));

    wrappedExecutor.submit(mockRunnable);
    Mockito.verify(mockExecutorService).submit(Mockito.any(Runnable.class));

    wrappedExecutor.submit(mockRunnable, "test-result");
    Mockito.verify(mockExecutorService).submit(Mockito.any(Runnable.class),
        Mockito.eq("test-result"));

    Callable<String> mockCallable = Mockito.mock(Callable.class);
    wrappedExecutor.submit(mockCallable);
    Mockito.verify(mockExecutorService).submit(Mockito.any(Callable.class));

    wrappedExecutor.invokeAny(List.of(mockCallable));
    Mockito.verify(mockExecutorService).invokeAny(Mockito.anyList());

    wrappedExecutor.invokeAny(List.of(mockCallable), 1234L, TimeUnit.MILLISECONDS);
    Mockito.verify(mockExecutorService).invokeAny(Mockito.anyList(), Mockito.eq(1234L),
        Mockito.eq(TimeUnit.MILLISECONDS));

    wrappedExecutor.invokeAll(List.of(mockCallable));
    Mockito.verify(mockExecutorService).invokeAll(Mockito.anyList());

    wrappedExecutor.invokeAll(List.of(mockCallable), 1234L, TimeUnit.MILLISECONDS);
    Mockito.verify(mockExecutorService).invokeAll(Mockito.anyList(), Mockito.eq(1234L),
        Mockito.eq(TimeUnit.MILLISECONDS));

    wrappedExecutor.shutdown();
    Mockito.verify(mockExecutorService).shutdown();

    wrappedExecutor.shutdownNow();
    Mockito.verify(mockExecutorService).shutdownNow();

    Mockito.when(mockExecutorService.awaitTermination(1234L, TimeUnit.MILLISECONDS))
        .thenReturn(true);
    assertThat(wrappedExecutor.awaitTermination(1234L, TimeUnit.MILLISECONDS), equalTo(true));
    Mockito.verify(mockExecutorService).awaitTermination(1234L, TimeUnit.MILLISECONDS);

    Mockito.when(mockExecutorService.isShutdown()).thenReturn(true);
    assertThat(wrappedExecutor.isShutdown(), equalTo(true));
    Mockito.verify(mockExecutorService).isShutdown();

    Mockito.when(mockExecutorService.isTerminated()).thenReturn(true);
    assertThat(wrappedExecutor.isTerminated(), equalTo(true));
    Mockito.verify(mockExecutorService).isTerminated();
  }

  @Test
  void testExecute() {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    ExecutorService wrappedExecutorService =
        DynamicContextPreservedExecutorService.wrap(executorService,
            coordinator);
    wrappedExecutorService.execute(contextCaptorTask);
    submitAndWait(contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    contextCaptorTask.reset();
    wrappedExecutorService.execute(contextCaptorTask);
    submitAndWait(contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-2")));
  }

  @Test
  void testSubmitRunnable() {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    ExecutorService wrappedExecutorService =
        DynamicContextPreservedExecutorService.wrap(executorService,
            coordinator);
    wrappedExecutorService.submit((Runnable) contextCaptorTask);
    submitAndWait(contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    contextCaptorTask.reset();
    wrappedExecutorService.submit((Runnable) contextCaptorTask);
    submitAndWait(contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-2")));
  }

  @Test
  void testSubmitRunnableAndResult() {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    ExecutorService wrappedExecutorService =
        DynamicContextPreservedExecutorService.wrap(executorService,
            coordinator);
    wrappedExecutorService.submit(contextCaptorTask, "test-result");
    submitAndWait(contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    contextCaptorTask.reset();
    wrappedExecutorService.submit(contextCaptorTask, "test-result");
    submitAndWait(contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-2")));
  }

  @Test
  void testSubmitCallable() {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask = new TestStringContextCaptor();
    ExecutorService wrappedExecutorService =
        DynamicContextPreservedExecutorService.wrap(executorService,
            coordinator);
    wrappedExecutorService.submit((Callable<String>) contextCaptorTask);
    submitAndWait(contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    contextCaptorTask.reset();
    wrappedExecutorService.submit((Callable<String>) contextCaptorTask);
    submitAndWait(contextCaptorTask);
    assertThat(contextCaptorTask.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-2")));
  }

  @Test
  void testInvokeAny() throws InterruptedException, ExecutionException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask1 = new TestStringContextCaptor();
    TestStringContextCaptor contextCaptorTask2 = new TestStringContextCaptor();
    List<TestStringContextCaptor> taskList = List.of(contextCaptorTask1, contextCaptorTask2);
    ExecutorService wrappedExecutorService =
        DynamicContextPreservedExecutorService.wrap(executorService,
            coordinator);
    String capturedContext = wrappedExecutorService.invokeAny(taskList);
    assertThat(capturedContext, is(Matchers.equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    taskList.forEach(TestStringContextCaptor::reset);
    capturedContext = wrappedExecutorService.invokeAny(taskList);
    assertThat(capturedContext, is(Matchers.equalTo(prefixContext + "new-context-2")));
  }

  @Test
  void testInvokeAnyWithTimeout() throws InterruptedException, ExecutionException,
      TimeoutException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask1 = new TestStringContextCaptor();
    TestStringContextCaptor contextCaptorTask2 = new TestStringContextCaptor();
    List<TestStringContextCaptor> taskList = List.of(contextCaptorTask1, contextCaptorTask2);
    ExecutorService wrappedExecutorService =
        DynamicContextPreservedExecutorService.wrap(executorService,
            coordinator);
    String capturedContext = wrappedExecutorService.invokeAny(taskList, 1, TimeUnit.SECONDS);
    assertThat(capturedContext, is(Matchers.equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    taskList.forEach(TestStringContextCaptor::reset);
    capturedContext = wrappedExecutorService.invokeAny(taskList, 1, TimeUnit.SECONDS);
    assertThat(capturedContext, is(Matchers.equalTo(prefixContext + "new-context-2")));
  }

  @Test
  void testInvokeAll() throws InterruptedException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask1 = new TestStringContextCaptor();
    TestStringContextCaptor contextCaptorTask2 = new TestStringContextCaptor();
    List<TestStringContextCaptor> taskList = List.of(contextCaptorTask1, contextCaptorTask2);
    ExecutorService wrappedExecutorService =
        DynamicContextPreservedExecutorService.wrap(executorService,
            coordinator);
    wrappedExecutorService.invokeAll(taskList);
    submitAndWait(taskList);
    assertThat(contextCaptorTask1.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-1")));
    assertThat(contextCaptorTask2.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    taskList.forEach(TestStringContextCaptor::reset);
    wrappedExecutorService.invokeAll(taskList);
    submitAndWait(taskList);
    assertThat(contextCaptorTask1.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-2")));
    assertThat(contextCaptorTask2.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-2")));
  }

  @Test
  void testInvokeAllWithTimeout() throws InterruptedException {
    String prefixContext = "testCapturedContextAtCreation";
    TestStringContextCoordinator coordinator = new TestStringContextCoordinator();
    coordinator.set(prefixContext + "new-context-1");

    TestStringContextCaptor contextCaptorTask1 = new TestStringContextCaptor();
    TestStringContextCaptor contextCaptorTask2 = new TestStringContextCaptor();
    List<TestStringContextCaptor> taskList = List.of(contextCaptorTask1, contextCaptorTask2);
    ExecutorService wrappedExecutorService =
        DynamicContextPreservedExecutorService.wrap(executorService,
            coordinator);
    wrappedExecutorService.invokeAll(taskList, 1, TimeUnit.SECONDS);
    submitAndWait(taskList);
    assertThat(contextCaptorTask1.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-1")));
    assertThat(contextCaptorTask2.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-1")));

    coordinator.set(prefixContext + "new-context-2");
    taskList.forEach(TestStringContextCaptor::reset);
    wrappedExecutorService.invokeAll(taskList, 1, TimeUnit.SECONDS);
    submitAndWait(taskList);
    assertThat(contextCaptorTask1.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-2")));
    assertThat(contextCaptorTask2.getCapturedContext(),
        is(Matchers.equalTo(prefixContext + "new-context-2")));
  }

  private void submitAndWait(List<TestStringContextCaptor> taskList) {
    taskList.forEach(k -> submitAndWait(k));
  }

  private void submitAndWait(TestStringContextCaptor task) {
    try {
      task.awaitCompletion(1, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
