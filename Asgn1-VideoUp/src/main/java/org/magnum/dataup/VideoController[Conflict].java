package org.magnum.dataup;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import org.magnum.dataup.model.Video;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class VideoController {
	
	private static AtomicLong count = new AtomicLong(0);
	private static List<Video> videos = new Vector<>();
	
	@RequestMapping(value = "/video", method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video){
		videos.add(video);
		video.setId(count.getAndIncrement());
		System.err.println(video.getId());
		System.err.println(video.getTitle());
		System.err.println(video.getDataUrl());
		return video;
	}
	
	@RequestMapping(value = "/video", method = RequestMethod.GET)
	public @ResponseBody List<Video> getVideos(){
		return videos;
	}
}
