package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.objetivo.ObjetivoUsuario;

import java.util.HashMap;
import java.util.Map;

public class ObjetivoUsuarioRepository {
    private final Map<Long, ObjetivoUsuario> objetivoUsuarios = new HashMap<>();
    private long nextId = 1;

    public void save(ObjetivoUsuario objetivoUsuario) {
        objetivoUsuario.setId(nextId++);
        objetivoUsuarios.put(objetivoUsuario.getId(), objetivoUsuario);
    }
}
