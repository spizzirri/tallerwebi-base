package com.tallerwebi.infraestructura;

import com.tallerwebi.dominio.EstadoTurno;
import com.tallerwebi.dominio.RepositorioTurno;
import com.tallerwebi.dominio.Turno;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository("repositorioTurno")
public class RepositorioTurnoImpl extends RepositorioBase<Turno> implements RepositorioTurno {

  @Autowired
  public RepositorioTurnoImpl(SessionFactory sessionFactory) {
    super(sessionFactory, Turno.class);
  }

  @Override
  public void guardar(Turno turno) {
    sesion().persist(turno);
  }

  @Override
  public List<Turno> listarPorSocio(Long socioId) {
    return sesion()
      .createQuery(
        "from Turno t where t.mascota.socio.id = :socioId order by t.fecha desc, t.hora desc",
        Turno.class
      )
      .setParameter("socioId", socioId)
      .getResultList();
  }

  @Override
  public List<LocalTime> horasOcupadas(Long veterinariaId, LocalDate fecha) {
    return sesion()
      .createQuery(
        "select t.hora from Turno t where t.veterinaria.id = :veterinariaId " +
        "and t.fecha = :fecha and t.estado = :estado",
        LocalTime.class
      )
      .setParameter("veterinariaId", veterinariaId)
      .setParameter("fecha", fecha)
      .setParameter("estado", EstadoTurno.CONFIRMADO)
      .getResultList();
  }
}
