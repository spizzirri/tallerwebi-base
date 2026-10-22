package com.valhalla.presentation.bookmark;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import com.valhalla.domain.bookmark.BookmarkEntry;
import com.valhalla.domain.bookmark.BookmarkStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Unit tests of {@link BookmarkCards}: the Guardados card carries the badge and tooltip of the
 * state (AC-11/AC-12) and only an available plan is openable. The snapshot fields (name, date,
 * place count, author) come straight from the read model.
 */
public class BookmarkCardsTest {

  private static final UUID ADMIN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000005");
  private static final UUID BOOKMARK_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000007");
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-000000000010");

  @Test
  public void anAvailablePlanHasNoBadgeNorTooltipAndIsOpenable() {
    Map<String, Object> card = BookmarkCards.card(entry(BookmarkStatus.AVAILABLE, true));

    assertThat(card.get("openable"), is(true));
    assertThat(card.get("publicPlan"), is(true));
    assertThat(card.get("badge"), is(nullValue()));
    assertThat(card.get("unavailableTooltip"), is(nullValue()));
  }

  @Test
  public void aPrivatePlanIsMarkedPrivadoAndKeepsTheCloneHint() {
    Map<String, Object> card = BookmarkCards.card(entry(BookmarkStatus.PRIVATE, false));

    assertThat(card.get("openable"), is(false));
    assertThat(card.get("publicPlan"), is(false));
    assertThat(card.get("badge"), is(equalTo("Privado")));
    assertThat(card.get("unavailableTooltip"), is(BookmarkCards.UNAVAILABLE_TOOLTIP));
  }

  @Test
  public void aDeletedPlanIsMarkedEliminadoAndKeepsTheCloneHint() {
    Map<String, Object> card = BookmarkCards.card(entry(BookmarkStatus.DELETED, true));

    assertThat(card.get("openable"), is(false));
    assertThat(card.get("badge"), is(equalTo("Eliminado")));
    assertThat(card.get("unavailableTooltip"), is(BookmarkCards.UNAVAILABLE_TOOLTIP));
  }

  @Test
  public void theCardCarriesTheSnapshotFieldsAndAGenericCover() {
    Map<String, Object> card = BookmarkCards.card(entry(BookmarkStatus.AVAILABLE, true));

    assertThat(card.get("bookmarkId"), is(equalTo(BOOKMARK_ID)));
    assertThat(card.get("planId"), is(equalTo(PLAN_ID)));
    assertThat(card.get("name"), is(equalTo("Bariloche")));
    assertThat(card.get("description"), is(equalTo("Una escapada")));
    assertThat(card.get("eventDate"), is(equalTo("2026-12-31")));
    assertThat(card.get("placeCount"), is(equalTo(3)));
    assertThat(card.get("administratorId"), is(equalTo(ADMIN_ID)));
    assertThat(card.get("administratorName"), is(equalTo("Ada Lovelace")));
    assertThat(String.valueOf(card.get("cover")), startsWith("/images/plans/cover-"));
  }

  @Test
  public void anUndatedPlanLeavesTheDateEmpty() {
    Map<String, Object> card = BookmarkCards.card(
      new BookmarkEntry(
        BOOKMARK_ID,
        PLAN_ID,
        "Bariloche",
        "Una escapada",
        null,
        null,
        3,
        ADMIN_ID,
        "Ada Lovelace",
        BookmarkStatus.AVAILABLE,
        true
      )
    );

    assertThat(card.get("eventDate"), is(nullValue()));
  }

  private static BookmarkEntry entry(BookmarkStatus status, boolean publicPlan) {
    return new BookmarkEntry(
      BOOKMARK_ID,
      PLAN_ID,
      "Bariloche",
      "Una escapada",
      LocalDate.of(2026, 12, 31),
      LocalTime.of(9, 0),
      3,
      ADMIN_ID,
      "Ada Lovelace",
      status,
      publicPlan
    );
  }
}
