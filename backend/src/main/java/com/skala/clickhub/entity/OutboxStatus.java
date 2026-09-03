package com.skala.clickhub.entity;

/** schema.sql: outbox_status */
public enum OutboxStatus {
    PENDING,
    PROCESSING,
    DONE,
    FAILED
}
