package com.tallerwebi.dominio;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.UsuarioDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioLoginImpl(RepositorioUsuario repositorioUsuario){
        this.repositorioUsuario = repositorioUsuario;
    }

    @Override
    public UsuarioDto consultarUsuario (String email, String password) {
        Usuario usuario = this.repositorioUsuario.buscarUsuarioPorEmailYPassword(email, password);
        if (usuario != null) {
        return new UsuarioDto(usuario.getNombre(), usuario.getApellido(), usuario.getEmail(),  usuario.getTelefono(), usuario.getDni());
        } else {
            return null;
        }
    }

    @Override
    public void registrar(Usuario usuario) throws UsuarioExistente {
        Usuario usuarioEncontrado = repositorioUsuario.buscarUsuario(usuario.getEmail());
        if(usuarioEncontrado != null){
            throw new UsuarioExistente("El usuario ya existe en el sistema, por favor ingrese con otro correo electrónico.");
        }
        repositorioUsuario.guardar(usuario);
    }

}

