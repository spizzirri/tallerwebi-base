package com.tallerwebi.dominio.calendario;

import java.util.List;

public interface RepositorioCalendario {

        ItemRendimiento guardar(ItemRendimiento dia);
        ItemRendimiento buscar(long id);
        void actualizar(ItemRendimiento itemRendimiento);
        void vaciarCalendario();
        //nuevos a testear
        List<ItemRendimiento> buscarPorTipoRendimiento(TipoRendimiento tipoRendimiento);
        void eliminar(ItemRendimiento itemRendimiento);
}
