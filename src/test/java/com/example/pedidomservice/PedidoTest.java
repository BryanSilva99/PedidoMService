package com.example.pedidomservice;

import com.example.pedidomservice.domain.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PedidoTest {
    @Test
    void crearAgregarYConfirmar() {
        Pedido pedido = new Pedido(PedidoId.nuevo());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertTrue(pedido.getItems().isEmpty());
        assertThrows(IllegalStateException.class, pedido::confirmar);
        pedido.agregarItem(new ItemPedido(new BigDecimal("50.00"), 2, 1L));
        assertEquals(new BigDecimal("100.00"), pedido.calcularTotal());
        pedido.confirmar();
        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
        assertThrows(IllegalStateException.class, pedido::confirmar);
        assertThrows(IllegalStateException.class,
                () -> pedido.agregarItem(new ItemPedido(BigDecimal.ZERO, 1, 2L)));
    }

    @Test
    void reconstruirConservaDatosYProtegeLaLista() {
        PedidoId id = PedidoId.nuevo();
        var items = new ArrayList<>(List.of(new ItemPedido(BigDecimal.TEN, 2, 1L)));
        Pedido pedido = Pedido.reconstruir(id, EstadoPedido.CONFIRMADO, items);
        items.clear();
        assertEquals(id, pedido.getId());
        assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());
        assertEquals(1, pedido.getItems().size());
        assertEquals(new BigDecimal("20"), pedido.calcularTotal());
        assertThrows(UnsupportedOperationException.class, () -> pedido.getItems().clear());
    }

    @Test
    void validaItemsYPermitePrecioCero() {
        assertDoesNotThrow(() -> new ItemPedido(BigDecimal.ZERO, 1, 1L));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(BigDecimal.TEN, 0, 1L));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(BigDecimal.TEN, -1, 1L));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(BigDecimal.ONE.negate(), 1, 1L));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(null, 1, 1L));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(BigDecimal.ONE, null, 1L));
        assertThrows(IllegalArgumentException.class, () -> new ItemPedido(BigDecimal.ONE, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(null));
        assertThrows(IllegalArgumentException.class, () -> new Pedido(PedidoId.nuevo()).agregarItem(null));
    }
}
