package com.tallerwebi.dominio;

public interface RepositorioDiaCalendario {

        void guardar(DiaCalendario dia);
        DiaCalendario buscar(Integer id);
        void modificar(DiaCalendario dia);

}
