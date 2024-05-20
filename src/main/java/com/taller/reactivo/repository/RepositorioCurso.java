package com.taller.reactivo.repository;

import com.taller.reactivo.model.Curso;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioCurso extends R2dbcRepository<Curso, Integer> {
}
