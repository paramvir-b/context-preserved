package com.rokoder.concurrency.contextpreserved;

final class TestStringContext {
  private static final ThreadLocal<String> THREAD_LOCAL = new ThreadLocal<>();

  public static String get() {
    return THREAD_LOCAL.get();
  }

  public static void set(String context) {
    THREAD_LOCAL.set(context);
  }
}
