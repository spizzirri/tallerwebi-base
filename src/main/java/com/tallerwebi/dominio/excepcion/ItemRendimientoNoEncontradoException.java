package com.tallerwebi.dominio.excepcion;

public class ItemRendimientoNoEncontradoException extends Throwable {
    public ItemRendimientoNoEncontradoException(Long id) {
        super("ItemRendimiento no encontrado.");
    }
}
