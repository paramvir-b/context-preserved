package com.rokoder.concurrency.contextpreserved;

import java.util.Objects;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;

/**
 * An {@link Executor} which preserves the {@link ThreadLocal} context across thread boundaries. It
 * sets the same context (which is captured at the time of creation of this calss) for all calls to
 * {@link Executor#execute(Runnable)}
 *
 * @param <C> Type of context
 */
public final class FixedContextPreservedExecutor<C> implements Executor {
  private final Executor delegate;
  private final ContextCoordinator<C> contextCoordinator;
  @Nullable
  private final C newContext;

  private FixedContextPreservedExecutor(Executor delegate, ContextCoordinator<C> contextCoordinator,
                                        @Nullable C newContext) {
    this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    this.contextCoordinator =
        Objects.requireNonNull(contextCoordinator, "contextCoordinator cannot be null");
    this.newContext = newContext;
  }

  /**
   * Decorates the passed {@link Executor} with a new one which preserves the {@link ThreadLocal}
   * context across thread boundaries using passed {@link ContextCoordinator}. The new context used
   * is captured, using passed {@link ContextCoordinator}, at the time of call to this api from the
   * thread that calls it. And the same context will be used for all calls to
   * {@link Executor#execute(Runnable)}
   *
   * @param executor Executor to be decorated
   * @param contextCoordinator Context coordinator
   * @param <C> Type of context
   * @return Newly created wrapped {@link Executor}
   */
  public static <C> Executor wrap(Executor executor, ContextCoordinator<C> contextCoordinator) {
    return new FixedContextPreservedExecutor<>(executor, contextCoordinator,
        contextCoordinator.get());
  }

  /**
   * Decorates the passed {@link Executor} with a new one which preserves the {@link ThreadLocal}
   * context across thread boundaries using passed {@link ContextCoordinator}. The new context used
   * is one passed. And the same context will be used for all calls to
   * {@link Executor#execute(Runnable)}
   *
   * @param executor Executor to be decorated
   * @param contextCoordinator Context coordinator
   * @param newContext New context to preserve
   * @param <C> Type of context
   * @return Newly created wrapped {@link Runnable}
   */
  public static <C> Executor wrap(Executor executor, ContextCoordinator<C> contextCoordinator,
                                  @Nullable C newContext) {
    return new FixedContextPreservedExecutor<>(executor, contextCoordinator, newContext);
  }

  @Override
  public void execute(Runnable command) {
    delegate.execute(ContextPreservedRunnable.wrap(command, contextCoordinator, newContext));
  }
}
