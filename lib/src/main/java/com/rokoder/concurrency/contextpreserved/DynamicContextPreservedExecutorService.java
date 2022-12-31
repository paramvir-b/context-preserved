package com.rokoder.concurrency.contextpreserved;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * An {@link ExecutorService} which preserves the {@link ThreadLocal} context across thread
 * boundaries. It captures the new context from calling thread and uses it for new thread which will
 * execute the command
 *
 * @param <C> Type of context
 */
public class DynamicContextPreservedExecutorService<C> implements ExecutorService {
  private final ExecutorService delegate;
  private final ContextCoordinator<C> contextCoordinator;

  private DynamicContextPreservedExecutorService(ExecutorService delegate,
                                                 ContextCoordinator<C> contextCoordinator) {
    this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    this.contextCoordinator =
        Objects.requireNonNull(contextCoordinator, "contextCoordinator cannot be null");
  }

  /**
   * Decorates the passed {@link ExecutorService} with a new one which preserves the
   * {@link ThreadLocal} context across thread boundaries using passed {@link ContextCoordinator}.
   * The new context used is captured at the time of the call to command execution apis, using
   * passed {@link ContextCoordinator}.
   *
   * @param executorService Executor service to be decorated
   * @param contextCoordinator Context coordinator
   * @param <C> Type of context
   * @return Newly created wrapped {@link ExecutorService}
   */
  public static <C> ExecutorService wrap(ExecutorService executorService,
                                         ContextCoordinator<C> contextCoordinator) {
    return new DynamicContextPreservedExecutorService<>(executorService, contextCoordinator);
  }

  @Override
  public void shutdown() {
    delegate.shutdown();
  }

  @Override
  public List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public boolean isTerminated() {
    return delegate.isTerminated();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.awaitTermination(timeout, unit);
  }

  @SuppressWarnings("keyfor")
  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return delegate.submit(ContextPreservedCallable.wrap(task, contextCoordinator,
        contextCoordinator.get()));
  }

  @SuppressWarnings("keyfor")
  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return delegate.submit(ContextPreservedRunnable.wrap(task, contextCoordinator,
        contextCoordinator.get()), result);
  }

  // Added org.checkerframework.checker.nullness.qual.Nullable as CheckerFramework is failing and
  // requires @Initialized @NonNull Future<@Nullable ? extends @Initialized @Nullable Object>
  @Override
  public Future<@Nullable ?> submit(Runnable task) {
    return delegate.submit(ContextPreservedRunnable.wrap(task, contextCoordinator,
        contextCoordinator.get()));
  }

  @SuppressWarnings("keyfor")
  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    Objects.requireNonNull(tasks, "tasks cannot be null");
    List<Callable<T>> wrappedTaskList =
        tasks.stream().map((k) -> ContextPreservedCallable.wrap(k, contextCoordinator,
            contextCoordinator.get())).collect(Collectors.toList());
    return delegate.invokeAll(wrappedTaskList);
  }

  @SuppressWarnings("keyfor")
  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
                                       TimeUnit unit) throws InterruptedException {
    Objects.requireNonNull(tasks, "tasks cannot be null");
    List<Callable<T>> wrappedTaskList =
        tasks.stream().map((k) -> ContextPreservedCallable.wrap(k, contextCoordinator,
            contextCoordinator.get())).collect(Collectors.toList());
    return delegate.invokeAll(wrappedTaskList, timeout, unit);
  }

  @SuppressWarnings("keyfor")
  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    Objects.requireNonNull(tasks, "tasks cannot be null");
    List<Callable<T>> wrappedTaskList =
        tasks.stream().map((k) -> ContextPreservedCallable.wrap(k, contextCoordinator,
            contextCoordinator.get())).collect(Collectors.toList());
    return delegate.invokeAny(wrappedTaskList);
  }

  @SuppressWarnings("keyfor")
  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    Objects.requireNonNull(tasks, "tasks cannot be null");
    List<Callable<T>> wrappedTaskList =
        tasks.stream().map((k) -> ContextPreservedCallable.wrap(k, contextCoordinator,
            contextCoordinator.get())).collect(Collectors.toList());
    return delegate.invokeAny(wrappedTaskList, timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    delegate.execute(ContextPreservedRunnable.wrap(command, contextCoordinator,
        contextCoordinator.get()));
  }
}
