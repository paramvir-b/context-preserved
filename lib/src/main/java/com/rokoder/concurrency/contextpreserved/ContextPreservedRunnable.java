package com.rokoder.concurrency.contextpreserved;

import java.util.Objects;
import javax.annotation.Nullable;

/**
 * {@link Runnable} which preserves the {@link ThreadLocal} context across thread boundaries.
 *
 * @param <C> Type of context
 */
public final class ContextPreservedRunnable<C> implements Runnable {
  private final Runnable delegate;
  private final ContextCoordinator<C> contextCoordinator;
  @Nullable
  private final C newContext;

  private ContextPreservedRunnable(Runnable delegate, ContextCoordinator<C> contextCoordinator,
                                   @Nullable C newContext) {
    this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
    this.contextCoordinator =
        Objects.requireNonNull(contextCoordinator, "contextCoordinator cannot be null");
    this.newContext = newContext;
  }

  /**
   * Decorates the passed {@link Runnable} with a new one which preserves the {@link ThreadLocal}
   * context across thread boundaries using passed {@link ContextCoordinator}. The new context used
   * is captured, using passed {@link ContextCoordinator}, at the time of call to this api from the
   * thread that calls it.
   *
   * @param runnable Runnable to be decorated
   * @param contextCoordinator Context coordinator
   * @param <C> Type of context
   * @return Newly created wrapped {@link Runnable}
   */
  public static <C> Runnable wrap(Runnable runnable, ContextCoordinator<C> contextCoordinator) {
    return new ContextPreservedRunnable<>(runnable, contextCoordinator, contextCoordinator.get());
  }

  /**
   * Decorates the passed {@link Runnable} with a new one which preserves the {@link ThreadLocal}
   * context across thread boundaries using passed {@link ContextCoordinator}. The new context used
   * is one passed.
   *
   * @param runnable Runnable to be decorated
   * @param contextCoordinator Context coordinator
   * @param newContext New context to preserve
   * @param <C> Type of context
   * @return Newly created wrapped {@link Runnable}
   */
  public static <C> Runnable wrap(Runnable runnable, ContextCoordinator<C> contextCoordinator,
                                  @Nullable C newContext) {
    return new ContextPreservedRunnable<>(runnable, contextCoordinator, newContext);
  }

  @Override
  public void run() {
    C prevContext = contextCoordinator.get();
    try {
      contextCoordinator.set(newContext);
      delegate.run();
    } finally {
      contextCoordinator.set(prevContext);
    }
  }
}
