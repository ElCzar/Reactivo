package com.taller.reactivo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        // If not found throw exception
        return repositorioNota.findByEstudianteId(id)
        .switchIfEmpty(Mono.error(new RegisterNotFoundException("No se encontraron notas para el estudiante")));
    }

    // Read single note
    @GetMapping("/estudiantes/{id}/notas/{idNota}")
    public Flux<Nota> traeNotaPorEstudiante(@PathVariable Integer id, @PathVariable Integer idNota) {
        return repositorioNota.findByEstudianteId(id)
                .filter(nota -> nota.getId().equals(idNota))
                .switchIfEmpty(Mono.error(new RegisterNotFoundException("No se encontró la nota")));
    }

    private Mono<Curso> traeCurso(Integer id) {
        return repositorioCurso.findById(id)
                .switchIfEmpty(Mono.error(new RegisterNotFoundException("No se encontró el curso")));
    }

    // Read all average notes (if all notes percentages are 100)
    @GetMapping("/estudiantes/{id}/promedios")
    public Flux<Promedio> traePromediosPorEstudiante(@PathVariable Integer id) {
        return repositorioNota.findByEstudianteId(id)
                .collectMultimap(Nota::getMateria_id)
                .flatMapMany(notasPorCurso -> Flux.fromIterable(notasPorCurso.keySet())
                        .flatMap(idCurso -> traeCurso(idCurso)
                                .flatMap(curso -> {
                                    var notas = notasPorCurso.get(idCurso);
                                    if (notas.stream().mapToDouble(Nota::getPorcentaje).sum() == 100.0) {
                                        double promedio = notas.stream().mapToDouble(nota -> nota.getValor() * nota.getPorcentaje() / 100).sum();
                                        return Mono.just(new Promedio(idCurso, curso.getNumero(), promedio));
                                    } else {
                                        return Mono.empty();
                                    }
                                }))
                );
    }

    // Read single average note
    @GetMapping("/estudiantes/{id}/promedios/{idCurso}")
    public Mono<Promedio> traePromedioPorEstudiante(@PathVariable Integer id, @PathVariable Integer idCurso) {
        return repositorioNota.findByEstudianteId(id)
                .filter(nota -> nota.getMateria_id().equals(idCurso))
                .collectList()
                .flatMap(notas -> traeCurso(idCurso)
                        .flatMap(curso -> {
                            if (notas.stream().mapToDouble(Nota::getPorcentaje).sum() == 100.0) {
                                double promedio = notas.stream().mapToDouble(nota -> nota.getValor() * nota.getPorcentaje() / 100).sum();
                                return Mono.just(new Promedio(idCurso, curso.getNumero(), promedio));
                            } else {
                                return Mono.error(new RegisterNotFoundException("No se encontró el promedio"));
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
        return traeCurso(nota.getMateria_id())
                .flatMap(curso -> repositorioNota.findByEstudianteId(nota.getEstudiante().getId())
                        .collectList()
                        .flatMap(notas -> {
                            if (notas.stream().mapToDouble(Nota::getPorcentaje).sum() + nota.getPorcentaje() <= 100) {
                                return repositorioNota.save(nota);
                            } else {
                                return Mono.error(new RegisterNotFoundException("El porcentaje total de las notas no puede ser mayor a 100"));
                            }
                        })
                );
    }

    // Update note
    @PostMapping("/actNota/{id}")
    public Mono<ResponseEntity<Nota>> actualizaNota(@PathVariable Integer id, @RequestBody Nota nota) {
        // If updated note makes the total percentage of notes greater than 100, throw exception
        return traeCurso(nota.getMateria_id())
                .flatMap(curso -> repositorioNota.findByEstudianteId(nota.getEstudiante().getId())
                        .collectList()
                        .flatMap(notas -> {
                            if (notas.stream().mapToDouble(Nota::getPorcentaje).sum() + nota.getPorcentaje() <= 100) {
                                return repositorioNota.findById(id)
                                        .flatMap(notaEncontrada -> {
                                            notaEncontrada.setMateria_id(nota.getMateria_id());
                                            notaEncontrada.setProfesor_id(nota.getProfesor_id());
                                            notaEncontrada.setObservacion(nota.getObservacion());
                                            notaEncontrada.setValor(nota.getValor());
                                            notaEncontrada.setPorcentaje(nota.getPorcentaje());
                                            notaEncontrada.setEstudiante(nota.getEstudiante());
                                            return repositorioNota.save(notaEncontrada);
                                        })
                                        .map(notaActualizada -> new ResponseEntity<>(notaActualizada, HttpStatus.OK))
                                        .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
                            } else {
                                return Mono.error(new RegisterNotFoundException("El porcentaje total de las notas no puede ser mayor a 100"));
                            }
                        })
                );
    }

    // Delete note
    @GetMapping("/borraNota/{id}")
    public Mono<ResponseEntity<Void>> borraNota(@PathVariable Integer id) {
        return repositorioNota.findById(id)
                .flatMap(nota -> repositorioNota.delete(nota)
                        .then(Mono.just(new ResponseEntity<Void>(HttpStatus.OK))))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
