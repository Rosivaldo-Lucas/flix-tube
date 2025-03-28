CREATE TABLE db_video_encoder.video (
    id CHAR(32) NOT NULL PRIMARY KEY,
    resource_id VARCHAR(255) NOT NULL,
    bucket VARCHAR(1000) NOT NULL,
    input_file_path NUMERIC(5, 2) NOT NULL,
    output_file_path TIMESTAMP(6) NOT NULL,
    status TIMESTAMP(6) NOT NULL,
    error TIMESTAMP(6) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);
