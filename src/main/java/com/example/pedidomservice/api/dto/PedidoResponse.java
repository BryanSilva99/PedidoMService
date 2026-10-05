package com.example.pedidomservice.api.dto;

import com.example.pedidomservice.domain.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(UUID id, EstadoPedido estado, List<ItemResponse> items, BigDecimal total) {
    public record ItemResponse(Long productoId, Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}

    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(pedido.getId().valor(), pedido.getEstado(),
                pedido.getItems().stream().map(item -> new ItemResponse(item.getProductoId(),
                        item.getCantidad(), item.getPrecioUnitario(), item.calcularSubTotal())).toList(),
                pedido.calcularTotal());
    }
}
