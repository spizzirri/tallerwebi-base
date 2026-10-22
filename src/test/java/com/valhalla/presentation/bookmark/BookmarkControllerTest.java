package com.valhalla.presentation.bookmark;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.valhalla.domain.bookmark.PlanBookmarkService;
import com.valhalla.domain.plan.Plan;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.ModelAndView;

/** [MARK] Unit tests of {@link BookmarkController}: session email and safe redirects. */
public class BookmarkControllerTest {

  private static final String EMAIL = "guardador@test.com";
  private static final UUID PLAN_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000a");
  private static final UUID COPY_ID = UUID.fromString("0192f0a1-0000-7000-8000-00000000000b");

  private BookmarkController controller;
  private PlanBookmarkService planBookmarkServiceMock;
  private Authentication authenticationMock;

  @BeforeEach
  public void init() {
    this.planBookmarkServiceMock = mock(PlanBookmarkService.class);
    this.authenticationMock = mock(Authentication.class);
    when(this.authenticationMock.getName()).thenReturn(EMAIL);
    this.controller = new BookmarkController(this.planBookmarkServiceMock);
  }

  @Test
  public void T_MARK_020_save_tomaElEmailDeLaSesionYVuelveAlReturnTo() {
    ModelAndView view = this.controller.save(PLAN_ID, "/for-you?q=cafe", this.authenticationMock);

    verify(this.planBookmarkServiceMock).save(PLAN_ID, EMAIL);
    assertThat(view.getViewName(), is("redirect:/for-you?q=cafe"));
  }

  @Test
  public void T_MARK_021_save_sinReturnToVuelveALaVistaPublica() {
    ModelAndView view = this.controller.save(PLAN_ID, null, this.authenticationMock);

    verify(this.planBookmarkServiceMock).save(PLAN_ID, EMAIL);
    assertThat(view.getViewName(), is("redirect:/plans/" + PLAN_ID + "/public"));
  }

  @Test
  public void T_MARK_022_save_conReturnToInseguroCaeAlDefault() {
    String[] unsafe = {
      null,
      "https://otro-sitio.com",
      "//otro-sitio.com",
      "/a\\b",
      "for-you",
      "",
      " ",
    };
    for (String returnTo : unsafe) {
      ModelAndView view = this.controller.save(PLAN_ID, returnTo, this.authenticationMock);
      assertThat(view.getViewName(), is("redirect:/plans/" + PLAN_ID + "/public"));
    }
  }

  @Test
  public void T_MARK_023_remove_redirigeAlReturnTo() {
    ModelAndView view =
      this.controller.remove(PLAN_ID, "/profile/7#bookmarks", this.authenticationMock);

    verify(this.planBookmarkServiceMock).remove(PLAN_ID, EMAIL);
    assertThat(view.getViewName(), is("redirect:/profile/7#bookmarks"));
  }

  @Test
  public void T_MARK_023b_remove_sinReturnToVuelveALaVistaPublica() {
    ModelAndView view = this.controller.remove(PLAN_ID, null, this.authenticationMock);

    assertThat(view.getViewName(), is("redirect:/plans/" + PLAN_ID + "/public"));
  }

  @Test
  public void T_MARK_023c_clone_redirigeAlDetalleDeLaCopia() {
    Plan copy = new Plan();
    copy.setId(COPY_ID);
    when(this.planBookmarkServiceMock.cloneBookmark(PLAN_ID, EMAIL)).thenReturn(copy);

    ModelAndView view = this.controller.clone(PLAN_ID, this.authenticationMock);

    verify(this.planBookmarkServiceMock).cloneBookmark(PLAN_ID, EMAIL);
    assertThat(view.getViewName(), is("redirect:/plans/" + COPY_ID));
  }
}
