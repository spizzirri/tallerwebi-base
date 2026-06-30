package com.tallerwebi.dominio.AliasDeRetiro;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional
public class ServicioPalabrasAliasImpl implements ServicioPalabrasAlias {

  private List<String> colores;
  private List<String> animales;
  private List<String> objetos;

  private static final String URL_ALIAS =
    "https://raw.githubusercontent.com/valentin-liz/kionet-alias/main/alias.json";

  @PostConstruct
  public void cargarPalabras() {
    try {
      RestTemplate restTemplate = new RestTemplate();

      String json = restTemplate.getForObject(URL_ALIAS, String.class);

      if (json == null) {
        throw new IllegalStateException("No se pudieron cargar las palabras.");
      }

      ObjectMapper mapper = new ObjectMapper();

      AliasDTO aliasDTO = mapper.readValue(json, AliasDTO.class);

      this.colores = aliasDTO.getColores();
      this.animales = aliasDTO.getAnimales();
      this.objetos = aliasDTO.getObjetos();
    } catch (Exception e) {
      throw new IllegalStateException(
        "No fue posible obtener las palabras desde el servidor externo.",
        e
      );
    }
  }

  @Override
  public List<String> obtenerColores() {
    return colores;
  }

  @Override
  public List<String> obtenerAnimales() {
    return animales;
  }

  @Override
  public List<String> obtenerObjetos() {
    return objetos;
  }
}
