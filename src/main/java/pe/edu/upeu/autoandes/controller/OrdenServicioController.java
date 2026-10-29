package pe.edu.upeu.autoandes.controller;

import jakarta.validation.Valid;
import pe.edu.upeu.autoandes.dto.AnularOrdenRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenServicioRequestDTO;
import pe.edu.upeu.autoandes.dto.OrdenServicioResponseDTO;
import pe.edu.upeu.autoandes.enums.EstadoOrdenServicio;
import pe.edu.upeu.autoandes.service.service.OrdenServicioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ordenes")
public class OrdenServicioController {

    private final OrdenServicioService ordenServicioService;

    public OrdenServicioController(OrdenServicioService ordenServicioService) {
        this.ordenServicioService = ordenServicioService;
    }

    @PostMapping
    public ResponseEntity<OrdenServicioResponseDTO> registrar(
            @Valid @RequestBody OrdenServicioRequestDTO request,
            Principal principal
    ) {
        String usuarioRegistro = principal == null ? "desarrollo" : principal.getName();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ordenServicioService.registrar(request, usuarioRegistro));
    }

    @GetMapping
    public ResponseEntity<List<OrdenServicioResponseDTO>> listar(
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) EstadoOrdenServicio estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta
    ) {
        return ResponseEntity.ok(ordenServicioService.listar(placa, estado, desde, hasta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenServicioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordenServicioService.buscarPorId(id));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<OrdenServicioResponseDTO> anular(
            @PathVariable Long id,
            @Valid @RequestBody AnularOrdenRequestDTO request
    ) {
        return ResponseEntity.ok(ordenServicioService.anular(id, request));
    }
}
