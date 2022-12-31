package com.rokoder.concurrency.contextpreserved;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

final class TestStringContextCaptor implements Runnable, Callable<String> {
  private final AtomicReference<String> capturedContext = new AtomicReference<>("");
  private final Runnable userCommand;
  private CountDownLatch countDownLatch = new CountDownLatch(1);

  TestStringContextCaptor(Runnable userCommand) {
    this.userCommand = Objects.requireNonNull(userCommand, "userCommand cannot be null");
  }

  TestStringContextCaptor() {
    this(() -> {
    });
  }

  String getCapturedContext() {
    return capturedContext.get();
  }

  @Override
  public void run() {
    capturedContext.set(TestStringContext.get());
    userCommand.run();
    countDownLatch.countDown();
  }

  @Override
  public String call() {
    run();
    return capturedContext.get();
  }

  void reset() {
    countDownLatch = new CountDownLatch(1);
  }

  boolean awaitCompletion(long timeout, TimeUnit unit) throws InterruptedException {
    return countDownLatch.await(timeout, unit);
  }
}
