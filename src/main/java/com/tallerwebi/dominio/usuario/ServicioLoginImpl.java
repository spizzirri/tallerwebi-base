package com.tallerwebi.dominio.usuario;

import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.excepcion.NoCambiosRestantesException;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import com.tallerwebi.dominio.reto.Reto;
import com.tallerwebi.dominio.reto.ServicioReto;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.NoSuchElementException;

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

    @Override
    public Reto obtenerRetoEnProceso() {
        return servicioReto.obtenerRetoEnProceso();
    }

    @Override
    public Usuario modificarRachaRetoTerminado(Usuario usuario, long retoId) {
        long diasPasados = servicioReto.terminarReto(retoId);
        if (usuario != null) {
            if (diasPasados < 2) {
                usuario.sumarRacha();
                repositorioUsuario.modificar(usuario);
            } else {
                usuario.setRachaDeRetos(0); // Resetear la racha
                repositorioUsuario.modificar(usuario); // Actualizar el usuario en el repositorio
            }
        }
        return usuario;
    }

    @Override
    public long calcularTiempoRestante(Long id) {
        return servicioReto.calcularTiempoRestante(id);
    }

    @Override
    public Reto cambiarReto(Long retoId, Usuario usuario) {
        if (usuario.getCambioReto() > 0) {
            Reto retoActual = servicioReto.obtenerRetoPorId(retoId);
            if (retoActual != null) {
                servicioReto.cambiarReto(retoActual);
            }

            Reto nuevoReto = servicioReto.obtenerRetoDisponible();
            if (nuevoReto != null) {
                usuario.setCambioReto(usuario.getCambioReto() - 1);
                repositorioUsuario.modificar(usuario);
            } else {
                throw new NoSuchElementException("No se encontró un nuevo reto disponible.");
            }
            return nuevoReto;
        } else {
            throw new NoCambiosRestantesException("No te quedan cambios. Debes terminar los retos.");
        }
    }






}

