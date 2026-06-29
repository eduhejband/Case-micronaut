package com.riskengine.metrics;

import com.riskengine.Application;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.time.Duration;

@Singleton
public class StartupMetricsLogger implements ApplicationEventListener<ServerStartupEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(StartupMetricsLogger.class);

    @Override
    public void onApplicationEvent(@NonNull ServerStartupEvent event) {
        long startupNanos = System.nanoTime() - Application.STARTED_AT_NANOS;

        Runtime runtime = Runtime.getRuntime();

        long maxMemoryMb = runtime.maxMemory() / 1024 / 1024;
        long totalMemoryMb = runtime.totalMemory() / 1024 / 1024;
        long freeMemoryMb = runtime.freeMemory() / 1024 / 1024;
        long usedMemoryMb = totalMemoryMb - freeMemoryMb;

        LOG.info(
                "startup_metrics startup_ms={} used_memory_mb={} total_memory_mb={} max_memory_mb={} vm_name={} java_version={}",
                Duration.ofNanos(startupNanos).toMillis(),
                usedMemoryMb,
                totalMemoryMb,
                maxMemoryMb,
                ManagementFactory.getRuntimeMXBean().getVmName(),
                System.getProperty("java.version")
        );
    }
}