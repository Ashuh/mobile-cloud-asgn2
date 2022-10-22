/*
 *
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.magnum.mobilecloud.video;

import static org.magnum.mobilecloud.video.client.VideoSvcApi.VIDEO_DURATION_SEARCH_PATH;
import static org.magnum.mobilecloud.video.client.VideoSvcApi.VIDEO_SVC_PATH;
import static org.magnum.mobilecloud.video.client.VideoSvcApi.VIDEO_TITLE_SEARCH_PATH;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class VideoController {
    private final VideoRepository repository;

    @Autowired
    public VideoController(VideoRepository repository) {
        this.repository = repository;
    }

    @GetMapping(VIDEO_SVC_PATH)
    @ResponseBody
    public Collection<Video> getVideos() {
        List<Video> videos = new ArrayList<>();
        repository.findAll().forEach(videos::add);
        return videos;
    }

    @GetMapping(VIDEO_TITLE_SEARCH_PATH)
    @ResponseBody
    public Collection<Video> getVideosByName(@RequestParam("title") String title) {
        return repository.findByName(title);
    }

    @GetMapping(VIDEO_DURATION_SEARCH_PATH)
    @ResponseBody
    public Collection<Video> getVideosByDurationLessThan(@RequestParam("duration") long duration) {
        return repository.findByDurationLessThan(duration);
    }

    @GetMapping(VIDEO_SVC_PATH + "/{id}")
    @ResponseBody
    public Video getVideo(@PathVariable("id") long id, HttpServletResponse response) {
        Optional<Video> video = repository.findById(id);
        if (video.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        return video.get();
    }

    @PostMapping(VIDEO_SVC_PATH)
    @ResponseBody
    public Video postVideo(@RequestBody Video video) {
        video.setLikes(0);
        return repository.save(video);
    }

    @PostMapping(VIDEO_SVC_PATH + "/{id}/like")
    public void likeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
        Optional<Video> videoOptional = repository.findById(id);

        if (videoOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Video video = videoOptional.get();
        String username = p.getName();
        Set<String> likedBy = video.getLikedBy();

        if (likedBy.contains(username)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        long newLikes = video.getLikes() + 1;
        video.setLikes(newLikes);
        likedBy.add(username);
        repository.save(video);
    }

    @PostMapping(VIDEO_SVC_PATH + "/{id}/unlike")
    public void unlikeVideo(@PathVariable("id") long id, Principal p, HttpServletResponse response) {
        Optional<Video> videoOptional = repository.findById(id);

        if (videoOptional.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Video video = videoOptional.get();
        String username = p.getName();
        Set<String> likedBy = video.getLikedBy();

        if (!likedBy.contains(username)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        long newLikes = video.getLikes() - 1;
        video.setLikes(newLikes);
        likedBy.remove(username);
        repository.save(video);
    }
}
