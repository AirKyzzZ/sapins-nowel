package com.example.nowel.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.example.nowel.entity.Client;
import com.example.nowel.entity.Decoration;
import com.example.nowel.repository.ClientRepository;
import com.example.nowel.repository.DecorationRepository;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

	private final DecorationRepository decorationRepository;
	private final ClientRepository clientRepository;

	public DevDataSeeder(DecorationRepository decorationRepository, ClientRepository clientRepository) {
		this.decorationRepository = decorationRepository;
		this.clientRepository = clientRepository;
	}

	@Override
	public void run(String... args) {
		if (decorationRepository.count() == 0) {
			decorationRepository.save(decoration("Boule", 10, 1));
			decorationRepository.save(decoration("Guirlande", 25, 3));
			decorationRepository.save(decoration("Etoile", 60, 2));
		}

		if (clientRepository.count() == 0) {
			Client client = new Client();
			client.setNom("Mansiet");
			client.setPrenom("Maxime");
			client.setSponsor("EPSI");
			clientRepository.save(client);
		}
	}

	private Decoration decoration(String nom, int valeur, int poids) {
		Decoration decoration = new Decoration();
		decoration.setNom(nom);
		decoration.setValeur(valeur);
		decoration.setPoids(poids);
		return decoration;
	}
}
