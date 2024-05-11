package com.tallerwebi.dominio.calendario;

public interface RepositorioCalendario {

        void guardar(ItemRendimiento dia);
        ItemRendimiento buscar(Integer id);
        void modificar(ItemRendimiento dia);

}
