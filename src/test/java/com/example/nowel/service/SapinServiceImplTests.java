package com.example.nowel.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.nowel.entity.BonDeCommande;
import com.example.nowel.entity.Client;
import com.example.nowel.entity.Decoration;
import com.example.nowel.entity.Sapin;
import com.example.nowel.repository.BonDeCommandeRepository;
import com.example.nowel.repository.ClientRepository;
import com.example.nowel.repository.DecorationRepository;
import com.example.nowel.repository.SapinRepository;

@ExtendWith(MockitoExtension.class)
class SapinServiceImplTests {

	@Mock
	private SapinRepository sapinRepository;

	@Mock
	private DecorationRepository decorationRepository;

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private BonDeCommandeRepository bonDeCommandeRepository;

	@InjectMocks
	private SapinServiceImpl sapinService;

	@Test
	void getSapinRetourneNullSiAbsent() {
		when(sapinRepository.findById(1L)).thenReturn(Optional.empty());

		assertThat(sapinService.getSapin(1L)).isNull();
	}

	@Test
	void createSapinRetourneLeNombreDeSapins() {
		when(sapinRepository.count()).thenReturn(4L);

		assertThat(sapinService.createSapin()).isEqualTo(4L);
		verify(sapinRepository).save(any(Sapin.class));
	}

	@Test
	void addDecorationAjouteSurUnSapinNonVendu() {
		Sapin sapin = new Sapin();
		Decoration decoration = decoration(10, 1);
		when(sapinRepository.findById(1L)).thenReturn(Optional.of(sapin));
		when(decorationRepository.findById(2L)).thenReturn(Optional.of(decoration));

		assertThat(sapinService.addDecoration(1L, 2L)).isTrue();
		assertThat(sapin.getDecorations()).containsExactly(decoration);
	}

	@Test
	void addDecorationRefuseUnSapinVendu() {
		Sapin sapin = new Sapin();
		sapin.setVendu(true);
		when(sapinRepository.findById(1L)).thenReturn(Optional.of(sapin));
		when(decorationRepository.findById(2L)).thenReturn(Optional.of(decoration(10, 1)));

		assertThat(sapinService.addDecoration(1L, 2L)).isFalse();
		assertThat(sapin.getDecorations()).isEmpty();
	}

	@Test
	void addDecorationRefuseUneDecorationInconnue() {
		when(sapinRepository.findById(1L)).thenReturn(Optional.of(new Sapin()));
		when(decorationRepository.findById(2L)).thenReturn(Optional.empty());

		assertThat(sapinService.addDecoration(1L, 2L)).isFalse();
	}

	@Test
	void vendreCalculeLesTotauxEtArrondiLesPointsAuPlusBas() {
		Sapin sapin = new Sapin();
		sapin.setDecorations(List.of(decoration(10, 1), decoration(9, 3)));
		Client client = new Client();
		client.setPointsFidelite(5);
		when(sapinRepository.findById(1L)).thenReturn(Optional.of(sapin));
		when(clientRepository.findById(2L)).thenReturn(Optional.of(client));
		when(bonDeCommandeRepository.save(any(BonDeCommande.class))).thenAnswer(appel -> appel.getArgument(0));

		BonDeCommande bonDeCommande = sapinService.vendre(1L, 2L);

		assertThat(bonDeCommande.getCoutTotal()).isEqualTo(19);
		assertThat(bonDeCommande.getPoidsTotal()).isEqualTo(4);
		assertThat(bonDeCommande.getAdresse()).isEqualTo("Maxime rue du bonjour");
		assertThat(bonDeCommande.getSapin()).isSameAs(sapin);
		assertThat(bonDeCommande.getClient()).isSameAs(client);
		assertThat(sapin.isVendu()).isTrue();
		assertThat(client.getPointsFidelite()).isEqualTo(6);
	}

	@Test
	void vendreRetourneNullSiDejaVendu() {
		Sapin sapin = new Sapin();
		sapin.setVendu(true);
		Client client = new Client();
		when(sapinRepository.findById(1L)).thenReturn(Optional.of(sapin));
		when(clientRepository.findById(2L)).thenReturn(Optional.of(client));

		assertThat(sapinService.vendre(1L, 2L)).isNull();
		assertThat(client.getPointsFidelite()).isZero();
		verify(bonDeCommandeRepository, never()).save(any());
	}

	@Test
	void vendreRetourneNullSiClientInconnu() {
		Sapin sapin = new Sapin();
		when(sapinRepository.findById(1L)).thenReturn(Optional.of(sapin));
		when(clientRepository.findById(2L)).thenReturn(Optional.empty());

		assertThat(sapinService.vendre(1L, 2L)).isNull();
		assertThat(sapin.isVendu()).isFalse();
	}

	private Decoration decoration(int valeur, int poids) {
		Decoration decoration = new Decoration();
		decoration.setValeur(valeur);
		decoration.setPoids(poids);
		return decoration;
	}
}
