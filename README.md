# Video Encoder Service

This project aims to perform MP4 video encoding using the Bento4 tool.
It was developed in Java with Spring Boot and Spring Cloud, providing a scalable and efficient solution for video processing.

## Technologies Used

- **Java**
- **Spring Boot**
- **Spring Cloud AWS S3**
- **PostgreSQL**
- **Bento4**
- **Docker**
- **RabbitMQ** (for asynchronous processing)
- **LocalStack** (to simulate S3 locally)

## How It Works

1. The video name is received through a RabbitMQ queue.
2. The system reads the queue, retrieves the video from an S3 bucket (simulated with LocalStack), and starts the encoding process.
3. Once processing is complete, the system uploads the video fragments to a designated folder in the S3 bucket.
4. The system logs the processing status and ensures reliability through message queue management.

## Features

- Asynchronous video encoding using message queues.
- Integration with S3 storage (using LocalStack for local simulation).
- Processing and conversion using Bento4.
- Logging and monitoring of the encoding process.

## Tech Design
![video-encoder-tech-design.png](docs/video-encoder-tech-design.png)
