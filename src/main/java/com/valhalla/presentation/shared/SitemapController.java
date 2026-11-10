package com.valhalla.presentation.shared;

import com.valhalla.config.BaseWebConfig.SiteOrigin;
import com.valhalla.domain.place.PlaceService;
import com.valhalla.domain.plan.PlanService;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * [PSHARE-007] GET /sitemap.xml: las URLs que los crawlers tienen que conocer, juntas en un
 * solo documento: la home, cada plan PUBLICO (/plans/{id}/public) y la ficha de cada lugar
 * (/places/{id}, ninguna es privada). Sin sesion: el permiso vive en SecurityConfig.
 *
 * <p>Los {@code <loc>} exigen URLs absolutas, asi que salen del bean siteOrigin (PUBLIC_BASE_URL
 * detras de un tunel, o el origen de la request). Ese bean es request-scoped sin proxy — el
 * record es final y no se puede proxear — por eso no se inyecta directo en este singleton: se
 * resuelve por request con ObjectProvider, que es el mismo camino por el que lo lee un template.
 */
@Controller
public class SitemapController {

  private static final String XML_HEADER =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n";
  private static final String XML_FOOTER = "</urlset>";

  private final PlanService planService;
  private final PlaceService placeService;
  private final ObjectProvider<SiteOrigin> siteOrigin;

  @Autowired
  public SitemapController(
    PlanService planService,
    PlaceService placeService,
    ObjectProvider<SiteOrigin> siteOrigin
  ) {
    this.planService = planService;
    this.placeService = placeService;
    this.siteOrigin = siteOrigin;
  }

  /** El documento completo: sin {@code lastmod} todavia, porque nada de esto tiene fecha propia. */
  @GetMapping("/sitemap.xml")
  @ResponseBody
  public ResponseEntity<String> siteMap() {
    SiteOrigin origin = this.siteOrigin.getObject();
    StringBuilder xml = new StringBuilder(XML_HEADER);
    for (String path : publicPaths()) {
      xml.append("  <url><loc>").append(origin.absolute(path)).append("</loc></url>\n");
    }
    xml.append(XML_FOOTER);
    return ResponseEntity.ok().contentType(MediaType.APPLICATION_XML).body(xml.toString());
  }

  /** La home, los planes publicos y todas las fichas de lugares. */
  private List<String> publicPaths() {
    List<String> paths = new ArrayList<>();
    paths.add("/");
    planService.getPublicPlans().forEach(plan -> paths.add("/plans/" + plan.getId() + "/public"));
    placeService.getAllPlaces().forEach(place -> paths.add("/places/" + place.getId()));
    return paths;
  }
}
