package com.taller.reactivo.repository;

import com.taller.reactivo.model.Estudiante;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioEstudiante extends R2dbcRepository<Estudiante, Integer> {
}