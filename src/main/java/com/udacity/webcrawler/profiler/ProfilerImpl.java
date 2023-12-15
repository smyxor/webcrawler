package com.udacity.webcrawler.profiler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * Concrete implementation of the {@link Profiler}.
 */
final class ProfilerImpl implements Profiler {

  private final Clock clock;
  private final ProfilingState state = new ProfilingState();
  private final ZonedDateTime startTime;

  @Inject
  ProfilerImpl(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    this.startTime = ZonedDateTime.now(clock);
  }

  @Override
  public <T> T wrap(Class<T> klass, T delegate) {
    Objects.requireNonNull(klass);
    if (delegate.getClass().getDeclaredMethods().length == 0 || Arrays.stream(delegate.getClass().getDeclaredMethods()).anyMatch(method -> method.getAnnotation(Profiled.class) != null)){
      throw new IllegalArgumentException("No annotated methods");
    } else {
      return (T) Proxy.newProxyInstance(
              klass.getClassLoader(),
              new Class<?>[]{klass},
              new ProfilingMethodInterceptor(this.clock, startTime, delegate, state));
    }


  }

  @Override
  public void writeData(Path path) throws IOException {
    try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, CREATE, APPEND)){
      writeData(writer);
    }
  }

  @Override
  public void writeData(Writer writer) throws IOException {
    writer.write("Run at " + RFC_1123_DATE_TIME.format(startTime));
    writer.write(System.lineSeparator());
    state.write(writer);
    writer.write(System.lineSeparator());
  }
}
