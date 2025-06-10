package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ServicioRegistroImpl implements ServicioRegistro{

    private RepositorioUsuario repositorioUsuario;

    public ServicioRegistroImpl(RepositorioUsuario repositorioUsuario){
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public String registrarUsuario(Usuario usuario) throws UsuarioExistente {
        Usuario usuarioEncontrado = repositorioUsuario.buscar(usuario.getEmail());
        if(usuarioEncontrado != null){
            throw new UsuarioExistente("Email existente");
        }
        String encriptada = new BCryptPasswordEncoder().encode(usuario.getPassword());
        usuario.setPassword(encriptada);
        repositorioUsuario.guardar(usuario);
        return "El usuario se ha registrado con éxito";
    }
}
