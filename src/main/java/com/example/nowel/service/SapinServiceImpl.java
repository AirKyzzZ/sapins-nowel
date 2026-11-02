package com.example.nowel.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nowel.entity.BonDeCommande;
import com.example.nowel.entity.Client;
import com.example.nowel.entity.Decoration;
import com.example.nowel.entity.Sapin;
import com.example.nowel.repository.BonDeCommandeRepository;
import com.example.nowel.repository.ClientRepository;
import com.example.nowel.repository.DecorationRepository;
import com.example.nowel.repository.SapinRepository;

@Service
public class SapinServiceImpl implements SapinService {

	private static final String ADRESSE_LIVRAISON = "Maxime rue du bonjour";
	private static final int DIVISEUR_POINTS_FIDELITE = 10;

	private final SapinRepository sapinRepository;
	private final DecorationRepository decorationRepository;
	private final ClientRepository clientRepository;
	private final BonDeCommandeRepository bonDeCommandeRepository;

	public SapinServiceImpl(SapinRepository sapinRepository, DecorationRepository decorationRepository,
			ClientRepository clientRepository, BonDeCommandeRepository bonDeCommandeRepository) {
		this.sapinRepository = sapinRepository;
		this.decorationRepository = decorationRepository;
		this.clientRepository = clientRepository;
		this.bonDeCommandeRepository = bonDeCommandeRepository;
	}

	@Override
	public Sapin getSapin(Long id) {
		return sapinRepository.findById(id).orElse(null);
	}

	@Override
	@Transactional
	public long createSapin() {
		sapinRepository.save(new Sapin());
		return sapinRepository.count();
	}

	@Override
	@Transactional
	public boolean addDecoration(Long idSapin, Long idDecoration) {
		Optional<Sapin> sapin = sapinRepository.findById(idSapin);
		Optional<Decoration> decoration = decorationRepository.findById(idDecoration);
		if (sapin.isEmpty() || decoration.isEmpty() || sapin.get().isVendu()) {
			return false;
		}

		sapin.get().getDecorations().add(decoration.get());
		sapinRepository.save(sapin.get());
		return true;
	}

	@Override
	@Transactional
	public BonDeCommande vendre(Long idSapin, Long idClient) {
		Optional<Sapin> sapinTrouve = sapinRepository.findById(idSapin);
		Optional<Client> clientTrouve = clientRepository.findById(idClient);
		if (sapinTrouve.isEmpty() || clientTrouve.isEmpty() || sapinTrouve.get().isVendu()) {
			return null;
		}

		Sapin sapin = sapinTrouve.get();
		Client client = clientTrouve.get();

		BonDeCommande bonDeCommande = new BonDeCommande();
		bonDeCommande.setAdresse(ADRESSE_LIVRAISON);
		bonDeCommande.setCoutTotal(sapin.getDecorations().stream().mapToInt(Decoration::getValeur).sum());
		bonDeCommande.setPoidsTotal(sapin.getDecorations().stream().mapToInt(Decoration::getPoids).sum());
		bonDeCommande.setSapin(sapin);
		bonDeCommande.setClient(client);

		sapin.setVendu(true);
		client.setPointsFidelite(client.getPointsFidelite() + bonDeCommande.getCoutTotal() / DIVISEUR_POINTS_FIDELITE);

		sapinRepository.save(sapin);
		clientRepository.save(client);
		return bonDeCommandeRepository.save(bonDeCommande);
	}

	@Override
	public BonDeCommande getCommande(Long id) {
		return bonDeCommandeRepository.findById(id).orElse(null);
	}
}
