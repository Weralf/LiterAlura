package com.weralf.literAlura.repository;

import com.weralf.literAlura.modelos.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AutorRepository extends JpaRepository<Autor, Long> {
    Optional<Autor> findByNombre(String nombre);

    @Query("SELECT s FROM Autor s WHERE s.fechaNacimiento <= :año AND s.fechaFallecimiento >= :año")
    List<Autor> autoresVivosEnDeterminadoAño(int año);
}