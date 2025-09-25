package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.Resenia;
import com.tallerwebi.dominio.ServicioResenia;
import org.junit.jupiter.api.BeforeEach;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.mockito.Mockito.mock;

public class ControladorReseniaTest {
    private ControladorResenia controladorResenia;
    private Resenia reseniaMock;
    private DatosResenia datosReseniaMock;
    private HttpServletRequest requestMock;
    private HttpSession sessionMock;
    private ServicioResenia servicioReseniaMock;

    @BeforeEach
    public void init(){
        reseniaMock = new Resenia(new Long(1), (short)1,"Malísima");
        datosReseniaMock = new DatosResenia(reseniaMock.getCalificacion(), reseniaMock.getHamburguesaId(), reseniaMock.getComentario());
        requestMock = mock(HttpServletRequest.class);
        sessionMock = mock(HttpSession.class);
        servicioReseniaMock =  mock(ServicioResenia.class);
        controladorResenia = new ControladorResenia(servicioReseniaMock);
    }
}
