package br.com.rosivaldolucas.flixtube.ms_video_encoder.repository;

import br.com.rosivaldolucas.flixtube.ms_video_encoder.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VideoRepository extends JpaRepository<Video, String> {

}
