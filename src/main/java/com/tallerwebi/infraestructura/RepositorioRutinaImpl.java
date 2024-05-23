package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.Usuario;
import com.tallerwebi.dominio.objetivo.Objetivo;
import com.tallerwebi.dominio.rutina.Ejercicio;
import com.tallerwebi.dominio.rutina.RepositorioRutina;
import com.tallerwebi.dominio.rutina.Rutina;
import com.tallerwebi.presentacion.DatosRutina;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RepositorioRutinaImpl implements RepositorioRutina {
    private List<Rutina> rutinas;

    public RepositorioRutinaImpl(){
        this.rutinas = new ArrayList<>();
        rutinas.add(new Rutina("Rutina volumen 1",Objetivo.GANANCIA_MUSCULAR));
        rutinas.add(new Rutina("Rutina perdida de peso 1",Objetivo.PERDIDA_DE_PESO));
        rutinas.add(new Rutina("Rutina de definicion 1",Objetivo.DEFINICION));
    }
    @Override
    public List<Rutina> getRutinas() {
        return this.rutinas;
    }


    @Override
    public Rutina getRutinaByObjetivo(Objetivo objetivo) {
        Rutina rutinaObtenida = null;

        for(Rutina rutina : this.rutinas){
            if(rutina.getObjetivo().equals(objetivo)){
                rutinaObtenida = rutina;
            }
        }

        return rutinaObtenida;

    }

    @Override
    public Rutina getRutinaParaUsuario(Usuario usuario) {
        Rutina rutinaObtenida = null;

        for(Rutina rutina : this.rutinas){
            if(rutina.getObjetivo().equals(usuario.getObjetivo())){
                rutinaObtenida = rutina;
            }
        }

        return rutinaObtenida;
    }
}
