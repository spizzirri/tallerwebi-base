package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.excepcion.UsuarioYaCargoSuRendimientoDelDiaException;
import com.tallerwebi.dominio.rutina.Rendimiento;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.util.List;

public interface ServicioCalendario {

    List<DatosItemRendimiento> obtenerItemsRendimiento();
    void guardarItemRendimiento(ItemRendimiento itemRendimiento);
    DatosItemRendimiento obtenerItemMasSeleccionado();
    void guardarItemRendimientoEnUsuario(ItemRendimiento itemRendimiento, Usuario usuario) throws UsuarioYaCargoSuRendimientoDelDiaException;

    List<DatosItemRendimiento> getItemsRendimientoDeUsuario(Usuario usuario);

    ItemRendimiento convertirRendimientoAItemRendimiento(Rendimiento rendimiento);

    boolean validarSiElUsuarioPuedeCargarRutinaHoy(Usuario usuario);
}
