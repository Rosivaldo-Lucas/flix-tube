CREATE TABLE db_video_encoder.video (
    transaction_id CHAR(36) NOT NULL PRIMARY KEY,
    bucket VARCHAR(1000) NOT NULL,
    input_path VARCHAR(1000) NOT NULL,
    output_path VARCHAR(1000) NOT NULL,
    input_filename VARCHAR(1000) NOT NULL,
    status VARCHAR(1000) NOT NULL,
    error VARCHAR(1000) NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);
