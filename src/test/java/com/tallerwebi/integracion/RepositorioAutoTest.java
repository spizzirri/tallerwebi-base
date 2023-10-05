package com.tallerwebi.integracion;

import com.tallerwebi.config.HibernateTestConfig;
import com.tallerwebi.config.SpringWebTestConfig;
import com.tallerwebi.dominio.Auto;
import com.tallerwebi.dominio.Marca;
import com.tallerwebi.dominio.RepositorioAuto;
import com.tallerwebi.dominio.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class RepositorioAutoTest {

    @Autowired
    private RepositorioAuto repositorioAuto;

    @Test
    @Rollback
    @Transactional
    public void debeEncontrarTresAutosCuandoExisteUnUsuarioConTresAutos(){
        Auto[] autos = dadoQueExisteUnUsuarioAConTresAutos();
        dadoQueExisteUnUsuarioBConDosAutos();

        List<Auto> autosEncontrados = cuandoBuscoLosAutosDeUsuarioA(autos[0].getUsuario());
        entoncesEnCuentroTresAutos(autosEncontrados);
    }

    @Test
    @Rollback
    @Transactional
    public void debeEncontrarTresAutosCuandoExisteSoloTresAutosFORD(){
        dadoQueExisteUnUsuarioAConDosAutosFORD();
        dadoQueExisteUnUsuarioBConUnAutoFORD();

        List<Auto> autosEncontrados = cuandoBuscoLosAutosDeMarcaFORD(Marca.FORD);
        entoncesEnCuentroTresAutos(autosEncontrados);
    }

    public Auto[] dadoQueExisteUnUsuarioAConDosAutosFORD(){
        Usuario usuarioA = new Usuario("a@test.com", "123", "ADMIN", true);
        Auto auto1 = new Auto(Marca.FORD, "AA111BB", usuarioA);
        Auto auto2 = new Auto(Marca.FORD, "AA222BB", usuarioA);

        Auto[] autos = {auto1, auto2};
        this.repositorioAuto.guardarAutos(autos);

        return autos;
    }
    public Auto[] dadoQueExisteUnUsuarioBConUnAutoFORD(){
        Usuario usuarioB = new Usuario("b@test.com", "123", "ADMIN", true);
        Auto auto1 = new Auto(Marca.FORD, "AA333BB", usuarioB);

        Auto[] autos = {auto1};
        this.repositorioAuto.guardarAutos(autos);

        return autos;
    }

    private Auto[] dadoQueExisteUnUsuarioAConTresAutos() {
        Usuario usuarioA = new Usuario("a@test.com", "123", "ADMIN", true);
        Auto auto1 = new Auto(Marca.FIAT, "AA111BB", usuarioA);
        Auto auto2 = new Auto(Marca.PEUGEOT, "AA222BB", usuarioA);
        Auto auto3 = new Auto(Marca.FORD, "AA333BB", usuarioA);

        Auto[] autos = {auto1, auto2, auto3};
        this.repositorioAuto.guardarAutos(autos);

        return autos;
    }

    private Auto[] dadoQueExisteUnUsuarioBConDosAutos(){
        Usuario usuarioB = new Usuario("b@test.com", "456", "ADMIN", true);
        Auto auto1 = new Auto(Marca.FIAT, "AA444BB", usuarioB);
        Auto auto2 = new Auto(Marca.PEUGEOT, "AA555BB", usuarioB);

        Auto[] autos = {auto1, auto2};
        this.repositorioAuto.guardarAutos(autos);

        return autos;
    }

    private List<Auto> cuandoBuscoLosAutosDeMarcaFORD(Marca marca){
        List<Auto> autos = this.repositorioAuto.buscarAutosPorMarca(marca);
        return autos;
    }

    private List<Auto> cuandoBuscoLosAutosDeUsuarioA(Usuario usuario){
        List<Auto> autos = this.repositorioAuto.buscarAutosDeUnUsuario(usuario);
        return autos;
    }

    private void entoncesEnCuentroTresAutos(List<Auto> autosEncontrados){
        assertThat(autosEncontrados, hasSize(3));
    }
}
