package com.example.pedidomservice.application.useCase;

import com.example.pedidomservice.domain.model.Pedido;
import com.example.pedidomservice.domain.model.PedidoId;
import com.example.pedidomservice.domain.repository.PedidoRepository;
import org.springframework.stereotype.Service;

@Service
public class CrearPedidoUseCase {
    private final PedidoRepository pedidoRepository;

    public CrearPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido ejecutar() {

        PedidoId pedidoId = PedidoId.nuevo();
        Pedido pedido = new Pedido(pedidoId);

        return pedidoRepository.guardar(pedido);
    }
}
