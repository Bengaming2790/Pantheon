package ca.techgarage.pantheon;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

@Retention(RetentionPolicy.RUNTIME) // required so reflection can read it
@Target(ElementType.FIELD)          // only usable on fields
public @interface Comment {
    String value();
}