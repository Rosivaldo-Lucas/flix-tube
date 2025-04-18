CREATE TABLE db_video_admin.video (
    id CHAR(36) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    duration NUMERIC(5, 2) NOT NULL,
    filename VARCHAR(255) NOT NULL,
    raw_location VARCHAR(255) NOT NULL,
    encoded_location VARCHAR(500) NULL,
    error VARCHAR(500) NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    processing_started_at TIMESTAMP(6) NULL,
    processing_ended_at TIMESTAMP(6) NULL
);
