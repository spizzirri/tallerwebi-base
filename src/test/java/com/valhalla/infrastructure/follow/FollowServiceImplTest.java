package com.valhalla.infrastructure.follow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.valhalla.domain.exception.UserNotFoundException;
import com.valhalla.domain.follow.Follow;
import com.valhalla.domain.follow.FollowRepository;
import com.valhalla.domain.user.User;
import com.valhalla.domain.user.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/** [FOLLOW] Unit tests of {@link FollowServiceImpl} against mocked ports (19-FOLLOW.md). */
public class FollowServiceImplTest {

  private static final UUID FOLLOWER_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000b");
  private static final UUID FOLLOWED_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000016");
  private static final UUID OTHER_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000021");
  private static final UUID MISSING_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000063");

  private FollowServiceImpl followService;
  private FollowRepository followRepositoryMock;
  private UserService userServiceMock;

  @BeforeEach
  public void init() {
    this.followRepositoryMock = mock(FollowRepository.class);
    this.userServiceMock = mock(UserService.class);
    this.followService = new FollowServiceImpl(this.followRepositoryMock, this.userServiceMock);
  }

  // --- follow: camino feliz ---

  @Test
  public void U01_follow_creaLaRelacionYDejaIsFollowingEnVerdadero() {
    // given
    when(this.userServiceMock.findById(FOLLOWER_ID)).thenReturn(user(FOLLOWER_ID, "Ana", "Perez"));
    when(this.userServiceMock.findById(FOLLOWED_ID)).thenReturn(user(FOLLOWED_ID, "Bruno", "Diaz"));
    // Primera consulta: no existia. Segunda: la que hace isFollowing despues del save.
    when(this.followRepositoryMock.existsByFollowerIdAndFollowedId(FOLLOWER_ID, FOLLOWED_ID))
      .thenReturn(false, true);

    // when
    this.followService.follow(FOLLOWER_ID, FOLLOWED_ID);

    // then
    ArgumentCaptor<Follow> captor = ArgumentCaptor.forClass(Follow.class);
    verify(this.followRepositoryMock, times(1)).save(captor.capture());
    Follow saved = captor.getValue();
    assertThat(saved.getFollower().getId(), is(FOLLOWER_ID));
    assertThat(saved.getFollowed().getId(), is(FOLLOWED_ID));
    // El id lo asigna la base: la arista que arma el servicio todavia no lo tiene.
    assertThat(saved.getId(), is(nullValue()));
    assertThat(this.followService.isFollowing(FOLLOWER_ID, FOLLOWED_ID), is(true));
    // El constructor vacio existe para JPA: por si solo no arma ninguna arista.
    assertThat(new Follow().getFollowed(), is(nullValue()));
  }

  @Test
  public void U02_follow_seguirDosVecesNoDuplicaLaRelacion() {
    // given: la relacion ya existe, el segundo intento no debe escribir
    when(this.userServiceMock.findById(FOLLOWER_ID)).thenReturn(user(FOLLOWER_ID, "Ana", "Perez"));
    when(this.userServiceMock.findById(FOLLOWED_ID)).thenReturn(user(FOLLOWED_ID, "Bruno", "Diaz"));
    when(this.followRepositoryMock.existsByFollowerIdAndFollowedId(FOLLOWER_ID, FOLLOWED_ID))
      .thenReturn(true);

    // when
    this.followService.follow(FOLLOWER_ID, FOLLOWED_ID);

    // then
    verify(this.followRepositoryMock, never()).save(any(Follow.class));
  }

  // --- unfollow ---

  @Test
  public void U03_unfollow_eliminaLaRelacionYEsIdempotenteCuandoNoExistia() {
    // given: la relacion existe
    when(this.followRepositoryMock.existsByFollowerIdAndFollowedId(FOLLOWER_ID, FOLLOWED_ID))
      .thenReturn(true);

    // when
    this.followService.unfollow(FOLLOWER_ID, FOLLOWED_ID);

    // then
    verify(this.followRepositoryMock, times(1))
      .deleteByFollowerIdAndFollowedId(FOLLOWER_ID, FOLLOWED_ID);

    // given: la segunda vuelta encuentra que ya no hay nada que borrar
    when(this.followRepositoryMock.existsByFollowerIdAndFollowedId(FOLLOWER_ID, FOLLOWED_ID))
      .thenReturn(false);

    // when
    this.followService.unfollow(FOLLOWER_ID, FOLLOWED_ID);

    // then: no se borra dos veces ni se rompe la vista (AC-02)
    verify(this.followRepositoryMock, times(1))
      .deleteByFollowerIdAndFollowedId(FOLLOWER_ID, FOLLOWED_ID);
  }

  // --- follow: self-follow ---

