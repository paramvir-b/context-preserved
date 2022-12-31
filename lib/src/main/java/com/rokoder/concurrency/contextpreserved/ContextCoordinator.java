package com.rokoder.concurrency.contextpreserved;

import javax.annotation.Nullable;

/**
 * {@link ThreadLocal} context coordinator for Context Preserved classes. It enables setting and
 * getting current {@link ThreadLocal} context.
 *
 * @param <C> Type of the context
 */
public interface ContextCoordinator<C> {

  /**
   * Get the current {@link ThreadLocal} context.
   *
   * @return Return current {@link ThreadLocal} context.
   */

  @Nullable
  C get();

  /**
   * Set the current {@link ThreadLocal} context.
   *
   * @param context New context to be set in the {@link ThreadLocal}.
   */
  void set(@Nullable C context);
}
