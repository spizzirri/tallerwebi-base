package com.tallerwebi.dominio.usuario;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.presentacion.DatosItemRendimiento;

public interface ServicioLogin {

    Usuario consultarUsuario(String email, String password);
    void registrar(Usuario usuario) throws UsuarioExistente;

    DatosItemRendimiento obtenerItemMasSeleccionado();

    Reto obtenerRetoDisponible();
    Reto obtenerRetoEnProceso();

    Usuario modificarRachaRetoTerminado(Usuario usuario, long retoId);

    long calcularTiempoRestante(Long id);

    Reto cambiarReto(Long retoId, Usuario usuario);
}
