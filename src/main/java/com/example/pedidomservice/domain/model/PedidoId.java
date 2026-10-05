package com.example.pedidomservice.domain.model;

import java.util.UUID;

public record PedidoId(UUID valor) {

    public PedidoId{
        if (valor == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
    }

    public static PedidoId nuevo(){
        return new PedidoId(UUID.randomUUID());
    }
}
