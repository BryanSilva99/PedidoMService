package com.example.pedidomservice.infrastructure.persistence;

import com.example.pedidomservice.domain.model.EstadoPedido;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "pedidos")
public class PedidoJpaEntity {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    protected PedidoJpaEntity() {
    }

    public PedidoJpaEntity(UUID id,EstadoPedido estado){
        this.id = id;
        this.estado = estado;
    }

    @OneToMany(mappedBy = "pedido",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<ItemPedidoJpaEntity> items = new ArrayList<>();

    public void agregarItem(ItemPedidoJpaEntity item) {
        items.add(item);
        item.setPedido(this);
    }

    public List<ItemPedidoJpaEntity> getItems() {
        return items;
    }

    public UUID getId() {
        return id;
    }

    public EstadoPedido getEstado() {
        return estado;
    }
}
