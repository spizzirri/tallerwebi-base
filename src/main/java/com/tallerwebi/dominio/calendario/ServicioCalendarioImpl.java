package com.tallerwebi.dominio.calendario;
import com.tallerwebi.dominio.excepcion.ItemRendimientoDuplicadoException;
import com.tallerwebi.dominio.excepcion.UsuarioYaCargoSuRendimientoDelDiaException;
import com.tallerwebi.dominio.rutina.Rendimiento;
import com.tallerwebi.dominio.usuario.Usuario;
import com.tallerwebi.presentacion.DatosItemRendimiento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
@Service
@Transactional
public class ServicioCalendarioImpl implements ServicioCalendario {


    private final RepositorioCalendario repositorioCalendario;

    @Autowired
    public ServicioCalendarioImpl(RepositorioCalendario repositorioCalendario){
        this.repositorioCalendario = repositorioCalendario;
    }

    @Override
    public List<DatosItemRendimiento> obtenerItemsRendimiento() {
        List<ItemRendimiento> items = this.repositorioCalendario.obtenerItemsRendimiento();
        if (items == null || items.isEmpty()) {
            return Collections.emptyList(); // Retorna una lista vacía en lugar de null
        }
        return this.convertirADatosItemRendimiento(items);
    }

    private List<DatosItemRendimiento> convertirADatosItemRendimiento(List<ItemRendimiento> listaItemRendimiento) {
    List<DatosItemRendimiento> datosItemRendimiento = new ArrayList<>();
        for (ItemRendimiento itemRendimiento: listaItemRendimiento) {
            datosItemRendimiento.add(new DatosItemRendimiento(itemRendimiento));
        }
        return datosItemRendimiento;
    }

    @Override
    public void guardarItemRendimiento(ItemRendimiento itemRendimiento) {
        if (!repositorioCalendario.existeItemRendimientoPorFecha(itemRendimiento.getFecha())) {
            itemRendimiento.setFecha(LocalDate.now());
            itemRendimiento.setDiaNombre();
            repositorioCalendario.guardar(itemRendimiento);
        } else {
            throw new ItemRendimientoDuplicadoException("No se puede guardar tu rendimiento más de una vez el mismo día.");
        }
    }

    @Override
    public DatosItemRendimiento obtenerItemMasSeleccionado() {
        ItemRendimiento itemRendimiento = repositorioCalendario.obtenerItemMasSeleccionado();
        return itemRendimiento != null ? new DatosItemRendimiento(itemRendimiento) : null;
    }

    @Override
    public void guardarItemRendimientoEnUsuario(ItemRendimiento itemRendimiento, Usuario usuario) throws UsuarioYaCargoSuRendimientoDelDiaException {
        if(this.validarSiElUsuarioPuedeCargarRutinaHoy(usuario)){
            this.repositorioCalendario.guardarRendimientoEnUsuario(itemRendimiento,usuario);
        }else {
            throw new UsuarioYaCargoSuRendimientoDelDiaException();
        }
    }

    @Override
    public List<DatosItemRendimiento> getItemsRendimientoDeUsuario(Usuario usuario) {
        return convertirADatosItemRendimiento(this.repositorioCalendario.getItemsRendimientoDeUsuario(usuario));
    }

    @Override
    public ItemRendimiento convertirRendimientoAItemRendimiento(Rendimiento rendimiento) {
        switch (rendimiento) {
            case ALTO:
                return new ItemRendimiento(TipoRendimiento.ALTO);
            case MEDIO:
                return new ItemRendimiento(TipoRendimiento.NORMAL);
            case BAJO:
                return new ItemRendimiento(TipoRendimiento.BAJO);
            default:
                return new ItemRendimiento(TipoRendimiento.DESCANSO);
        }
    }

    @Override
    public boolean validarSiElUsuarioPuedeCargarRutinaHoy(Usuario usuario) {
        return repositorioCalendario.getItemRendimientoDeUsuarioHoyPorId(usuario.getId()) == null;
    }


}
