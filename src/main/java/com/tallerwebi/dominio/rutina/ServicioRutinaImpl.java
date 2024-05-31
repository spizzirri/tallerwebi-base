package com.tallerwebi.dominio.rutina;
import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.objetivo.Objetivo;

import com.tallerwebi.presentacion.DatosRutina;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ServicioRutinaImpl implements ServicioRutina {

    private RepositorioRutina repositorioRutina;

    public ServicioRutinaImpl(RepositorioRutina repositorioRutina){
        this.repositorioRutina = repositorioRutina;
    }

    public ServicioRutinaImpl(){}


    @Override
    public List<DatosRutina> getRutinas() {
        return this.convertToDatosRutina(this.repositorioRutina.getRutinas());
    }

    @Override
    public boolean agregarRutina(Rutina rutina) throws RutinaYaExisteException{

        if (rutina == null || rutina.getNombre() == null || rutina.getNombre().isEmpty() || rutina.getObjetivo() == null ) {
           return false;
        }

        if (this.repositorioRutina.buscarRutinaPorId(rutina.getIdRutina()) != null) {
            throw new RutinaYaExisteException();
        }

        repositorioRutina.guardarRutina(rutina);
        return true;
    }

    @Override
    public boolean agregarEjercicio(Ejercicio ejercicio) throws EjercicioYaExisteException {
        if (ejercicio == null || ejercicio.getNombre() == null || ejercicio.getNombre().isEmpty() || ejercicio.getObjetivo() == null
            || ejercicio.getGrupoMuscularObjetivo() == null || ejercicio.getSeries() <= 0 || ejercicio.getRepeticiones() <= 0) {
            return false;
        }

        if (this.repositorioRutina.buscarEjercicioPorId(ejercicio.getIdEjercicio()) != null) {
            throw new EjercicioYaExisteException();
        }

        repositorioRutina.guardarEjercicio(ejercicio);
        return true;
    }


    @Override//falta testear
    public boolean eliminarRutina(Rutina rutina) throws RutinaNoEncontradaException {
        Long idRutina = rutina.getIdRutina();
        Rutina rutinaBuscada = repositorioRutina.buscarRutinaPorId(idRutina);

        if (rutinaBuscada == null) {
            throw new RutinaNoEncontradaException();
        }

        repositorioRutina.eliminarRutina(rutinaBuscada);
        return true;

    }

    @Override//falta testear
    public boolean eliminarEjercicio(Ejercicio ejercicio) throws EjercicioNoEncontradoException {
        Long idEjercicio = ejercicio.getIdEjercicio();
        Ejercicio ejercicioBuscado = repositorioRutina.buscarEjercicioPorId(idEjercicio);

        if (ejercicioBuscado == null) {
            throw new EjercicioNoEncontradoException();
        }

        repositorioRutina.eliminarEjercicio(ejercicioBuscado);
        return true;
    }



    @Override
    public DatosRutina getRutinaByObjetivo(Objetivo objetivo) {
        return this.convertRutinaADatosRutina(this.repositorioRutina.getRutinaByObjetivo(objetivo));
    }

    @Override
    public DatosRutina getRutinaParaUsuario(Usuario usuario) {
        return this.convertRutinaADatosRutina(this.repositorioRutina.getRutinaParaUsuario(usuario));
    }

    @Override
    public List<DatosRutina> getRutinasDeUsuario(Usuario usuario) throws UsuarioSinRutinasException {

        List<DatosRutina> rutinas = this.convertToDatosRutina(this.repositorioRutina.getRutinasDeUsuario(usuario));

        if (!rutinas.isEmpty()){
            return rutinas;
        }else {
            throw new UsuarioSinRutinasException();

        }
    }

    @Override
    public boolean validarObjetivosDeUsuarioYRutina(Usuario usuario, Rutina rutina) throws UsuarioNoExisteException, RutinaNoEncontradaException, DiferenciaDeObjetivosExcepcion {

        if(usuario.getId() == null){
            throw new UsuarioNoExisteException();
        }
        if (rutina.getIdRutina() == null) {
            throw new RutinaNoEncontradaException();
        }
        if (usuario.getObjetivo() != rutina.getObjetivo()) {
            throw new DiferenciaDeObjetivosExcepcion();
        }
        return usuario.getObjetivo().equals(rutina.getObjetivo());
    }

    @Override
    public List<DatosRutina> getRutinasPorObjetivoDeUsuario(Usuario usuario) {
        List<Rutina> rutinas = this.repositorioRutina.getRutinasPorObjetivoDeUsuario(usuario);
        List<DatosRutina> datosRutinas = new ArrayList<>();

        for (Rutina rutina : rutinas){
            if(rutina.getObjetivo().equals(usuario.getObjetivo())){
                datosRutinas.add(new DatosRutina(rutina));
            }
        }

        return datosRutinas;

    }

    @Override
    public Usuario getUsuarioById(Long id) throws UsuarioNoExisteException {

        if (this.repositorioRutina.getUsuarioPorId(id) == null){
            throw new UsuarioNoExisteException();
        }

        return this.repositorioRutina.getUsuarioPorId(id);

    }

    @Override
    public Rutina getRutinaById(Long idRutina) throws RutinaNoEncontradaException {

        Rutina rutina = repositorioRutina.buscarRutinaPorId(idRutina);
        if (rutina == null) {
            throw new RutinaNoEncontradaException("La rutina con ID " + idRutina + " no existe.");
        }
        return rutina;
    }

    @Override
    public Ejercicio getEjercicioById(Long idEjercicio) throws EjercicioNoEncontradoException {
        Ejercicio ejercicio = repositorioRutina.buscarEjercicioPorId(idEjercicio);
        if (ejercicio == null) {
            throw new EjercicioNoEncontradoException("El ejercicio con ID " + idEjercicio + " no existe.");
        }
        return ejercicio;
    }

    @Override
    public boolean existeRutinaEnUsuario(Rutina rutina, Usuario usuario) {

        Rutina rutinaBuscada = repositorioRutina.buscarRutinaEnUsuario(rutina,usuario);

        return rutinaBuscada != null;
    }

    @Override
    public boolean existeEjercicioEnRutina(Ejercicio ejercicio, Rutina rutina) {
        Ejercicio ejercicioBuscado = repositorioRutina.buscarEjercicioEnRutina(ejercicio,rutina);

        return ejercicioBuscado != null;
    }


    private List<DatosRutina> convertToDatosRutina(List<Rutina> rutinas) {

        List<DatosRutina> datosRutinas = new ArrayList<>();

        for (Rutina rutina : rutinas){
            datosRutinas.add(new DatosRutina(rutina));
        }

        return datosRutinas;
    }

    private DatosRutina convertRutinaADatosRutina(Rutina rutina) {

        DatosRutina datosRutina = new DatosRutina(rutina);
        return datosRutina;
    }

}