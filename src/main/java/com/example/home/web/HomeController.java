package com.example.home.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping(path = { "/", "/{date:\\d{4}-\\d{2}-\\d{2}}/**" })
	public String home() {
		return "forward:/index.html";
	}

}
