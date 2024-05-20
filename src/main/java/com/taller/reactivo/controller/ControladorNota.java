package com.taller.reactivo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taller.reactivo.exceptions.RegisterNotFoundException;
import com.taller.reactivo.model.Curso;
import com.taller.reactivo.model.Nota;
import com.taller.reactivo.model.Promedio;
import com.taller.reactivo.repository.RepositorioCurso;
import com.taller.reactivo.repository.RepositorioNota;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/")
public class ControladorNota {
    @Autowired
    private RepositorioNota repositorioNota;
    @Autowired
    private RepositorioCurso repositorioCurso;

    // Read all notes
    @GetMapping("/estudiantes/{id}/notas")
    public Flux<Nota> traeNotasPorEstudiante(@PathVariable Integer id) {
        // Get the notes from the table and filter them by student id
        return repositorioNota.findAll().filter(nota -> nota.getEstudiante_id().equals(id));
    }

    // Read single note
    @GetMapping("/estudiantes/{id}/notas/{idNota}")
    public Flux<Nota> traeNotaPorEstudiante(@PathVariable Integer id, @PathVariable Integer idNota) {
        return repositorioNota.findAll().filter(nota -> nota.getEstudiante_id().equals(id) && nota.getId().equals(idNota));
    }

    private Mono<Curso> traeCurso(Integer id, Integer estudiante_id) {
        return repositorioCurso.findByMateriaIdAndEstudianteId(id, estudiante_id)
                .switchIfEmpty(Mono.error(new RegisterNotFoundException("No se encontró el curso")));
    }

    // Read all average notes (if all notes percentages are 100)
    @GetMapping("/estudiantes/{id}/promedios")
    public Flux<Promedio> traePromediosPorEstudiante(@PathVariable Integer id) {
        // Get all notes from the student and group them by course
        return repositorioNota.findAll()
                .filter(nota -> nota.getEstudiante_id().equals(id))
                .collectList()
                .flatMapMany(notas -> Flux.fromIterable(notas)
                        .groupBy(Nota::getMateria_id)
                        .flatMap(group -> group.collectList()
                                .flatMap(notasPorCurso -> traeCurso(group.key(), id)
                                        .flatMap(curso -> {
                                            if (notasPorCurso.stream().mapToDouble(Nota::getPorcentaje).sum() == 100.0) {
                                                double promedio = notasPorCurso.stream().mapToDouble(nota -> nota.getValor() * nota.getPorcentaje() / 100).sum();
                                                return Mono.just(new Promedio(group.key(), curso.getNumero(), promedio));
                                            } else {
                                                return Mono.empty();
                                            }
                                        })
                                )
                        )
                );
    }

    // Read single average note
    @GetMapping("/estudiantes/{id}/promedios/{idCurso}")
    public Mono<Promedio> traePromedioPorEstudiante(@PathVariable Integer id, @PathVariable Integer idCurso) {
        return repositorioNota.findAll()
                .filter(nota -> nota.getEstudiante_id().equals(id) && nota.getMateria_id().equals(idCurso))
                .collectList()
                .flatMap(notasPorCurso -> traeCurso(idCurso, id)
                        .flatMap(curso -> {
                            if (notasPorCurso.stream().mapToDouble(Nota::getPorcentaje).sum() == 100.0) {
                                double promedio = notasPorCurso.stream().mapToDouble(nota -> nota.getValor() * nota.getPorcentaje() / 100).sum();
                                return Mono.just(new Promedio(idCurso, curso.getNumero(), promedio));
                            } else {
                                return Mono.error(new RegisterNotFoundException("El porcentaje total de las notas no es 100"));
                            }
                        })
                );
    }

    // Read all notes
    @GetMapping("/notas")
    public Flux<Nota> traeNotas() {
        return repositorioNota.findAll();
    }

    // Read single note
    @GetMapping("/notas/{id}")
    public Mono<ResponseEntity<Nota>> traeNotaPorId(@PathVariable Integer id) {
        return repositorioNota.findById(id)
                .map(nota -> new ResponseEntity<>(nota, HttpStatus.OK))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Create note
    @PostMapping("/creaNota")
    public Mono<Nota> creaNota(@RequestBody Nota nota) {
        // If new note makes the total percentage of notes greater than 100, throw exception do not use flatMap with findEstudianteById
        if (nota.getPorcentaje() >= 100 || nota.getPorcentaje() <= 0 || nota.getValor() <= 0 || nota.getValor() >= 5){
            return Mono.error(new RegisterNotFoundException("El porcentaje de la nota no puede ser mayor a 100"));
        }

        // If there is no course with the given characteristics, throw exception
        if (repositorioCurso.findByMateriaIdAndEstudianteId(nota.getMateria_id(), nota.getEstudiante_id()) == null) {
            return Mono.error(new RegisterNotFoundException("No se encontró el curso"));
        }

        return repositorioNota.findAll()
                .filter(notaExistente -> notaExistente.getEstudiante_id().equals(nota.getEstudiante_id()) && notaExistente.getMateria_id().equals(nota.getMateria_id()))
                .collectList()
                .flatMap(notas -> {
                    if (notas.stream().mapToDouble(Nota::getPorcentaje).sum() + nota.getPorcentaje() <= 100) {
                        return repositorioNota.save(nota);
                    } else {
                        return Mono.error(new RegisterNotFoundException("El porcentaje total de las notas no puede ser mayor a 100"));
                    }
                });
    }

    // Update note
    @PostMapping("/actNota/{id}")
    public Mono<ResponseEntity<Nota>> actualizaNota(@PathVariable Integer id, @RequestBody Nota nota) {
        // If updated note makes the total percentage of notes greater than 100, throw exception
        return repositorioNota.findById(id)
                .flatMap(notaEncontrada -> {
                    notaEncontrada.setEstudiante_id(nota.getEstudiante_id());
                    notaEncontrada.setMateria_id(nota.getMateria_id());
                    notaEncontrada.setValor(nota.getValor());
                    notaEncontrada.setPorcentaje(nota.getPorcentaje());
                    return repositorioNota.findAll()
                            .filter(notaExistente -> notaExistente.getEstudiante_id().equals(nota.getEstudiante_id()) && notaExistente.getMateria_id().equals(nota.getMateria_id()) && !notaExistente.getId().equals(id))
                            .collectList()
                            .flatMap(notas -> {
                                if (notas.stream().mapToDouble(Nota::getPorcentaje).sum() + nota.getPorcentaje() <= 100) {
                                    return repositorioNota.save(notaEncontrada)
                                            .map(notaActualizada -> new ResponseEntity<>(notaActualizada, HttpStatus.OK));
                                } else {
                                    return Mono.error(new RegisterNotFoundException("El porcentaje total de las notas no puede ser mayor a 100"));
                                }
                            });
                })
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // Delete note
    @DeleteMapping("/borraNota/{id}")
    public Mono<ResponseEntity<Void>> borraNota(@PathVariable Integer id) {
        return repositorioNota.findById(id)
                .flatMap(nota -> repositorioNota.delete(nota)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
