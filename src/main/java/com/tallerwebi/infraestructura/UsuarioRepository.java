package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;

import java.util.HashMap;
import java.util.Map;

public class UsuarioRepository {
    private final Map<String, Usuario> usuarios = new HashMap<>();

    public Usuario findByEmail(String email) {
        return usuarios.get(email);
    }

    public void save(Usuario usuario) {
        usuarios.put(usuario.getEmail(), usuario);
    }
}
