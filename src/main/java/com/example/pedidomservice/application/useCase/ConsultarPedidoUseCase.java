package com.example.pedidomservice.application.useCase;

import com.example.pedidomservice.domain.model.*;
import com.example.pedidomservice.domain.repository.PedidoRepository;
import org.springframework.stereotype.Service;

@Service
public class ConsultarPedidoUseCase {
    private final PedidoRepository repository;

    public ConsultarPedidoUseCase(PedidoRepository repository) {
        this.repository = repository;
    }

    public Pedido ejecutar(PedidoId id) {
        Pedido pedido = repository.buscarPorId(id).orElseThrow(PedidoNoEncontradoException::new);
        return pedido;
    }
}
