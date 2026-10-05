package com.example.pedidomservice.application.useCase;

import com.example.pedidomservice.domain.model.Pedido;
import com.example.pedidomservice.domain.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListarPedidosUseCase {
    private final PedidoRepository repository;

    public ListarPedidosUseCase(PedidoRepository repository) {
        this.repository = repository;
    }

    public List<Pedido> ejecutar() {
        return repository.listar();
    }
}
