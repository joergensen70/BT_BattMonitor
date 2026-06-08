package com.fasterxml.jackson.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.TimeZone;

/* JADX INFO: loaded from: classes.dex */
@JacksonAnnotation
@Target({ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonFormat {
    public static final String DEFAULT_LOCALE = "##default";
    public static final String DEFAULT_TIMEZONE = "##default";

    String locale() default "##default";

    String pattern() default "";

    Shape shape() default Shape.ANY;

    String timezone() default "##default";

    public enum Shape {
        ANY,
        SCALAR,
        ARRAY,
        OBJECT,
        NUMBER,
        NUMBER_FLOAT,
        NUMBER_INT,
        STRING,
        BOOLEAN;

        public boolean isNumeric() {
            return this == NUMBER || this == NUMBER_INT || this == NUMBER_FLOAT;
        }

        public boolean isStructured() {
            return this == OBJECT || this == ARRAY;
        }
    }

    public static class Value {
        private TimeZone _timezone;
        private final Locale locale;
        private final String pattern;
        private final Shape shape;
        private final String timezoneStr;

        public Value() {
            this("", Shape.ANY, "", "");
        }

        public Value(JsonFormat jsonFormat) {
            this(jsonFormat.pattern(), jsonFormat.shape(), jsonFormat.locale(), jsonFormat.timezone());
        }

        public Value(String str, Shape shape, String str2, String str3) {
            this(str, shape, (str2 == null || str2.length() == 0 || "##default".equals(str2)) ? null : new Locale(str2), (str3 == null || str3.length() == 0 || "##default".equals(str3)) ? null : str3, null);
        }

        public Value(String str, Shape shape, Locale locale, TimeZone timeZone) {
            this.pattern = str;
            this.shape = shape;
            this.locale = locale;
            this._timezone = timeZone;
            this.timezoneStr = null;
        }

        public Value(String str, Shape shape, Locale locale, String str2, TimeZone timeZone) {
            this.pattern = str;
            this.shape = shape;
            this.locale = locale;
            this._timezone = timeZone;
            this.timezoneStr = str2;
        }

        public Value withPattern(String str) {
            return new Value(str, this.shape, this.locale, this.timezoneStr, this._timezone);
        }

        public Value withShape(Shape shape) {
            return new Value(this.pattern, shape, this.locale, this.timezoneStr, this._timezone);
        }

        public Value withLocale(Locale locale) {
            return new Value(this.pattern, this.shape, locale, this.timezoneStr, this._timezone);
        }

        public Value withTimeZone(TimeZone timeZone) {
            return new Value(this.pattern, this.shape, this.locale, null, timeZone);
        }

        public String getPattern() {
            return this.pattern;
        }

        public Shape getShape() {
            return this.shape;
        }

        public Locale getLocale() {
            return this.locale;
        }

        public String timeZoneAsString() {
            TimeZone timeZone = this._timezone;
            if (timeZone != null) {
                return timeZone.getID();
            }
            return this.timezoneStr;
        }

        public TimeZone getTimeZone() {
            TimeZone timeZone = this._timezone;
            if (timeZone != null) {
                return timeZone;
            }
            String str = this.timezoneStr;
            if (str == null) {
                return null;
            }
            TimeZone timeZone2 = TimeZone.getTimeZone(str);
            this._timezone = timeZone2;
            return timeZone2;
        }

        public boolean hasShape() {
            return this.shape != Shape.ANY;
        }

        public boolean hasPattern() {
            String str = this.pattern;
            return str != null && str.length() > 0;
        }

        public boolean hasLocale() {
            return this.locale != null;
        }

        public boolean hasTimeZone() {
            if (this._timezone != null) {
                return true;
            }
            String str = this.timezoneStr;
            return (str == null || str.isEmpty()) ? false : true;
        }
    }
}
