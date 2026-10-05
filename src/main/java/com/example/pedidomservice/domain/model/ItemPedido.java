package com.example.pedidomservice.domain.model;

import java.math.BigDecimal;

public class ItemPedido {
    private final Long productoId;
    private final Integer cantidad;
    private final BigDecimal precioUnitario;

    public ItemPedido(BigDecimal precioUnitario, Integer cantidad, Long productoId) {

        if (productoId == null || cantidad == null || precioUnitario == null) {
            throw new IllegalArgumentException("Producto, cantidad y precio son obligatorios");
        }
        if(cantidad<=0){
            throw new IllegalArgumentException("la cantidad debe ser mayor a cero");
        }

        if(precioUnitario.compareTo(BigDecimal.ZERO)<0){
            throw new IllegalArgumentException("el precio unitario no puede ser negativo");
        }

        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.productoId = productoId;
    }

    //metodo para calcular el subtotal del item
    public BigDecimal calcularSubTotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }

    public Long getProductoId() {
        return productoId;
    }


    public Integer getCantidad() {
        return cantidad;
    }


    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }



}
