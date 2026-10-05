package com.example.pedidomservice.infrastructure.persistence;

import com.example.pedidomservice.domain.model.*;
import com.example.pedidomservice.domain.repository.PedidoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;

@Repository
@Transactional
public class JpaPedidoRepository implements PedidoRepository {
    private final SpringDataPedidoRepository repository;

    public JpaPedidoRepository(SpringDataPedidoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        PedidoJpaEntity entity = new PedidoJpaEntity(pedido.getId().valor(), pedido.getEstado());
        for (ItemPedido item : pedido.getItems()) {
            entity.agregarItem(new ItemPedidoJpaEntity(
                    item.getProductoId(), item.getCantidad(), item.getPrecioUnitario()));
        }
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarPorId(PedidoId id) {
        return repository.findById(id.valor()).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listar() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    private Pedido toDomain(PedidoJpaEntity entity) {
        return Pedido.reconstruir(new PedidoId(entity.getId()), entity.getEstado(),
                entity.getItems().stream().map(item -> new ItemPedido(
                        item.getPrecioUnitario(), item.getCantidad(), item.getProductoId())).toList());
    }

    @Override
    public void eliminar(PedidoId id) {
        repository.deleteById(id.valor());
    }
}
