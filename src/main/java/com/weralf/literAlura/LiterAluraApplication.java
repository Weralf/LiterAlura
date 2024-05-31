package com.weralf.literAlura;

import com.weralf.literAlura.PRINCIPAL.principal;
import com.weralf.literAlura.repository.AutorRepository;
import com.weralf.literAlura.repository.LibrosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.security.Principal;

@SpringBootApplication
public class LiterAluraApplication implements CommandLineRunner {
	@Autowired
	private LibrosRepository repository;
	@Autowired
	private AutorRepository autorRepository;
	public static void main(String[] args) {
		SpringApplication.run(LiterAluraApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		principal principal = new principal(repository, autorRepository);
		principal.muestraElMenu();
	}
}
