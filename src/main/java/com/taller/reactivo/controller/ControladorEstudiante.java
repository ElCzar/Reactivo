package com.taller.reactivo.controller;

import com.taller.reactivo.model.Estudiante;
import com.taller.reactivo.repository.RepositorioEstudiante;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/")
public class ControladorEstudiante {
    @Autowired
    private RepositorioEstudiante repositorioEstudiante;

    // Read all
    @GetMapping("/estudiantes")
    public Flux<Estudiante> traeEstudiantes() {
        return repositorioEstudiante.findAll();
    }

    // Read one
    @GetMapping("/estudiantes/{id}")
    public Mono<ResponseEntity<Estudiante>> traeEstudiantePorId(@PathVariable Integer id) {
        return repositorioEstudiante.findById(id)
                .map(estudiante -> new ResponseEntity<>(estudiante, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Create
    @PostMapping("/crea")
    public Mono<Estudiante> creaEstudiante(@RequestBody Estudiante estudiante) {
        return repositorioEstudiante.save(estudiante);
    }

    // Update
    @PutMapping("/act/{id}")
    public Mono<ResponseEntity<Estudiante>> actualizaEstudiante(@PathVariable Integer id, @RequestBody Estudiante estudiante) {
        return repositorioEstudiante.findById(id)
                .flatMap(estudianteEncontrado -> {
                    estudianteEncontrado.setNombre(estudiante.getNombre());
                    estudianteEncontrado.setApellido(estudiante.getApellido());
                    estudianteEncontrado.setCorreo(estudiante.getCorreo());
                    return repositorioEstudiante.save(estudianteEncontrado);
                })
                .map(estudianteActualizado -> new ResponseEntity<>(estudianteActualizado, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete
    @DeleteMapping("/borra/{id}")
    public Mono<ResponseEntity<Void>> borraEstudiante(@PathVariable Integer id) {
        return repositorioEstudiante.findById(id)
                .flatMap(estudiante -> repositorioEstudiante.delete(estudiante)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
