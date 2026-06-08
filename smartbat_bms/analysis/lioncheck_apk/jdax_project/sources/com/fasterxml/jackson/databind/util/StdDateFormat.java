package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.io.NumberInput;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/* JADX INFO: loaded from: classes.dex */
public class StdDateFormat extends DateFormat {
    protected static final DateFormat DATE_FORMAT_ISO8601;
    protected static final DateFormat DATE_FORMAT_ISO8601_Z;
    protected static final DateFormat DATE_FORMAT_PLAIN;
    protected static final DateFormat DATE_FORMAT_RFC1123;
    private static final Locale DEFAULT_LOCALE;
    private static final TimeZone DEFAULT_TIMEZONE;
    public static final StdDateFormat instance;
    protected transient DateFormat _formatISO8601;
    protected transient DateFormat _formatISO8601_z;
    protected transient DateFormat _formatPlain;
    protected transient DateFormat _formatRFC1123;
    protected final Locale _locale;
    protected transient TimeZone _timezone;
    protected static final String DATE_FORMAT_STR_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    protected static final String DATE_FORMAT_STR_ISO8601_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    protected static final String DATE_FORMAT_STR_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    protected static final String DATE_FORMAT_STR_PLAIN = "yyyy-MM-dd";
    protected static final String[] ALL_FORMATS = {DATE_FORMAT_STR_ISO8601, DATE_FORMAT_STR_ISO8601_Z, DATE_FORMAT_STR_RFC1123, DATE_FORMAT_STR_PLAIN};

