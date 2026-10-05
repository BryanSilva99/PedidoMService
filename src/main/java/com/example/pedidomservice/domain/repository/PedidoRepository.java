package com.example.pedidomservice.domain.repository;

import com.example.pedidomservice.domain.model.Pedido;
import com.example.pedidomservice.domain.model.PedidoId;

import java.util.Optional;
import java.util.List;

public interface PedidoRepository {

    Pedido guardar(Pedido pedido);
    Optional<Pedido> buscarPorId(PedidoId id);
    List<Pedido> listar();
    void eliminar(PedidoId id);
}
