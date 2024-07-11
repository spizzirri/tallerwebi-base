package com.tallerwebi.dominio.excepcion;

public class UsuarioYaCargoSuRendimientoDelDiaException extends Exception {
    public UsuarioYaCargoSuRendimientoDelDiaException(String s) {
        super(s);
    }
    public UsuarioYaCargoSuRendimientoDelDiaException() {
        super("El usuario ya cargo su rendimiento, espere hasta mañana.");
    }
}
