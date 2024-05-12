package com.tallerwebi.dominio.calendario;

import com.tallerwebi.dominio.excepcion.ItemRendimientoNoEncontradoException;

public interface RepositorioCalendario {

        void guardar(ItemRendimiento dia);
        ItemRendimiento buscar(Integer id);
        void modificar(ItemRendimiento dia);
        void actualizar(ItemRendimiento itemRendimiento);

}
