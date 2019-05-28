package es.ua.dlsi.copymus.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class ScannerRedirectController {

	@GetMapping
	protected String redirectRoot() {
		return "redirect:/scanner/index.html";
	}

	@GetMapping("/scanner")
	protected String redirectFolder() {
		return "redirect:/scanner/index.html";
	}
}
