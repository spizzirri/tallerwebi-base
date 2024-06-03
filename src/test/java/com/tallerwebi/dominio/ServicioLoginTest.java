package com.tallerwebi.dominio;

import com.tallerwebi.dominio.calendario.ServicioCalendario;
import com.tallerwebi.dominio.reto.ServicioReto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class ServicioLoginTest {

    private RepositorioUsuario repositorioUsuario;
    private ServicioCalendario servicioCalendario;
    private ServicioLogin servicioLogin;
    private ServicioReto servicioReto;

    @BeforeEach
    public void init() {
        this.repositorioUsuario = mock(RepositorioUsuario.class);
        this.servicioCalendario = mock(ServicioCalendario.class);
        this.servicioReto = mock(ServicioReto.class);
        this.servicioLogin = new ServicioLoginImpl(this.repositorioUsuario, this.servicioCalendario, this.servicioReto);
    }


}
