package com.tallerwebi.infraestructura;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.tallerwebi.dominio.Escuela;
import com.tallerwebi.dominio.RepositorioEscuela;
import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { SpringWebTestConfig.class, HibernateTestConfig.class })

public class RepositorioEscuelaTest {

	@Autowired
	private RepositorioEscuela repositorio;

	@Transactional
	@Rollback
	@Test
	public void guardarEscuelaDeberiaPersistirla() {

		Escuela escuela = new Escuela();

		repositorio.guardar(escuela);

		Escuela buscada = repositorio.buscarPor(escuela.getId());

		assertThat(buscada, is(notNullValue()));

	}
	
	@Transactional
	@Rollback
	@Test
	public void buscarPorNombre() {

		Escuela escuela = new Escuela();
		escuela.setNombre("Elmina Paz de Gallo");
		repositorio.guardar(escuela);
		
		Escuela escuela2 = new Escuela();
		escuela2.setNombre("Aviador Matienzo");
		repositorio.guardar(escuela2);

		List<Escuela> buscadas = repositorio.buscarPor(escuela2.getNombre());

		assertThat(buscadas,hasSize(1));

	}
}
