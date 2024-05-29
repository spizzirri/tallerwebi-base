package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.DiaCalendario;
import com.tallerwebi.dominio.RepositorioDiaCalendario;

import java.util.HashMap;
import java.util.Map;

public class RepositorioDiaCalendarioImpl implements RepositorioDiaCalendario {

        private DiaCalendario diaCalendario;
        private Map<Integer, DiaCalendario> repositorio;

        public RepositorioDiaCalendarioImpl() {
            this.repositorio = new HashMap<>();
        }

        @Override
        public void guardar(DiaCalendario dia) {
            repositorio.put(dia.getId(), dia);
        }


        @Override
        public DiaCalendario buscar(Integer id) {
            return repositorio.get(id);
        }

        @Override
        public void modificar(DiaCalendario dia) {
            if (repositorio.containsKey(dia.getId())) {
                repositorio.put(dia.getId(), dia);
            } else {
                throw new IllegalArgumentException("El día no existe en el repositorio.");
            }
        }



}
