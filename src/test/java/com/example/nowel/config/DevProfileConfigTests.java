package com.example.nowel.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Properties;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

class DevProfileConfigTests {

	private Properties devProperties() throws IOException {
		return PropertiesLoaderUtils.loadProperties(new ClassPathResource("application-dev.properties"));
	}

	@Test
	void leProfilDevEstActifParDefaut() throws IOException {
		Properties properties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("application.properties"));

		assertThat(properties.getProperty("spring.profiles.active")).isEqualTo("dev");
	}

	@Test
	void leProfilDevRespecteLaConsigne() throws IOException {
		Properties properties = devProperties();

		assertThat(properties.getProperty("server.port")).isEqualTo("8080");
		assertThat(properties.getProperty("spring.datasource.url")).endsWith("/nowelle_dev");
		assertThat(properties.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("update");
	}

	@Test
	void leMotDePasseVientDeLEnvironnement() throws IOException {
		assertThat(devProperties().getProperty("spring.datasource.password")).isEqualTo("${DB_PASSWORD}");
	}
}
