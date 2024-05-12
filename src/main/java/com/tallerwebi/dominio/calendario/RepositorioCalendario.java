package com.tallerwebi.dominio.calendario;

public interface RepositorioCalendario {

        void guardar(ItemRendimiento dia);
        ItemRendimiento buscar(long id);
        void actualizar(ItemRendimiento itemRendimiento);
        void vaciarCalendario();

}
