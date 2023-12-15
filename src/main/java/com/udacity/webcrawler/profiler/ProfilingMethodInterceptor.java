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

    // TODO: You will need to add more instance fields and constructor arguments to this class.
    ProfilingMethodInterceptor(Clock clock, ZonedDateTime startTime, Object delegate, ProfilingState state) {
        this.clock = Objects.requireNonNull(clock);
        this.startTime = startTime;
        this.delegate = delegate;
        this.state = state;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // TODO: This method interceptor should inspect the called method to see if it is a profiled
        //       method. For profiled methods, the interceptor should record the start time, then
        //       invoke the method using the object that is being profiled. Finally, for profiled
        //       methods, the interceptor should record how long the method call took, using the
        //       ProfilingState methods.

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
