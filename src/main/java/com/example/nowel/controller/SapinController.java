package com.example.nowel.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.nowel.entity.BonDeCommande;
import com.example.nowel.entity.Sapin;
import com.example.nowel.service.SapinService;

@RestController
@RequestMapping("/sapin")
public class SapinController {

	private final SapinService sapinService;

	public SapinController(SapinService sapinService) {
		this.sapinService = sapinService;
	}

	@GetMapping("/get")
	public Sapin getSapin(@RequestParam Long id) {
		return sapinService.getSapin(id);
	}

	@PostMapping("/create")
	public long createSapin() {
		return sapinService.createSapin();
	}

	@PostMapping("/addDecoration")
	public boolean addDecoration(@RequestParam Long idSapin, @RequestParam Long idDecoration) {
		return sapinService.addDecoration(idSapin, idDecoration);
	}

	@PostMapping("/vente")
	public BonDeCommande vendre(@RequestParam Long idSapin, @RequestParam Long idClient) {
		return sapinService.vendre(idSapin, idClient);
	}

	@GetMapping("/commande/get")
	public BonDeCommande getCommande(@RequestParam Long id) {
		return sapinService.getCommande(id);
	}
}
