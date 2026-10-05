package com.example.pedidomservice.application.useCase;

import com.example.pedidomservice.domain.model.Pedido;
import com.example.pedidomservice.domain.model.PedidoId;
import com.example.pedidomservice.domain.repository.PedidoRepository;
import org.springframework.stereotype.Service;

@Service
public class ConfirmarPedidoUseCase {
    private final PedidoRepository pedidoRepository;

    public ConfirmarPedidoUseCase(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    public Pedido ejecutar(PedidoId pedidoId) {
        Pedido pedido = pedidoRepository
                .buscarPorId(pedidoId)
                .orElseThrow(PedidoNoEncontradoException::new);
        pedido.confirmar();
        return pedidoRepository.guardar(pedido);
    }
}
