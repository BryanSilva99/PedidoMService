package com.example.pedidomservice.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Pedido {
    private final PedidoId id;
    private EstadoPedido estado;
    private final List<ItemPedido> items;

    private Pedido(PedidoId id, EstadoPedido estado, List<ItemPedido> items) {
        if (id == null || estado == null || items == null || items.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Id, estado e items son obligatorios");
        }
        this.id = id;
        this.estado = estado;
        this.items = new ArrayList<>(items);
    }

    public Pedido(PedidoId id) {
        this(id, EstadoPedido.PENDIENTE, List.of());
    }

    public static Pedido reconstruir(PedidoId id, EstadoPedido estado, List<ItemPedido> items){
        return new Pedido(id, estado, items);
    }

    // Reglas del negocio

    public void agregarItem(ItemPedido item) {

        if(estado!=EstadoPedido.PENDIENTE)
            throw new IllegalStateException("Solo se pueden agregar productos a pedidos pendientes");

        if (item == null) throw new IllegalArgumentException("El item es obligatorio");
        items.add(item);
    }

    public BigDecimal calcularTotal() {
        return items.stream()
                .map(ItemPedido::calcularSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add); // acumula los subtotales, empezando desde cero
    }

    public void confirmar() {
        if(items.isEmpty()){
            throw new IllegalStateException("No se puede confirmar un pedido sin items");
        }

        if(estado!=EstadoPedido.PENDIENTE){
            throw new IllegalStateException("solo se puede confirmar un pedido pendiente");
        }

        estado = EstadoPedido.CONFIRMADO;
    }

    public void cancelar() {
        if(estado==EstadoPedido.CANCELADO){
            throw new IllegalStateException("el pedido ya se encuentra cancelado");
        }
        estado = EstadoPedido.CANCELADO;
    }

    public PedidoId getId() {
        return id;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<ItemPedido> getItems() {
        return List.copyOf(items);
    }
}
