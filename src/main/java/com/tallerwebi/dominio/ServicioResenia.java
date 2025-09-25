package com.tallerwebi.dominio;

import com.tallerwebi.dominio.excepcion.ReseniaInvalida;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ServicioResenia {
    List<Resenia> consultarReseniasDeHamburguesa(Long hamburguesaId);
    void crearResenia(Resenia resenia);
    boolean esReseniaValida(Resenia resenia) throws ReseniaInvalida;
}
