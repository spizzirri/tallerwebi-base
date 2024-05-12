package com.tallerwebi.dominio.calendario;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
public class ServicioItemRendimientoImpl implements ServicioItemRendimiento {

    private RepositorioCalendario repositorioCalendario;
    private List<ItemRendimiento> calendarioItems;

    public ServicioItemRendimientoImpl(RepositorioCalendario repositorioCalendario){
        this.repositorioCalendario = repositorioCalendario;
    }

    @Override
    public List<ItemRendimiento> obtenerItems() {
        return List.of();
    }

    @Override
    public List<ItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento) {
        return repositorioCalendario.buscarPorTipoRendimiento(tipoRendimiento);
    }

    @Override
    public ItemRendimiento getItemPorId(Long id) {
        return repositorioCalendario.buscar(id);
    }

    @Override
    public ItemRendimiento guardarItemRendimiento(ItemRendimiento itemRendimiento) {
        return repositorioCalendario.guardar(itemRendimiento);
    }

    @Override
    public ItemRendimiento actualizarItemRendimiento(ItemRendimiento itemRendimiento) {
        return repositorioCalendario.guardar(itemRendimiento);
    }

    @Override
    public void eliminarItemRendimiento(ItemRendimiento dia) {
        repositorioCalendario.eliminar(dia);
    }

    @Override
    public List<TipoRendimiento> obtenerOpcionesRendimiento() {
        return List.of();
    }


}
