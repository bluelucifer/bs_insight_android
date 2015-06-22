// Copyright 2012 Square, Inc.
package kr.co.wisetracker.insight.lib.squareup.tape;

/**
 * Inject dependencies into tasks of any kind.
 *
 * @param <T> The type of tasks to inject.
 */
public interface TaskInjector<T extends Task> {
  void injectMembers(T task);
}
