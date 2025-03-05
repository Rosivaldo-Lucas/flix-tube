package br.com.rosivaldolucas.flixtube.ms_video_encoder.service;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.entity.Video;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VideoProcessingService {

    public void downloadVideo(Video video) {
        log.info("video downloaded");
    }

    public void fragmentVideo(Video video) {
        log.info("video fragmented");
    }

    public void encodeVideo(Video video) {
        log.info("video encoded");
    }

}
