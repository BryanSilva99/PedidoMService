package com.example.pedidomservice.application.useCase;

import com.example.pedidomservice.domain.model.*;
import com.example.pedidomservice.domain.repository.PedidoRepository;
import org.springframework.stereotype.Service;

@Service
public class AgregarItemUseCase {
    private final PedidoRepository repository;

    public AgregarItemUseCase(PedidoRepository repository) {
        this.repository = repository;
    }

    public Pedido ejecutar(PedidoId id, Long productoId, Integer cantidad, java.math.BigDecimal precioUnitario) {
        Pedido pedido = repository.buscarPorId(id).orElseThrow(PedidoNoEncontradoException::new);
        pedido.agregarItem(new ItemPedido(precioUnitario, cantidad, productoId));
        return repository.guardar(pedido);
    }
}
