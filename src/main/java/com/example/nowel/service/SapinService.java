package com.example.nowel.service;

import com.example.nowel.entity.BonDeCommande;
import com.example.nowel.entity.Sapin;

public interface SapinService {

	Sapin getSapin(Long id);

	long createSapin();

	boolean addDecoration(Long idSapin, Long idDecoration);

	BonDeCommande vendre(Long idSapin, Long idClient);

	BonDeCommande getCommande(Long id);
}
