package com.taller.reactivo.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Promedio {
    private Integer materia_id;
    private String nombre;
    private Double promedio;
}
