package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.ReseniaInvalida;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service("servicioResenia")
@Transactional
public class ServicioReseniaImpl implements ServicioResenia {
    private RepositorioResenia repositorioResenia;

    @Autowired
    public ServicioReseniaImpl(RepositorioResenia repositorioResenia) {this.repositorioResenia = repositorioResenia;}

    public ServicioReseniaImpl() {}

    public List<Resenia> consultarReseniasDeHamburguesa(Long hamburguesaId) {
        return repositorioResenia.buscarPorHamburguesa(hamburguesaId);}


    public void crearResenia(Resenia resenia) {
        repositorioResenia.guardar(resenia);}

    public boolean esReseniaValida(Resenia resenia) throws ReseniaInvalida {
        short calificacion = resenia.getCalificacion();
        if(calificacion>5 || calificacion<1){
            return false;}
        return true;
    }
}