  @Test
  public void U04_follow_seguirseASiMismoLanzaYNoPersisteNada() {
    // when / then
    assertThrows(
      IllegalArgumentException.class,
      () -> this.followService.follow(FOLLOWER_ID, FOLLOWER_ID)
    );
    verify(this.followRepositoryMock, never()).save(any(Follow.class));
    verifyNoInteractions(this.userServiceMock);
  }

  // --- follow: usuario inexistente ---

  @Test
  public void U05_follow_seguirAUnUsuarioInexistenteLanzaUserNotFoundException() {
    // given: el seguidor existe, el seguido no
    when(this.userServiceMock.findById(FOLLOWER_ID)).thenReturn(user(FOLLOWER_ID, "Ana", "Perez"));
    when(this.userServiceMock.findById(MISSING_ID)).thenThrow(new UserNotFoundException());

    // when / then
    assertThrows(
      UserNotFoundException.class,
      () -> this.followService.follow(FOLLOWER_ID, MISSING_ID)
    );
    verify(this.followRepositoryMock, never()).save(any(Follow.class));
  }

  // --- isFollowing ---

  @Test
  public void U06_isFollowing_esFalsoSinRelacionYVerdaderoConRelacion() {
    // given: sin relacion
    when(this.followRepositoryMock.existsByFollowerIdAndFollowedId(FOLLOWER_ID, FOLLOWED_ID))
      .thenReturn(false);

    // then
    assertThat(this.followService.isFollowing(FOLLOWER_ID, FOLLOWED_ID), is(false));

    // given: con relacion
    when(this.followRepositoryMock.existsByFollowerIdAndFollowedId(FOLLOWER_ID, FOLLOWED_ID))
      .thenReturn(true);

    // then
    assertThat(this.followService.isFollowing(FOLLOWER_ID, FOLLOWED_ID), is(true));
  }

  // --- conteos [AC-04] ---

  @Test
  public void U07_counts_cuentanSoloLasRelacionesDelUsuario() {
    // given: los agregados los resuelve la base para el id pedido
    when(this.followRepositoryMock.countByFollowedId(FOLLOWED_ID)).thenReturn(3L);
    when(this.followRepositoryMock.countByFollowerId(FOLLOWER_ID)).thenReturn(2L);
    when(this.followRepositoryMock.countByFollowedId(OTHER_ID)).thenReturn(0L);

    // when / then
    assertThat(this.followService.countFollowers(FOLLOWED_ID), is(3L));
    assertThat(this.followService.countFollowing(FOLLOWER_ID), is(2L));
    assertThat(this.followService.countFollowers(OTHER_ID), is(0L));
    verify(this.followRepositoryMock, times(1)).countByFollowedId(FOLLOWED_ID);
    verify(this.followRepositoryMock, times(1)).countByFollowerId(FOLLOWER_ID);
  }

  // --- listas [AC-05] y autores del feed [AC-15] ---

  @Test
  public void U08_listas_lleganOrdenadasPorNombreYSinDuplicados() {
    // given: la consulta ya devuelve el orden por nombre
    List<User> followers = List.of(
      user(OTHER_ID, "Ana", "Perez"),
      user(FOLLOWED_ID, "Bruno", "Diaz")
    );
    List<User> following = List.of(user(FOLLOWER_ID, "Carla", "Gomez"));
    when(this.followRepositoryMock.findFollowers(FOLLOWED_ID)).thenReturn(followers);
    when(this.followRepositoryMock.findFollowing(FOLLOWER_ID)).thenReturn(following);
    when(this.followRepositoryMock.findFollowedIds(FOLLOWER_ID)).thenReturn(List.of(FOLLOWED_ID));

    // when
    List<User> seguidores = this.followService.followers(FOLLOWED_ID);
    List<User> seguidos = this.followService.following(FOLLOWER_ID);

    // then: mismo orden que la consulta, sin reordenar en memoria
    assertThat(seguidores, is(equalTo(followers)));
    assertThat(seguidos, is(equalTo(following)));
    assertThat(seguidores.stream().map(User::getFirstName).toList(), is(List.of("Ana", "Bruno")));
    // Sin duplicados: cada usuario aparece una sola vez en la lista
    assertThat(seguidores.stream().map(User::getId).distinct().count(), is(2L));
    assertThat(seguidos.stream().map(User::getId).distinct().count(), is(1L));
    // El scope Siguiendo resuelve los autores en una sola consulta (AC-15)
    assertThat(this.followService.followedIds(FOLLOWER_ID), is(equalTo(List.of(FOLLOWED_ID))));
    verify(this.followRepositoryMock, times(1)).findFollowedIds(FOLLOWER_ID);
  }

  private static User user(UUID id, String firstName, String lastName) {
    User user = new User();
    user.setId(id);
    user.setFirstName(firstName);
    user.setLastName(lastName);
    return user;
  }
}
