package com.valhalla.infrastructure.user;

import com.valhalla.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaUserRepository extends JpaRepository<User, UUID> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);

  /**
   * [USEARCH] LIKE case-insensitive y sin acentos sobre nombre y apellido, con el usuario de la
   * sesion excluido. {@code translate} baja los acentos a ASCII (HSQLDB y PostgreSQL lo soportan)
   * y {@code lower} iguala mayusculas. El {@code escape '!'} hace que {@code %} y {@code _} se
   * busquen como texto: una consulta de solo comodines no puede listar a todos.
   */
  @Query(
    """
    select u from User u
    where u.id <> :excludeUserId
      and (
        lower(function('translate', coalesce(u.firstName, ''),
          'áàäâãéèêëíìîïóòôöõúùûüñçÁÀÄÂÃÉÈÊËÍÌÎÏÓÒÔÕÚÙÛÜÑÇ',
          'aaaaaeeeeiiiiooooouuuuncAAAAAEEEEIIIIOOOOOUUUUNC'))
          like lower(concat('%', :query, '%')) escape '!'
        or lower(function('translate', coalesce(u.lastName, ''),
          'áàäâãéèêëíìîïóòôöõúùûüñçÁÀÄÂÃÉÈÊËÍÌÎÏÓÒÔÕÚÙÛÜÑÇ',
          'aaaaaeeeeiiiiooooouuuuncAAAAAEEEEIIIIOOOOOUUUUNC'))
          like lower(concat('%', :query, '%')) escape '!'
      )
    order by lower(u.firstName), lower(u.lastName), u.id
    """
  )
  List<User> searchByName(
    @Param("query") String normalizedQuery,
    @Param("excludeUserId") UUID excludeUserId
  );
}
