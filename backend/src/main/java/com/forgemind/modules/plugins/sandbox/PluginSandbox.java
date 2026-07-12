package com.forgemind.modules.plugins.sandbox;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Component;

/**
 * Provides a secure sandbox environment for executing JavaScript plugins.
 * Uses GraalVM Polyglot API to isolate execution, prevent filesystem/network access,
 * and enforce timeouts.
 */
@Slf4j
@Component
public class PluginSandbox {

  private final ExecutorService executorService = Executors.newCachedThreadPool();

  /**
   * Executes a plugin script within a strict sandbox.
   *
   * @param script JavaScript source code
   * @param timeout Maximum execution time
   * @return The resulting Polyglot Value
   */
  public Value executeSafely(String script, Duration timeout) {
    Callable<Value> task = () -> {
      try (Context context = Context.newBuilder("js")
          .allowAllAccess(false)
          .allowIO(false)
          .allowNativeAccess(false)
          .allowCreateThread(false)
          .allowHostClassLookup(className -> false) // No Java class access
          .build()) {
        
        Source source = Source.newBuilder("js", script, "plugin.js").build();
        return context.eval(source);
      }
    };

    Future<Value> future = executorService.submit(task);
    try {
      return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (TimeoutException e) {
      future.cancel(true);
      log.warn("Plugin execution timed out after {} ms", timeout.toMillis());
      throw new RuntimeException("Plugin execution timed out");
    } catch (InterruptedException | ExecutionException e) {
      log.error("Plugin execution failed", e);
      throw new RuntimeException("Plugin execution failed: " + e.getMessage());
    }
  }
}
