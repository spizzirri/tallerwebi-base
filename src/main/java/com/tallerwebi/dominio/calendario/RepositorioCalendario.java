package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface RepositorioCalendario {

        List<ItemRendimiento> obtenerItemsRendimiento();

        void guardar(ItemRendimiento dia);

        public Usuario getUsuarioById(Long id);

        void guardarRendimientoEnUsuario(ItemRendimiento rendimiento, Usuario usuario);

        List<ItemRendimiento> getItemsRendimientoDeUsuario(Usuario usuario);

        boolean existeItemRendimientoPorFecha(LocalDate fecha);

        ItemRendimiento obtenerItemMasSeleccionado();

        ItemRendimiento getItemRendimientoDeUsuarioHoyPorId(Long id);

}
