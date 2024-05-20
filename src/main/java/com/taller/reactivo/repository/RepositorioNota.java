package com.taller.reactivo.repository;

import com.taller.reactivo.model.Nota;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioNota extends R2dbcRepository<Nota, Integer> {
}