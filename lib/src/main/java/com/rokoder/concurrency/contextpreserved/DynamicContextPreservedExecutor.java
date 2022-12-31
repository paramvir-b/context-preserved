package com.rokoder.concurrency.contextpreserved;

import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * An {@link Executor} which preserves the {@link ThreadLocal} context across thread boundaries. It
 * captures the new context from thread calling {@link Executor#execute(Runnable)} and uses it for
 * new thread which will execute {@link Executor#execute(Runnable)}
 *
 * @param <C> Type of context
 */
public final class DynamicContextPreservedExecutor<C> implements Executor {
  private final Executor delegate;
  private final ContextCoordinator<C> contextCoordinator;

  private DynamicContextPreservedExecutor(Executor delegate,
                                          ContextCoordinator<C> contextCoordinator) {
    this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    this.contextCoordinator =
        Objects.requireNonNull(contextCoordinator, "contextCoordinator cannot be null");
  }

  /**
   * Decorates the passed {@link Executor} with a new one which preserves the {@link ThreadLocal}
   * context across thread boundaries using passed {@link ContextCoordinator}. The new context used
   * is captured at the time of the call to {@link Executor#execute(Runnable)}, using passed
   * {@link ContextCoordinator}. The new context is preserved for passed {@link Runnable} to
   * {@link Executor#execute(Runnable)}
   *
   * @param executor Executor to be decorated
   * @param contextCoordinator Context coordinator
   * @param <C> Type of context
   * @return Newly created wrapped {@link Executor}
   */
  public static <C> Executor wrap(Executor executor, ContextCoordinator<C> contextCoordinator) {
    return new DynamicContextPreservedExecutor<>(executor, contextCoordinator);
  }

  @Override
  public void execute(Runnable command) {
    delegate.execute(ContextPreservedRunnable.wrap(command, contextCoordinator,
        contextCoordinator.get()));
  }
}
