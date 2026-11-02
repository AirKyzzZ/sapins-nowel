package com.example.nowel.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.nowel.config.DevDataSeeder;
import com.example.nowel.entity.BonDeCommande;
import com.example.nowel.entity.Client;
import com.example.nowel.entity.Decoration;
import com.example.nowel.entity.Sapin;
import com.example.nowel.repository.BonDeCommandeRepository;
import com.example.nowel.repository.ClientRepository;
import com.example.nowel.repository.DecorationRepository;
import com.example.nowel.repository.SapinRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SapinApiIntegrationTests {

	private static final String ID_INCONNU = "999999";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SapinRepository sapinRepository;

	@Autowired
	private DecorationRepository decorationRepository;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private BonDeCommandeRepository bonDeCommandeRepository;

	@BeforeEach
	void viderLaBase() {
		bonDeCommandeRepository.deleteAll();
		sapinRepository.deleteAll();
		clientRepository.deleteAll();
		decorationRepository.deleteAll();
	}

	@Test
	void createAjouteUnSapinEtRetourneLeNombreEnBase() throws Exception {
		mockMvc.perform(post("/sapin/create")).andExpect(status().isOk()).andExpect(content().string("1"));
		mockMvc.perform(post("/sapin/create")).andExpect(status().isOk()).andExpect(content().string("2"));

		assertThat(sapinRepository.count()).isEqualTo(2);
	}

	@Test
	void getRetourneUnNouveauSapinNonVenduEtVide() throws Exception {
		Sapin sapin = sapinRepository.save(new Sapin());

		mockMvc.perform(get("/sapin/get").param("id", sapin.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(sapin.getId()))
				.andExpect(jsonPath("$.vendu").value(false))
				.andExpect(jsonPath("$.decorations").isEmpty());
	}

	@Test
	void getRetourneUnCorpsVideSiSapinInconnu() throws Exception {
		mockMvc.perform(get("/sapin/get").param("id", ID_INCONNU))
				.andExpect(status().isOk())
				.andExpect(content().string(""));
	}

	@Test
	void addDecorationPersisteLaDecorationSurLeSapin() throws Exception {
		Sapin sapin = sapinRepository.save(new Sapin());
		Decoration decoration = decoration("Boule", 10, 1);

		addDecoration(sapin, decoration).andExpect(status().isOk()).andExpect(content().string("true"));

		mockMvc.perform(get("/sapin/get").param("id", sapin.getId().toString()))
				.andExpect(jsonPath("$.decorations.length()").value(1))
				.andExpect(jsonPath("$.decorations[0].nom").value("Boule"))
				.andExpect(jsonPath("$.decorations[0].valeur").value(10))
				.andExpect(jsonPath("$.decorations[0].poids").value(1));
	}

	@Test
	void uneDecorationPeutEtreSurPlusieursSapins() throws Exception {
		Decoration decoration = decoration("Boule", 10, 1);

		addDecoration(sapinRepository.save(new Sapin()), decoration).andExpect(content().string("true"));
		addDecoration(sapinRepository.save(new Sapin()), decoration).andExpect(content().string("true"));
	}

	@Test
	void addDecorationRetourneFalseSurUnSapinVendu() throws Exception {
		Sapin sapin = new Sapin();
		sapin.setVendu(true);
		sapin = sapinRepository.save(sapin);

		addDecoration(sapin, decoration("Boule", 10, 1)).andExpect(content().string("false"));

		mockMvc.perform(get("/sapin/get").param("id", sapin.getId().toString()))
				.andExpect(jsonPath("$.decorations").isEmpty());
	}

	@Test
	void addDecorationRetourneFalseSiIdInconnu() throws Exception {
		Sapin sapin = sapinRepository.save(new Sapin());
		Decoration decoration = decoration("Boule", 10, 1);

		mockMvc.perform(post("/sapin/addDecoration").param("idSapin", ID_INCONNU)
				.param("idDecoration", decoration.getId().toString())).andExpect(content().string("false"));
		mockMvc.perform(post("/sapin/addDecoration").param("idSapin", sapin.getId().toString())
				.param("idDecoration", ID_INCONNU)).andExpect(content().string("false"));
	}

	@Test
	void venteCreeLeBonDeCommandeEtPersisteLesEffets() throws Exception {
		Sapin sapin = sapinRepository.save(new Sapin());
		addDecoration(sapin, decoration("Boule", 10, 1));
		addDecoration(sapin, decoration("Guirlande", 9, 3));
		Client client = client(5);

		vente(sapin, client)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.adresse").value("Maxime rue du bonjour"))
				.andExpect(jsonPath("$.coutTotal").value(19))
				.andExpect(jsonPath("$.poidsTotal").value(4))
				.andExpect(jsonPath("$.sapin.id").value(sapin.getId()))
				.andExpect(jsonPath("$.sapin.vendu").value(true))
				.andExpect(jsonPath("$.client.id").value(client.getId()))
				.andExpect(jsonPath("$.client.pointsFidelite").value(6));

		assertThat(sapinRepository.findById(sapin.getId()).orElseThrow().isVendu()).isTrue();
		assertThat(clientRepository.findById(client.getId()).orElseThrow().getPointsFidelite()).isEqualTo(6);
		assertThat(bonDeCommandeRepository.count()).isEqualTo(1);
	}

	@Test
	void venteDUnSapinDejaVenduNeChangeRien() throws Exception {
		Sapin sapin = sapinRepository.save(new Sapin());
		addDecoration(sapin, decoration("Etoile", 60, 2));
		Client client = client(0);
		vente(sapin, client).andExpect(jsonPath("$.client.pointsFidelite").value(6));

		vente(sapin, client).andExpect(status().isOk()).andExpect(content().string(""));

		assertThat(bonDeCommandeRepository.count()).isEqualTo(1);
		assertThat(clientRepository.findById(client.getId()).orElseThrow().getPointsFidelite()).isEqualTo(6);
	}

	@Test
	void venteAvecClientInconnuNeVendPasLeSapin() throws Exception {
		Sapin sapin = sapinRepository.save(new Sapin());

		mockMvc.perform(post("/sapin/vente").param("idSapin", sapin.getId().toString()).param("idClient", ID_INCONNU))
				.andExpect(status().isOk())
				.andExpect(content().string(""));

		assertThat(sapinRepository.findById(sapin.getId()).orElseThrow().isVendu()).isFalse();
		assertThat(bonDeCommandeRepository.count()).isZero();
	}

	@Test
	void commandeGetRetourneLeBonDeCommande() throws Exception {
		Sapin sapin = sapinRepository.save(new Sapin());
		addDecoration(sapin, decoration("Etoile", 60, 2));
		vente(sapin, client(0));
		BonDeCommande bonDeCommande = bonDeCommandeRepository.findAll().iterator().next();

		mockMvc.perform(get("/sapin/commande/get").param("id", bonDeCommande.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(bonDeCommande.getId()))
				.andExpect(jsonPath("$.coutTotal").value(60))
				.andExpect(jsonPath("$.poidsTotal").value(2))
				.andExpect(jsonPath("$.sapin.decorations.length()").value(1));
	}

	@Test
	void commandeGetRetourneUnCorpsVideSiInconnue() throws Exception {
		mockMvc.perform(get("/sapin/commande/get").param("id", ID_INCONNU))
				.andExpect(status().isOk())
				.andExpect(content().string(""));
	}

	@Test
	void lesRoutesRespectentLeurVerbe() throws Exception {
		mockMvc.perform(post("/sapin/get").param("id", "1")).andExpect(status().isMethodNotAllowed());
		mockMvc.perform(get("/sapin/create")).andExpect(status().isMethodNotAllowed());
		mockMvc.perform(get("/sapin/addDecoration")).andExpect(status().isMethodNotAllowed());
		mockMvc.perform(get("/sapin/vente")).andExpect(status().isMethodNotAllowed());
		mockMvc.perform(post("/sapin/commande/get").param("id", "1")).andExpect(status().isMethodNotAllowed());
	}

	@Test
	void unParametreManquantRetourne400() throws Exception {
		mockMvc.perform(post("/sapin/vente").param("idSapin", "1")).andExpect(status().isBadRequest());
	}

	@Test
	void leSeederEstIdempotent() throws Exception {
		DevDataSeeder seeder = new DevDataSeeder(decorationRepository, clientRepository);

		seeder.run();
		seeder.run();

		assertThat(decorationRepository.count()).isEqualTo(3);
		assertThat(clientRepository.count()).isEqualTo(1);
	}

	private ResultActions addDecoration(Sapin sapin, Decoration decoration) throws Exception {
		return mockMvc.perform(post("/sapin/addDecoration").param("idSapin", sapin.getId().toString())
				.param("idDecoration", decoration.getId().toString()));
	}

	private ResultActions vente(Sapin sapin, Client client) throws Exception {
		return mockMvc.perform(post("/sapin/vente").param("idSapin", sapin.getId().toString())
				.param("idClient", client.getId().toString()));
	}

	private Decoration decoration(String nom, int valeur, int poids) {
		Decoration decoration = new Decoration();
		decoration.setNom(nom);
		decoration.setValeur(valeur);
		decoration.setPoids(poids);
		return decorationRepository.save(decoration);
	}

	private Client client(int pointsFidelite) {
		Client client = new Client();
		client.setNom("Mansiet");
		client.setPrenom("Maxime");
		client.setSponsor("EPSI");
		client.setPointsFidelite(pointsFidelite);
		return clientRepository.save(client);
	}
}
