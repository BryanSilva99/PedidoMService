package com.example.pedidomservice.api.controller;

import com.example.pedidomservice.api.dto.*;
import com.example.pedidomservice.application.useCase.*;
import com.example.pedidomservice.domain.model.PedidoId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final CrearPedidoUseCase crear;
    private final ConsultarPedidoUseCase consultar;
    private final AgregarItemUseCase agregar;
    private final ConfirmarPedidoUseCase confirmar;
    private final ListarPedidosUseCase listar;

    public PedidoController(CrearPedidoUseCase crear, ConsultarPedidoUseCase consultar,
                            AgregarItemUseCase agregar, ConfirmarPedidoUseCase confirmar,
                            ListarPedidosUseCase listar) {
        this.crear = crear;
        this.consultar = consultar;
        this.agregar = agregar;
        this.confirmar = confirmar;
        this.listar = listar;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> crear() {
        PedidoResponse response = PedidoResponse.from(crear.ejecutar());
        return ResponseEntity.created(URI.create("/pedidos/" + response.id())).body(response);
    }

    @GetMapping
    public List<PedidoResponse> listar() {
        return listar.ejecutar().stream().map(PedidoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PedidoResponse consultar(@PathVariable UUID id) {
        return PedidoResponse.from(consultar.ejecutar(new PedidoId(id)));
    }

    @PostMapping("/{id}/items")
    public PedidoResponse agregar(@PathVariable UUID id, @RequestBody AgregarItemRequest request) {
        return PedidoResponse.from(agregar.ejecutar(new PedidoId(id), request.productoId(),
                request.cantidad(), request.precioUnitario()));
    }

    @PostMapping("/{id}/confirmar")
    public PedidoResponse confirmar(@PathVariable UUID id) {
        return PedidoResponse.from(confirmar.ejecutar(new PedidoId(id)));
    }
}
