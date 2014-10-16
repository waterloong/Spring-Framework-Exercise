package org.magnum.dataup.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.VideoFileManager;
import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;


@Controller
public class VideoController {
	
	private AtomicLong count = new AtomicLong(1);
	private List<Video> videos = new Vector<>();
	private VideoFileManager fileManager;
	
	{
		try {
			fileManager = VideoFileManager.get();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		videos.add(video);
		video.setId(count.getAndIncrement());
		video.setDataUrl("http://localhost:8080/video/" + count.get() + "/data");
		return video;
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody List<Video> getVideos(){
		return videos;
	}
	
	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<VideoStatus> addVideoData(
			@PathVariable("id") String id, 
            @RequestParam("data") MultipartFile data,
            HttpServletResponse response
			){
		int index = (int) (Long.valueOf(id) - 1);
		if (index >= videos.size() || index < 0) {
			return new ResponseEntity<VideoStatus>(HttpStatus.NOT_FOUND);
		} else {
			Video video = videos.get(index);
			try {
				fileManager.saveVideoData(video, data.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return new ResponseEntity<VideoStatus>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<VideoStatus>(new VideoStatus(VideoState.READY), HttpStatus.OK);
		}
	}
	
	@RequestMapping(value = "/video/{id}/data", method = RequestMethod.GET)
	public void getVideoData(
			@PathVariable("id") String id,
            HttpServletResponse response
			){
		int index = (int) (Long.valueOf(id) - 1);
		if (index >= videos.size() || index < 0) {
			response.setStatus(404);
		} else {
			Video video = videos.get(index);
			try {
				fileManager.copyVideoData(video, response.getOutputStream());
				response.setStatus(201);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				response.setStatus(500);
			}
		}
	}
}
