package com.tallerwebi.dominio.calendario;

import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.util.List;

public interface RepositorioCalendario {

        List<ItemRendimiento> obtenerItemsRendimiento();
        ItemRendimiento guardar(ItemRendimiento dia);
        ItemRendimiento buscar(long id);
        void actualizar(ItemRendimiento itemRendimiento);
        void vaciarCalendario();
        //nuevos a testear
        List<ItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento);
        void eliminar(ItemRendimiento itemRendimiento);

}
