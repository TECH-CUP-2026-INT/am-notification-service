CREATE TABLE notification (
    id           UUID PRIMARY KEY,
    recipient_id UUID NOT NULL,
    type         VARCHAR(50) NOT NULL,
    message      VARCHAR(500) NOT NULL,
    reference_id UUID,
    is_read      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    read_at      TIMESTAMP
);

CREATE INDEX ix_notification_recipient_id ON notification (recipient_id);
CREATE INDEX ix_notification_recipient_unread ON notification (recipient_id, is_read);
CREATE INDEX ix_notification_recipient_created ON notification (recipient_id, created_at DESC);
