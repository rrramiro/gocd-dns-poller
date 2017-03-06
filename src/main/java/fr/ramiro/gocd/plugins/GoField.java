package fr.ramiro.gocd.plugins;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface GoField {
    String name();
    String displayName();
    int displayOrder() default 0;
    String defaultValue() default "";
    boolean partOfIdentity() default true;
    boolean required() default true;
}