package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

    private ServicioCalendario servicioCalendario;
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    public ServicioLoginImpl(RepositorioUsuario repositorioUsuario, ServicioCalendario servicioCalendario) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioCalendario = servicioCalendario;
    }

    @Override
    public Usuario consultarUsuario (String email, String password) {
        return repositorioUsuario.buscarUsuario(email, password);
    }

    @Override
    public void registrar(Usuario usuario) throws UsuarioExistente {
        Usuario usuarioEncontrado = repositorioUsuario.buscarUsuario(usuario.getEmail(), usuario.getPassword());
        if(usuarioEncontrado != null){
            throw new UsuarioExistente();
        }
        repositorioUsuario.guardar(usuario);
    }

    @Override
    public DatosItemRendimiento obtenerItemMasSeleccionado() {
        return servicioCalendario.obtenerItemMasSeleccionado();
    }

}

