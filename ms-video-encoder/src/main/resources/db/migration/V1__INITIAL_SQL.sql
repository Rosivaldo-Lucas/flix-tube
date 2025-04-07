CREATE TABLE db_video_encoder.video (
    id CHAR(32) NOT NULL PRIMARY KEY,
    resource_id VARCHAR(255) NOT NULL,
    bucket VARCHAR(1000) NOT NULL,
    input_file_path VARCHAR(1000) NOT NULL,
    input_filename VARCHAR(1000) NOT NULL,
    output_file_path VARCHAR(1000) NOT NULL,
    status VARCHAR(1000) NOT NULL,
    error VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);
