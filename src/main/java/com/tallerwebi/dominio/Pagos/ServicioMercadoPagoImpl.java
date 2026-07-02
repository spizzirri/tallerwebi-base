package com.tallerwebi.dominio.Pagos;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.tallerwebi.dominio.Pedidos.Pedido;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServicioMercadoPagoImpl implements ServicioMercadoPago {

  private static final Logger logger = LoggerFactory.getLogger(ServicioMercadoPagoImpl.class);

  @Value("${mp.access.token}")
  private String accessToken;

  @Value("${app.base.url}")
  private String appBaseUrl;

  @Override
  public String crearPreferenciaDePago(List<Pedido> pedidos) {
    if (pedidos == null || pedidos.isEmpty()) {
      return null;
    }
    try {
      MercadoPagoConfig.setAccessToken(accessToken);
      List<PreferenceItemRequest> itemsMercadoPago = new ArrayList<>();
      for (Pedido pedido : pedidos) {
        if (pedido.getItems() == null) {
          continue;
        }
        pedido
          .getItems()
          .forEach(item ->
            itemsMercadoPago.add(
              PreferenceItemRequest
                .builder()
                .title(item.getProducto().getNombre())
                .quantity(item.getCantidad())
                .unitPrice(BigDecimal.valueOf(item.getPrecioUnitario()))
                .currencyId("ARS")
                .build()
            )
          );
      }
      if (itemsMercadoPago.isEmpty()) {
        return null;
      }

      // Armamos la referencia con los IDs de los pedidos que se están pagando en este intento
      String referenciaExterna = pedidos
        .stream()
        .map(p -> String.valueOf(p.getId()))
        .collect(java.util.stream.Collectors.joining(","));

      PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest
        .builder()
        .success(appBaseUrl + "/pago-exitoso")
        .failure(appBaseUrl + "/pago-fallido")
        .pending(appBaseUrl + "/pago-pendiente")
        .build();

      return new PreferenceClient()
        .create(
          PreferenceRequest
            .builder()
            .items(itemsMercadoPago)
            .backUrls(backUrls)
            .autoReturn("approved")
            .externalReference(referenciaExterna) // 👈 nuevo
            .build()
        )
        .getInitPoint();
    } catch (Exception e) {
      logger.error("Error API Mercado Pago", e);
      return null;
    }
  }
}
