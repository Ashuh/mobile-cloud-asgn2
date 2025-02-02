package org.magnum.mobilecloud.video.repository;

import java.util.Collection;

import org.springframework.data.repository.CrudRepository;

public interface VideoRepository extends CrudRepository<Video, Long> {
    Collection<Video> findByName(String name);

    Collection<Video> findByDurationLessThan(long duration);
}
