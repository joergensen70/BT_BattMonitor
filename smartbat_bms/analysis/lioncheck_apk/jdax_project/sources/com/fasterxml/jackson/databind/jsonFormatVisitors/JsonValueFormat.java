package com.fasterxml.jackson.databind.jsonFormatVisitors;

import android.support.v4.app.NotificationCompat;

/* JADX INFO: loaded from: classes.dex */
public enum JsonValueFormat {
    DATE_TIME { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.1
        @Override // java.lang.Enum
        public String toString() {
            return "date-time";
        }
    },
    DATE { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.2
        @Override // java.lang.Enum
        public String toString() {
            return "date";
        }
    },
    TIME { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.3
        @Override // java.lang.Enum
        public String toString() {
            return "time";
        }
    },
    UTC_MILLISEC { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.4
        @Override // java.lang.Enum
        public String toString() {
            return "utc-millisec";
        }
    },
    REGEX { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.5
        @Override // java.lang.Enum
        public String toString() {
            return "regex";
        }
    },
    COLOR { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.6
        @Override // java.lang.Enum
        public String toString() {
            return "color";
        }
    },
    STYLE { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.7
        @Override // java.lang.Enum
        public String toString() {
            return "style";
        }
    },
    PHONE { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.8
        @Override // java.lang.Enum
        public String toString() {
            return "phone";
        }
    },
    URI { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.9
        @Override // java.lang.Enum
        public String toString() {
            return "uri";
        }
    },
    EMAIL { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.10
        @Override // java.lang.Enum
        public String toString() {
            return NotificationCompat.CATEGORY_EMAIL;
        }
    },
    IP_ADDRESS { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.11
        @Override // java.lang.Enum
        public String toString() {
            return "ip-address";
        }
    },
    IPV6 { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.12
        @Override // java.lang.Enum
        public String toString() {
            return "ipv6";
        }
    },
    HOST_NAME { // from class: com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat.13
        @Override // java.lang.Enum
        public String toString() {
            return "host-name";
        }
    }
}