    static {
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        DEFAULT_TIMEZONE = timeZone;
        Locale locale = Locale.US;
        DEFAULT_LOCALE = locale;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT_STR_RFC1123, locale);
        DATE_FORMAT_RFC1123 = simpleDateFormat;
        simpleDateFormat.setTimeZone(timeZone);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(DATE_FORMAT_STR_ISO8601, locale);
        DATE_FORMAT_ISO8601 = simpleDateFormat2;
        simpleDateFormat2.setTimeZone(timeZone);
        SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat(DATE_FORMAT_STR_ISO8601_Z, locale);
        DATE_FORMAT_ISO8601_Z = simpleDateFormat3;
        simpleDateFormat3.setTimeZone(timeZone);
        SimpleDateFormat simpleDateFormat4 = new SimpleDateFormat(DATE_FORMAT_STR_PLAIN, locale);
        DATE_FORMAT_PLAIN = simpleDateFormat4;
        simpleDateFormat4.setTimeZone(timeZone);
        instance = new StdDateFormat();
    }

    public StdDateFormat() {
        this._locale = DEFAULT_LOCALE;
    }

    @Deprecated
    public StdDateFormat(TimeZone timeZone) {
        this(timeZone, DEFAULT_LOCALE);
    }

    public StdDateFormat(TimeZone timeZone, Locale locale) {
        this._timezone = timeZone;
        this._locale = locale;
    }

    public static TimeZone getDefaultTimeZone() {
        return DEFAULT_TIMEZONE;
    }

    public StdDateFormat withTimeZone(TimeZone timeZone) {
        if (timeZone == null) {
            timeZone = DEFAULT_TIMEZONE;
        }
        return timeZone.equals(this._timezone) ? this : new StdDateFormat(timeZone, this._locale);
    }

    public StdDateFormat withLocale(Locale locale) {
        return locale.equals(this._locale) ? this : new StdDateFormat(this._timezone, locale);
    }

    @Override // java.text.DateFormat, java.text.Format
    public StdDateFormat clone() {
        return new StdDateFormat(this._timezone, this._locale);
    }

    @Deprecated
    public static DateFormat getBlueprintISO8601Format() {
        return DATE_FORMAT_ISO8601;
    }

    @Deprecated
    public static DateFormat getISO8601Format(TimeZone timeZone) {
        return getISO8601Format(timeZone, DEFAULT_LOCALE);
    }

    public static DateFormat getISO8601Format(TimeZone timeZone, Locale locale) {
        return _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, timeZone, locale);
    }

    @Deprecated
    public static DateFormat getBlueprintRFC1123Format() {
        return DATE_FORMAT_RFC1123;
    }

    public static DateFormat getRFC1123Format(TimeZone timeZone, Locale locale) {
        return _cloneFormat(DATE_FORMAT_RFC1123, DATE_FORMAT_STR_RFC1123, timeZone, locale);
    }

    @Deprecated
    public static DateFormat getRFC1123Format(TimeZone timeZone) {
        return getRFC1123Format(timeZone, DEFAULT_LOCALE);
    }

    @Override // java.text.DateFormat
    public void setTimeZone(TimeZone timeZone) {
        if (timeZone.equals(this._timezone)) {
            return;
        }
        this._formatRFC1123 = null;
        this._formatISO8601 = null;
        this._formatISO8601_z = null;
        this._formatPlain = null;
        this._timezone = timeZone;
    }

    @Override // java.text.DateFormat
    public Date parse(String str) throws ParseException {
        String strTrim = str.trim();
        ParsePosition parsePosition = new ParsePosition(0);
        Date date = parse(strTrim, parsePosition);
        if (date != null) {
            return date;
        }
        StringBuilder sb = new StringBuilder();
        for (String str2 : ALL_FORMATS) {
            if (sb.length() > 0) {
                sb.append("\", \"");
            } else {
                sb.append('\"');
            }
            sb.append(str2);
        }
        sb.append('\"');
        throw new ParseException(String.format("Can not parse date \"%s\": not compatible with any of standard forms (%s)", strTrim, sb.toString()), parsePosition.getErrorIndex());
    }

    @Override // java.text.DateFormat
    public Date parse(String str, ParsePosition parsePosition) {
        if (looksLikeISO8601(str)) {
            return parseAsISO8601(str, parsePosition);
        }
        int length = str.length();
        while (true) {
            length--;
            if (length < 0) {
                break;
            }
            char cCharAt = str.charAt(length);
            if (cCharAt < '0' || cCharAt > '9') {
                if (length > 0 || cCharAt != '-') {
                    break;
                }
            }
        }
        if (length < 0 && (str.charAt(0) == '-' || NumberInput.inLongRange(str, false))) {
            return new Date(Long.parseLong(str));
        }
        return parseAsRFC1123(str, parsePosition);
    }

    @Override // java.text.DateFormat
    public StringBuffer format(Date date, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (this._formatISO8601 == null) {
            this._formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, this._timezone, this._locale);
        }
        return this._formatISO8601.format(date, stringBuffer, fieldPosition);
    }

    public String toString() {
        String str = "DateFormat " + getClass().getName();
        TimeZone timeZone = this._timezone;
        if (timeZone != null) {
            str = str + " (timezone: " + timeZone + ")";
        }
        return str + "(locale: " + this._locale + ")";
    }

    protected boolean looksLikeISO8601(String str) {
        return str.length() >= 5 && Character.isDigit(str.charAt(0)) && Character.isDigit(str.charAt(3)) && str.charAt(4) == '-';
    }

    /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
    protected Date parseAsISO8601(String str, ParsePosition parsePosition) {
        DateFormat dateFormat_cloneFormat;
        int length = str.length();
        int i = length - 1;
        char cCharAt = str.charAt(i);
        if (length <= 10 && Character.isDigit(cCharAt)) {
            dateFormat_cloneFormat = this._formatPlain;
            if (dateFormat_cloneFormat == null) {
                dateFormat_cloneFormat = _cloneFormat(DATE_FORMAT_PLAIN, DATE_FORMAT_STR_PLAIN, this._timezone, this._locale);
                this._formatPlain = dateFormat_cloneFormat;
            }
        } else if (cCharAt == 'Z') {
            DateFormat dateFormat_cloneFormat2 = this._formatISO8601_z;
            if (dateFormat_cloneFormat2 == null) {
                dateFormat_cloneFormat2 = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z, this._timezone, this._locale);
                this._formatISO8601_z = dateFormat_cloneFormat2;
            }
            if (str.charAt(length - 4) == ':') {
                StringBuilder sb = new StringBuilder(str);
                sb.insert(i, ".000");
                str = sb.toString();
            }
            dateFormat_cloneFormat = dateFormat_cloneFormat2;
        } else if (hasTimeZone(str)) {
            int i2 = length - 3;
            char cCharAt2 = str.charAt(i2);
            if (cCharAt2 == ':') {
                StringBuilder sb2 = new StringBuilder(str);
                sb2.delete(i2, length - 2);
                str = sb2.toString();
            } else if (cCharAt2 == '+' || cCharAt2 == '-') {
                str = str + "00";
            }
            int length2 = str.length();
            int iLastIndexOf = (length2 - str.lastIndexOf(84)) - 6;
            if (iLastIndexOf < 12) {
                int i3 = length2 - 5;
                StringBuilder sb3 = new StringBuilder(str);
                switch (iLastIndexOf) {
                    case 6:
                        sb3.insert(i3, "00.000");
                    case 5:
                        sb3.insert(i3, ":00.000");
                        break;
                    case 8:
                        sb3.insert(i3, ".000");
                        break;
                    case 9:
                        sb3.insert(i3, "000");
                        break;
                    case 10:
                        sb3.insert(i3, "00");
                        break;
                    case 11:
                        sb3.insert(i3, '0');
                        break;
                }
                str = sb3.toString();
            }
            dateFormat_cloneFormat = this._formatISO8601;
            if (dateFormat_cloneFormat == null) {
                dateFormat_cloneFormat = _cloneFormat(DATE_FORMAT_ISO8601, DATE_FORMAT_STR_ISO8601, this._timezone, this._locale);
                this._formatISO8601 = dateFormat_cloneFormat;
            }
        } else {
            StringBuilder sb4 = new StringBuilder(str);
            int iLastIndexOf2 = (length - str.lastIndexOf(84)) - 1;
            if (iLastIndexOf2 < 12) {
                switch (iLastIndexOf2) {
                    case 9:
                        sb4.append('0');
                        break;
                    case 10:
                        sb4.append('0');
                        sb4.append('0');
                        break;
                    case 11:
                        sb4.append('0');
                        sb4.append('0');
                        sb4.append('0');
                        break;
                    default:
                        sb4.append(".000");
                        break;
                }
            }
            sb4.append('Z');
            str = sb4.toString();
            dateFormat_cloneFormat = this._formatISO8601_z;
            if (dateFormat_cloneFormat == null) {
                dateFormat_cloneFormat = _cloneFormat(DATE_FORMAT_ISO8601_Z, DATE_FORMAT_STR_ISO8601_Z, this._timezone, this._locale);
                this._formatISO8601_z = dateFormat_cloneFormat;
            }
        }
        return dateFormat_cloneFormat.parse(str, parsePosition);
    }

    protected Date parseAsRFC1123(String str, ParsePosition parsePosition) {
        if (this._formatRFC1123 == null) {
            this._formatRFC1123 = _cloneFormat(DATE_FORMAT_RFC1123, DATE_FORMAT_STR_RFC1123, this._timezone, this._locale);
        }
        return this._formatRFC1123.parse(str, parsePosition);
    }

    private static final boolean hasTimeZone(String str) {
        char cCharAt;
        char cCharAt2;
        int length = str.length();
        if (length < 6) {
            return false;
        }
        char cCharAt3 = str.charAt(length - 6);
        return cCharAt3 == '+' || cCharAt3 == '-' || (cCharAt = str.charAt(length + (-5))) == '+' || cCharAt == '-' || (cCharAt2 = str.charAt(length + (-3))) == '+' || cCharAt2 == '-';
    }

    private static final DateFormat _cloneFormat(DateFormat dateFormat, String str, TimeZone timeZone, Locale locale) {
        if (!locale.equals(DEFAULT_LOCALE)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(str, locale);
            if (timeZone == null) {
                timeZone = DEFAULT_TIMEZONE;
            }
            simpleDateFormat.setTimeZone(timeZone);
            return simpleDateFormat;
        }
        DateFormat dateFormat2 = (DateFormat) dateFormat.clone();
        if (timeZone != null) {
            dateFormat2.setTimeZone(timeZone);
        }
        return dateFormat2;
    }
}
