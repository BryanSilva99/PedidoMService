package com.example.pedidomservice.api.dto;

import java.math.BigDecimal;

public record AgregarItemRequest(Long productoId, Integer cantidad, BigDecimal precioUnitario) {}
