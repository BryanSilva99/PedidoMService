package com.example.pedidomservice.application.useCase;

public class PedidoNoEncontradoException extends RuntimeException {
    public PedidoNoEncontradoException() {
        super("Pedido no encontrado");
    }
}
