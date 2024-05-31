package com.weralf.literAlura.repository;

import com.weralf.literAlura.modelos.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibrosRepository extends JpaRepository<Libro, Long> {
    Optional<Libro> findByTituloContainsIgnoreCase(String nombreSerie);

    List<Libro> findByIdioma(String idioma);

}
