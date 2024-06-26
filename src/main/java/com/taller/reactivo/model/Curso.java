package com.taller.reactivo.model;

import java.io.Serializable;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "curso")
public class Curso implements Serializable{
    @Id
    @org.springframework.data.annotation.Id
    private Integer materia_id;
    private Integer profesor_id;
    private String numero;
    private Integer estudiante_id;
    private LocalDate fecha_inicio;
    private LocalDate fecha_fin;
}
