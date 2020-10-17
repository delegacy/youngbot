package com.github.delegacy.youngbot.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.meta.TypeQualifierDefault;

/**
 * Indicates the return values, parameters and fields are non-nullable by default. Annotate a package with
 * this annotation and annotate nullable return values, parameters and fields with {@link Nullable}.
 */
@Nonnull
@Documented
@Target(ElementType.PACKAGE)
@Retention(RetentionPolicy.RUNTIME)
@TypeQualifierDefault({ ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD })
public @interface NonNullByDefault {
}
