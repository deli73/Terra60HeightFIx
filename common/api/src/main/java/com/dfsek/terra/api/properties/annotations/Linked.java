package com.dfsek.terra.api.properties.annotations;

import com.dfsek.terra.api.properties.PropertyHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that this property holder shares properties
 * with the {@link #value()} holder.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Linked {
    Class<? extends PropertyHolder> value();
}