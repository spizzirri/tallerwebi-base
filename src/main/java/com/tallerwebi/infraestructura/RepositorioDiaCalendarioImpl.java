package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.calendario.DiaCalendario;
import com.tallerwebi.dominio.calendario.RepositorioDiaCalendario;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository("repositorioDiaCalendario")
public class RepositorioDiaCalendarioImpl implements RepositorioDiaCalendario {

    private SessionFactory sessionFactory;
    
    @Autowired
    public RepositorioDiaCalendarioImpl(SessionFactory sessionFactory){
        this.sessionFactory = sessionFactory;
    }
    
        private DiaCalendario diaCalendario;
        private Map<Integer, DiaCalendario> repositorio;

        public RepositorioDiaCalendarioImpl() {
            this.repositorio = new HashMap<>();
        }

        @Override
        public void guardar(DiaCalendario dia) {
            this.sessionFactory.getCurrentSession().save(dia);
        }





        
        @Override
        public DiaCalendario buscar(Integer id) {
            return repositorio.get(id);
        }

        @Override
        public void modificar(DiaCalendario dia) {
        }


}
