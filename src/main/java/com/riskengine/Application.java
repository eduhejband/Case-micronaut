package com.riskengine;

import io.micronaut.runtime.Micronaut;

public class Application {

    public static final long STARTED_AT_NANOS = System.nanoTime();

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}