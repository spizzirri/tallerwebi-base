package com.tallerwebi.infraestructura;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.tallerwebi.dominio.Escuela;
import com.tallerwebi.dominio.RepositorioEscuela;

@Repository
public class RepositorioEscuelaImpl implements RepositorioEscuela {

	private SessionFactory sessionFactory;

	@Autowired
	public RepositorioEscuelaImpl(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public void guardar(Escuela escuela) {
		sessionFactory.getCurrentSession().save(escuela);
	}

	@Override
	public Escuela buscarPor(Long id) {
		return sessionFactory.getCurrentSession().get(Escuela.class, id);
	}

	@Override
	public List<Escuela> buscarPor(String nombre) {
		return sessionFactory.getCurrentSession().createCriteria(Escuela.class)
				.add(Restrictions.eq("nombre", nombre))
				.list();
	}

}
