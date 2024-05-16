package com.tallerwebi.dominio.calendario;

import com.tallerwebi.presentacion.DatosItemRendimiento;

import java.util.List;

public interface RepositorioCalendario {

        List<ItemRendimiento> obtenerItemsRendimiento();
        void guardar(ItemRendimiento dia);
        List<ItemRendimiento> obtenerItemsPorTipoRendimiento(TipoRendimiento tipoRendimiento);





        //nuevos a testear
        void actualizar(ItemRendimiento itemRendimiento);
        void vaciarCalendario();
        void eliminar(ItemRendimiento itemRendimiento);
        ItemRendimiento buscar(long id);
}
