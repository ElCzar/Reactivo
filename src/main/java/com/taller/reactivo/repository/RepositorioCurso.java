package com.taller.reactivo.repository;

import com.taller.reactivo.model.Curso;

import reactor.core.publisher.Mono;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioCurso extends R2dbcRepository<Curso, Integer> {
    @Query("SELECT c.* FROM Curso c WHERE c.materia_id = :materiaId AND c.estudiante_id = :estudianteId")
    Mono<Curso> findByMateriaIdAndEstudianteId(@Param("materiaId") Integer materiaId, @Param("estudianteId") Integer estudianteId);
}