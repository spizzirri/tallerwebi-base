package com.tallerwebi.dominio;

import com.tallerwebi.dominio.entidades.Componente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service("servicioProductoEspecifico")
@Transactional
public class ServicioProductoEspecificoImpl implements ServicioProductoEspecifico {

    private RepositorioComponente repositorioComponente;

    @Autowired
    public ServicioProductoEspecificoImpl(RepositorioComponente repositorioComponente) {
        this.repositorioComponente = repositorioComponente;
    }

    @Override
    public Componente obtenerComponentePorId(Long idComponente) {
        return this.repositorioComponente.buscarComponenteConImagenesPorId(idComponente);
    }
}
