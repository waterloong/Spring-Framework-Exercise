package org.magnum.mobilecloud.video.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoController {
	
	@Autowired
	private VideoRepository videoRepository;
	
	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		return this.videoRepository.save(video);
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideos(){
		Iterable<Video> it = this.videoRepository.findAll();
		List<Video> list = new ArrayList<>();
		it.forEach(list::add);
		return list;
	}	
	
	@RequestMapping(value = "/video/{id}", method = RequestMethod.GET)
	public @ResponseBody Video getVideo(
			@PathVariable long id
			){
		return this.videoRepository.findOne(id);
	}
	
	/*
	 *  POST /video/{id}/like
		Allows a user to like a video. Returns 200 Ok on success, 404 if the video is not found, or 400 if the user has already liked the video.
		The service should should keep track of which users have liked a video and prevent a user from liking a video twice. A POJO Video object is provided for you and you will need to annotate and/or add to it in order to make it persistable.
		A user is only allowed to like a video once. If a user tries to like a video a second time, the operation should fail and return 400 Bad Request.
	 */
	@RequestMapping(value = "/video/{id}/like", method = RequestMethod.POST)
	public void likeVideo(
			@PathVariable long id,
			HttpServletResponse response,
            Principal p
			){
		Video video = this.videoRepository.findOne(id);
		if (video == null) {
			response.setStatus(404);
		} else if (video.getUsers().contains(p.getName())){
			response.setStatus(400);
		} else {
			video.setLikes(video.getLikes() + 1);
			video.getUsers().add(p.getName());
			this.videoRepository.save(video);
			response.setStatus(200);
		}
	}
	
	/*
	 POST /video/{id}/unlike
		Allows a user to unlike a video that he/she previously liked. 
		Returns 200 OK on success, 404 if the video is not found, and a 400 if the user has not previously liked the specified video.
	*/
	@RequestMapping(value = "/video/{id}/unlike", method = RequestMethod.POST)
	public void unlikeVideo(
			@PathVariable long id,
			HttpServletResponse response,
            Principal p
			){
		Video video = this.videoRepository.findOne(id);
		if (video == null) {
			response.setStatus(404);
		} else if (!video.getUsers().contains(p.getName())){
			response.setStatus(400);
		} else {
			video.setLikes(video.getLikes() - 1);
			video.getUsers().remove(p.getName());
			this.videoRepository.save(video);
			response.setStatus(200);
		}
	}
	
	/*
	 * GET /video/{id}/likedby
	 * Returns a list of the string usernames of the users that have liked the specified video. If the video is not found, a 404 error should be generated.
	 */
	@RequestMapping(value = "/video/{id}/likedby", method = RequestMethod.GET)
	public ResponseEntity<Collection<String>> getUsers(
			@PathVariable long id
			) {
		Video video = this.videoRepository.findOne(id);
		if (video == null) {
			return new ResponseEntity<Collection<String>>(HttpStatus.NOT_FOUND);
		} else {
			
			return new ResponseEntity<Collection<String>>(video.getUsers(), HttpStatus.OK);
		}
	}
	
	/*
	 * GET /video/search/findByName?title={title}
	 * Returns a list of videos whose titles match the given parameter or an empty list if none are found.
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> searchVideos(String title) {
		System.out.println("title\n" + title);
		return this.videoRepository.findByName(title);
	}
	
	/*
	 *GET /video/search/findByDurationLessThan?duration={duration}
	 *Returns a list of videos whose durations are less than the given parameter or an empty list if none are found. 
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> searchVideos(
			@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration
			) {
		System.out.println("duration\n" + duration);
		return this.videoRepository.findByDurationLessThan(duration);
	}
}
