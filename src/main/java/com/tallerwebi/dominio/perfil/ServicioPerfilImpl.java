package com.tallerwebi.dominio.perfil;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
@Transactional
public class ServicioPerfilImpl implements ServicioPerfil {

    private RepositorioPerfil repositorioPerfil;

    public ServicioPerfilImpl(RepositorioPerfil repositorioPerfil){
        this.repositorioPerfil = repositorioPerfil;
    }

    @Override
    @Transactional
    public void guardarPerfil(Perfil perfil) {
        repositorioPerfil.guardarPerfil(perfil);
    }

    @Override
    @Transactional
    public Perfil obtenerPerfilPorId(Long id) {
        return repositorioPerfil.obtenerPerfilPorId(id);
    }

    @Override
    @Transactional
    public void actualizarPerfil(Long idPerfil, Perfil perfilActualizado) {
        // Verificar si el perfil con el id proporcionado existe en la base de datos
        Perfil perfilExistente = repositorioPerfil.obtenerPerfilPorId(idPerfil);
        if (perfilExistente == null) {
            throw new IllegalArgumentException("No se encontró ningún perfil con el ID proporcionado: " + idPerfil);
        }

        // Copiar los atributos actualizados al perfil existente
        perfilExistente.setEdad(perfilActualizado.getEdad());
        perfilExistente.setPeso(perfilActualizado.getPeso());
        perfilExistente.setAltura(perfilActualizado.getAltura());
        perfilExistente.setGenero(perfilActualizado.getGenero());
        perfilExistente.setObjetivoFitness(perfilActualizado.getObjetivoFitness());
        perfilExistente.setCondicionesAlternas(perfilActualizado.getCondicionesAlternas());
        perfilExistente.setExperienciaEjercicio(perfilActualizado.getExperienciaEjercicio());
        perfilExistente.setSuplementos(perfilActualizado.getSuplementos());

        // Actualizar el perfil en la base de datos
        repositorioPerfil.actualizarPerfil(perfilExistente);
    }

}
