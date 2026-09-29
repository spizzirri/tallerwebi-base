package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.FiltroCobertura;
import com.tallerwebi.dominio.Mascota;
import com.tallerwebi.dominio.ServicioCobertura;
import com.tallerwebi.dominio.ServicioSocio;
import com.tallerwebi.dominio.Socio;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ControladorServicio extends ControladorConSocio {

  private ServicioCobertura servicioCobertura;

  @Autowired
  public ControladorServicio(ServicioSocio servicioSocio, ServicioCobertura servicioCobertura) {
    super(servicioSocio);
    this.servicioCobertura = servicioCobertura;
  }

  @RequestMapping(path = "/servicios", method = RequestMethod.GET)
  public ModelAndView listarServicios(
    @RequestParam(name = "mascotaId", required = false) Long mascotaId,
    @RequestParam(name = "filtro", defaultValue = "TODOS") FiltroCobertura filtro,
    HttpSession sesion
  ) {
    Socio socio = socioLogueado(sesion);
    List<Mascota> mascotas = socio == null ? List.of() : socio.getMascotas();
    Mascota mascota = elegirMascota(socio, mascotas, mascotaId);

    Map<String, Object> modelo = new ModelMap();
    modelo.put("mascotas", mascotas);
    modelo.put("mascota", mascota);
    modelo.put("filtro", filtro);
    modelo.put("filtros", FiltroCobertura.values());
    modelo.put("items", servicioCobertura.coberturaDe(mascota, filtro));
    return new ModelAndView("servicios", modelo);
  }

  private Mascota elegirMascota(Socio socio, List<Mascota> mascotas, Long mascotaId) {
    Mascota elegida = socio == null || mascotaId == null ? null : socio.buscarMascota(mascotaId);
    if (elegida == null && !mascotas.isEmpty()) {
      return mascotas.get(0);
    }
    return elegida;
  }
}
