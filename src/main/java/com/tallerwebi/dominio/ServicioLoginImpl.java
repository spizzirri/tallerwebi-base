package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioLogin")
@Transactional
public class ServicioLoginImpl implements ServicioLogin {

    private ServicioCalendario servicioCalendario;
    private RepositorioUsuario repositorioUsuario;
    private ServicioReto servicioReto;

    @Autowired
    public ServicioLoginImpl(RepositorioUsuario repositorioUsuario, ServicioCalendario servicioCalendario, ServicioReto servicioReto) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicioCalendario = servicioCalendario;
        this.servicioReto = servicioReto;
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

    @Override
    public Reto obtenerRetoDisponible(){return servicioReto.obtenerRetoDisponible();}

}

