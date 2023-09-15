package com.tallerwebi.dominio;

import java.util.List;

public interface RepositorioEscuela {

	void guardar(Escuela escuela);

	Escuela buscarPor(Long id);

	List<Escuela> buscarPor(String nombre);

}
