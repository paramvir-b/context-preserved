package com.rokoder.concurrency.contextpreserved;

import java.util.Objects;
import java.util.concurrent.Callable;
import javax.annotation.Nullable;

/**
 * {@link Callable} which preserves the {@link ThreadLocal} context across thread boundaries.
 *
 * @param <C> Type of context
 * @param <V> the result type of method {@link Callable#call()}
 */
public final class ContextPreservedCallable<C, V> implements Callable<V> {
  private final Callable<V> delegate;
  private final ContextCoordinator<C> contextCoordinator;
  @Nullable
  private final C newContext;

  private ContextPreservedCallable(Callable<V> delegate, ContextCoordinator<C> contextCoordinator,
                                   @Nullable C newContext) {
    this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    this.contextCoordinator =
        Objects.requireNonNull(contextCoordinator, "contextCoordinator cannot be null");
    this.newContext = newContext;
  }

  /**
   * Decorates the passed {@link Callable} with a new one which preserves the {@link ThreadLocal}
   * context across thread boundaries using passed {@link ContextCoordinator}. The new context used
   * is captured, using passed {@link ContextCoordinator}, at the time of call to this api from the
   * thread that calls it.
   *
   * @param callable Callable to be decorated
   * @param contextCoordinator Context coordinator
   * @param <C> Type of context
   * @param <V> Result type of method {@link Callable#call()}
   * @return Newly created wrapped {@link Callable}
   */
  public static <C, V> Callable<V> wrap(Callable<V> callable,
                                        ContextCoordinator<C> contextCoordinator) {
    return new ContextPreservedCallable<>(callable, contextCoordinator, contextCoordinator.get());
  }

  /**
   * Decorates the passed {@link Callable} with a new one which preserves the {@link ThreadLocal}
   * context across thread boundaries using passed {@link ContextCoordinator}. The new context used
   * is one passed.
   *
   * @param callable Callable to be decorated
   * @param contextCoordinator Context coordinator
   * @param newContext New context to preserve
   * @param <C> Type of context
   * @param <V> Result type of method {@link Callable#call()}
   * @return Newly created wrapped {@link Callable}
   */
  public static <C, V> Callable<V> wrap(Callable<V> callable,
                                        ContextCoordinator<C> contextCoordinator,
                                        @Nullable C newContext) {
    return new ContextPreservedCallable<>(callable, contextCoordinator, newContext);
  }

  @Override
  public V call() throws Exception {
    C prevContext = contextCoordinator.get();
    try {
      contextCoordinator.set(newContext);
      return delegate.call();
    } finally {
      contextCoordinator.set(prevContext);
    }
  }
}
