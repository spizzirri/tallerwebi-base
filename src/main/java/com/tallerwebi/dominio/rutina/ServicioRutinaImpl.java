package com.tallerwebi.dominio.rutina;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.dominio.excepcion.*;
import com.tallerwebi.dominio.objetivo.Objetivo;

import com.tallerwebi.presentacion.DatosRutina;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional
public class ServicioRutinaImpl implements ServicioRutina {

    private final RepositorioRutina repositorioRutina;
    @Autowired
    public ServicioRutinaImpl(RepositorioRutina repositorioRutina){
        this.repositorioRutina = repositorioRutina;
    }

    @Override
    public List<DatosRutina> getRutinas() {
        return this.convertToDatosRutina(this.repositorioRutina.getRutinas());
    }

    @Override
    public boolean agregarRutina(Rutina rutina) throws RutinaYaExisteException, RutinaInvalidaException {

        if (rutina == null || rutina.getNombre() == null || rutina.getNombre().isEmpty() || rutina.getObjetivo() == null ) {
            throw new RutinaInvalidaException();
        }

        if (this.repositorioRutina.buscarRutinaPorId(rutina.getIdRutina()) != null) {
            throw new RutinaYaExisteException();
        }

        repositorioRutina.guardarRutina(rutina);
        return true;
    }

    @Override
    public boolean agregarEjercicio(Ejercicio ejercicio) throws EjercicioYaExisteException, EjercicioInvalidoException {
        if (ejercicio == null || ejercicio.getNombre() == null || ejercicio.getNombre().isEmpty() || ejercicio.getObjetivo() == null
            || ejercicio.getGrupoMuscularObjetivo() == null || ejercicio.getSeries() <= 0 || ejercicio.getRepeticiones() <= 0) {
            throw new EjercicioInvalidoException();
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
    public DatosRutina getDatosRutinaById(Long idRutina) throws RutinaNoEncontradaException {

        Rutina rutina = repositorioRutina.buscarRutinaPorId(idRutina);
        if (rutina == null) {
            throw new RutinaNoEncontradaException("La rutina con ID " + idRutina + " no existe.");
        }
        return convertRutinaADatosRutina(rutina);
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

    @Override
    public List<DatosRutina> getRutinasPorObjetivo(Objetivo objetivo) throws ListaDeRutinasVaciaException {

        if (objetivo == null) {
            throw new IllegalArgumentException("El objetivo no puede ser nulo");
        }

        List<Rutina> rutinas = repositorioRutina.getRutinasByObjetivo(objetivo);
        if (rutinas == null || rutinas.isEmpty()) {
            throw new ListaDeRutinasVaciaException();
        }

        return convertToDatosRutina(rutinas);
    }

    @Override
    public DatosRutina getRutinaActualDelUsuario(Usuario usuario) throws UsuarioNoExisteException, RutinaNoEncontradaException {
        Usuario usuarioBuscado = this.getUsuarioById(usuario.getId());
        if (usuarioBuscado == null) {
            throw new UsuarioNoExisteException("El usuario con ID " + usuario.getId() + " no existe.");
        }

        Rutina rutinaActiva = this.repositorioRutina.getRutinaActivaDelUsuario(usuarioBuscado);
        if (rutinaActiva == null) {
            throw new RutinaNoEncontradaException("No se encontró una rutina activa.");
        }

        return convertRutinaADatosRutina(rutinaActiva);
    }


    @Override
    public void asignarRutinaAUsuario(Rutina rutina, Usuario usuario) throws RutinaYaExisteException {
        Rutina rutinaActivaBuscada = this.repositorioRutina.getRutinaActivaDelUsuario(usuario);
        if(rutinaActivaBuscada == null){
            this.repositorioRutina.asignarRutinaAUsuario(rutina,usuario);
        }else{
            throw new RutinaYaExisteException("La rutina ya se encuentra activa y asignada al usuario");
        }
    }

    @Override
    public void liberarRutinaActivaDelUsuario(Usuario usuario) {
        this.repositorioRutina.liberarRutinaActivaDelUsuario(usuario);
    }

    @Override
    public void actualizarEstadoEjercicio(Usuario usuario, Long ejercicioId, EstadoEjercicio.Estado estado) {
        repositorioRutina.actualizarEstadoEjercicio(usuario, ejercicioId, estado);
    }


    @Override
    public List<EstadoEjercicio> getEstadosEjercicios(Usuario usuario, DatosRutina rutina) {
        return repositorioRutina.getEstadoEjercicioList(usuario, rutina);
    }

    @Override
    public Rendimiento calcularRendimiento(List<EstadoEjercicio> estadosEjercicios) {
        int completos = 0;
        int incompletos = 0;
        int noEmpezados = 0;

        for (EstadoEjercicio estado : estadosEjercicios) {
            switch (estado.getEstado()) {
                case COMPLETO:
                    completos++;
                    break;
                case INCOMPLETO:
                    incompletos++;
                    break;
                case NO_EMPEZADO:
                    noEmpezados++;
                    break;
            }
        }

        int totalEjercicios = completos + incompletos + noEmpezados;

        if (totalEjercicios == 0) {
            return Rendimiento.BAJO;
        }

        double porcentajeCompletos = (double) completos / totalEjercicios;
        double porcentajeIncompletos = (double) incompletos / totalEjercicios;
        double porcentajeNoEmpezados = (double) noEmpezados / totalEjercicios;

        if (porcentajeNoEmpezados >= 0.75) {
            return Rendimiento.BAJO;
        }else if (porcentajeCompletos >= 0.75) {
            return Rendimiento.ALTO;
        }else if (porcentajeCompletos >= 0.5 && porcentajeIncompletos >= 0.5) {
            return Rendimiento.MEDIO;
        }else if (porcentajeCompletos >= 0.33 && porcentajeIncompletos >= 0.33 && porcentajeNoEmpezados >= 0.33) {
            return Rendimiento.MEDIO;
        }else if (porcentajeCompletos >= 0.50 && porcentajeIncompletos >= 0.25 ) {
            return Rendimiento.MEDIO;
        }else if (porcentajeIncompletos >= 0.50 && porcentajeCompletos >= 0.25 ) {
            return Rendimiento.MEDIO;
        } else {
            return Rendimiento.BAJO;
        }
    }

    @Override
    public void guardarObjetivoEnUsuario(Objetivo objetivo ,Usuario usuario) throws UsuarioNoExisteException, ObjetivoNoExisteException {

        if(usuario == null){
            throw new UsuarioNoExisteException();
        }else if (objetivo == null){
            throw new ObjetivoNoExisteException();
        }else{
            this.repositorioRutina.setObjetivoUsuario(objetivo,usuario);
        }
    }

    private List<DatosRutina> convertToDatosRutina(List<Rutina> rutinas) {

        List<DatosRutina> datosRutinas = new ArrayList<>();

        for (Rutina rutina : rutinas){
            datosRutinas.add(new DatosRutina(rutina));
        }

        return datosRutinas;
    }

    private DatosRutina convertRutinaADatosRutina(Rutina rutina) {
        return new DatosRutina(rutina);
    }

}