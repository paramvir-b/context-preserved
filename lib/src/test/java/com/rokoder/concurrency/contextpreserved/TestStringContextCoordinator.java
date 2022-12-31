package com.rokoder.concurrency.contextpreserved;

final class TestStringContextCoordinator implements ContextCoordinator<String> {
  @Override
  public String get() {
    return TestStringContext.get();
  }

  @Override
  public void set(String context) {
    TestStringContext.set(context);
  }
}
