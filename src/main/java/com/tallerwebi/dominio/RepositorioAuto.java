package com.tallerwebi.dominio;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
public interface RepositorioAuto {
    public void guardarAutos(Auto[] autos);
    public Auto buscarAutoPorPatente(String patente);
    public List<Auto> buscarAutosPorMarca(Marca marca);
    public List<Auto> buscarAutosDeUnUsuario(Usuario usuario);
}
