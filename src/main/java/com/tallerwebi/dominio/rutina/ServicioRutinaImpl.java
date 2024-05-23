package com.tallerwebi.dominio.rutina;

import com.tallerwebi.dominio.Usuario;
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
    public DatosRutina getRutinaByObjetivo(Objetivo objetivo) {
        return this.convertRutinaADatosRutina(this.repositorioRutina.getRutinaByObjetivo(objetivo));
    }

    @Override
    public DatosRutina getRutinaParaUsuario(Usuario usuario) {
        return this.convertRutinaADatosRutina(this.repositorioRutina.getRutinaParaUsuario(usuario));
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