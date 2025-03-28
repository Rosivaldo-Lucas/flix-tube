CREATE TABLE db_video_admin.video_media (
    id CHAR(32) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    raw_location VARCHAR(255) NOT NULL,
    encoded_location VARCHAR(500) NOT NULL,
    media_status VARCHAR(50) NOT NULL
);

CREATE TABLE db_video_admin.video (
    id CHAR(32) NOT NULL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    duration NUMERIC(5, 2) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    video_media_id CHAR(32) NULL,

    CONSTRAINT fk_video_video_media_id FOREIGN KEY (video_media_id) REFERENCES db_video_admin.video_media (id) ON DELETE CASCADE
);
