package com.tallerwebi.dominio.Pedidos;

import com.tallerwebi.dominio.Hijos.Hijo;
import com.tallerwebi.dominio.Usuario.Usuario;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.*;

@Entity
public class Pedido {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column
  @Enumerated(EnumType.STRING) // guarda "PAGO_PENDIENTE" en la BD, no un número
  private EstadoPedido estado;

  @ManyToOne
  @JoinColumn(name = "hijo_id")
  private Hijo hijo;

  @ManyToOne
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @OneToMany(
    mappedBy = "pedido",
    cascade = CascadeType.ALL,
    fetch = FetchType.EAGER,
    orphanRemoval = true
  )
  private List<ItemPedido> items;

  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Date fecha;

  @Column(name = "fecha_retiro", nullable = false)
  private LocalDate fechaRetiro;

  @Column(nullable = false)
  private Double subtotal;

  public Pedido() {
    this.items = new ArrayList<>();
    this.fecha = new Date();
    this.estado = EstadoPedido.EN_CARRITO; // ← siempre nace así
  }

  public void agregarItem(ItemPedido item) {
    this.items.add(item);
  }

  public Double calcularSubtotal() {
    this.subtotal = items.stream().mapToDouble(ItemPedido::getSubtotal).sum();
    return this.subtotal;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Hijo getHijo() {
    return hijo;
  }

  public void setHijo(Hijo hijo) {
    this.hijo = hijo;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public List<ItemPedido> getItems() {
    return items;
  }

  public void setItems(List<ItemPedido> items) {
    this.items = items;
  }

  public Date getFecha() {
    return fecha;
  }

  public void setFecha(Date fecha) {
    this.fecha = fecha;
  }

  public LocalDate getFechaRetiro() {
    return fechaRetiro;
  }

  public void setFechaRetiro(LocalDate fechaRetiro) {
    this.fechaRetiro = fechaRetiro;
  }

  public Double getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(Double subtotal) {
    this.subtotal = subtotal;
  }

  public EstadoPedido getEstado() {
    return estado;
  }

  public void setEstado(EstadoPedido estado) {
    this.estado = estado;
  }
}
