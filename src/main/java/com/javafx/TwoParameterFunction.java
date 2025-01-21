package com.javafx;

@FunctionalInterface
public interface TwoParameterFunction<T, U, R> {
    R apply(T var1, U var2);
}