package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

    private final Clock clock;

    private ZonedDateTime startTime;

    private final Object delegate;

    private final ProfilingState state;

    ProfilingMethodInterceptor(Clock clock, ZonedDateTime startTime, Object delegate, ProfilingState state) {
        this.clock = Objects.requireNonNull(clock);
        this.startTime = startTime;
        this.delegate = delegate;
        this.state = state;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(delegate, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        } finally {
            if (method.getAnnotation(Profiled.class) != null) {
                state.record(delegate.getClass(), method, Duration.between(startTime.toInstant(), clock.instant()));
                startTime = ZonedDateTime.now(clock);
            }
        }
    }
}
