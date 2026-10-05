package com.example.pedidomservice;

import com.example.pedidomservice.domain.model.*;
import com.example.pedidomservice.domain.repository.PedidoRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import com.example.pedidomservice.api.controller.PedidoController;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

// Requiere MySQL configurado mediante DB_URL, DB_USERNAME y DB_PASSWORD.
@SpringBootTest
@Transactional
class PedidoMServiceApplicationTests {
    @Autowired PedidoRepository repository;
    @Autowired EntityManager entityManager;
    @Autowired PedidoController controller;

    @Test
    void listarPedidosDevuelveElMismoContratoQueLaConsultaIndividual() throws Exception {
        Pedido pendiente = new Pedido(PedidoId.nuevo());
        Pedido confirmado = new Pedido(PedidoId.nuevo());
        confirmado.agregarItem(new ItemPedido(new BigDecimal("50.00"), 2, 1L));
        confirmado.confirmar();
        repository.guardar(pendiente);
        repository.guardar(confirmado);
        entityManager.flush();
        entityManager.clear();

        var mvc = MockMvcBuilders.standaloneSetup(controller).build();
        var listado = mvc.perform(get("/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[*].id", hasItem(pendiente.getId().valor().toString())))
                .andExpect(jsonPath("$[*].id", hasItem(confirmado.getId().valor().toString())))
                .andReturn().getResponse().getContentAsString();
        for (Pedido pedido : java.util.List.of(pendiente, confirmado)) {
            var individual = mvc.perform(get("/pedidos/" + pedido.getId().valor()))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            assertTrue(listado.contains(individual));
        }
    }

    @Test
    void guardarRecuperarYActualizarConservaItemsYEstado() {
        Pedido pedido = new Pedido(PedidoId.nuevo());
        repository.guardar(pedido);
        entityManager.flush();
        entityManager.clear();

        Pedido recuperado = repository.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(EstadoPedido.PENDIENTE, recuperado.getEstado());
        assertTrue(recuperado.getItems().isEmpty());
        recuperado.agregarItem(new ItemPedido(new BigDecimal("50.00"), 2, 1L));
        repository.guardar(recuperado);
        entityManager.flush();
        entityManager.clear();

        recuperado = repository.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(1, recuperado.getItems().size());
        assertEquals(1L, recuperado.getItems().getFirst().getProductoId());
        assertEquals(2, recuperado.getItems().getFirst().getCantidad());
        assertEquals(0, new BigDecimal("100.00").compareTo(recuperado.calcularTotal()));
        recuperado.confirmar();
        repository.guardar(recuperado);
        entityManager.flush();
        entityManager.clear();

        recuperado = repository.buscarPorId(pedido.getId()).orElseThrow();
        assertEquals(EstadoPedido.CONFIRMADO, recuperado.getEstado());
        assertEquals(1, recuperado.getItems().size());
        assertEquals(0, new BigDecimal("100.00").compareTo(recuperado.calcularTotal()));
    }
}
