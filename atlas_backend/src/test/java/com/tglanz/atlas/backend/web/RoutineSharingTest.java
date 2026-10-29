package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import com.tglanz.atlas.backend.routine.application.port.in.RoutineUseCase;
import com.tglanz.atlas.backend.routine.domain.Routine;
import com.tglanz.atlas.backend.routine.domain.RoutineContent;
import com.tglanz.atlas.backend.routine.domain.Section;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "atlas.site-url=https://atlas.example")
@AutoConfigureMockMvc
class RoutineSharingTest {
  @Autowired MockMvc mvc;
  @Autowired RoutineUseCase routines;
  @Autowired MovementQueryUseCase movements;

  private Routine create(String owner) {
    var choices = movements.names();
    return routines.create(
        owner,
        new RoutineContent(
            "Shared practice",
            "Owner-written notes",
            List.of(
                new Section(
                    "Prepare",
                    "Start slowly",
                    List.of(
                        choices.get(0).key().value(),
                        choices.get(1).key().value(),
                        choices.get(0).key().value())))));
  }

  private String owner() {
    return "share-owner-" + UUID.randomUUID();
  }

  private String publicPath(UUID token) {
    return "/routines/shared/" + token;
  }

  @Test
  void routinesStayPrivateUnlessTheOwnerCreatesAPublicLink() throws Exception {
    String owner = owner();
    Routine routine = create(owner);
    assertThat(routine.shareToken()).isNull();
    mvc.perform(get("/routines/{id}", routine.id())).andExpect(status().is3xxRedirection());
    mvc.perform(get(publicPath(routine.id()))).andExpect(status().isNotFound());
    mvc.perform(get(publicPath(UUID.randomUUID()))).andExpect(status().isNotFound());
    mvc.perform(get("/routines/shared/invalid-link")).andExpect(status().isNotFound());
  }

  @Test
  void activeLinkIsAnonymousReadOnlyAndPreservesTheMovementOrder() throws Exception {
    String owner = owner();
    Routine routine = create(owner);
    mvc.perform(post("/routines/{id}/share", routine.id()).with(user(owner)).with(csrf()))
        .andExpect(status().is3xxRedirection());
    UUID token = routines.get(owner, routine.id()).shareToken();
    assertThat(token).isNotNull().isNotEqualTo(routine.id());
    var result =
        mvc.perform(get(publicPath(token)))
            .andExpect(status().isOk())
            .andExpect(header().string("Cache-Control", "private, no-store"))
            .andExpect(header().string("Referrer-Policy", "no-referrer"))
            .andReturn();
    String html = result.getResponse().getContentAsString();
    assertThat(html)
        .contains(
            "Shared practice",
            "Owner-written notes",
            "Start slowly",
            "noindex,nofollow",
            "Shared routine")
        .doesNotContain(
            owner,
            routine.id().toString(),
            "Edit routine",
            "Create public link",
            "Disable public link",
            "More actions");
    var view = (RoutinePages.RoutineView) result.getModelAndView().getModel().get("routine");
    assertThat(view.sections().getFirst().movements())
        .extracting(m -> m.key().value())
        .containsExactlyElementsOf(routine.content().sections().getFirst().movements());
    mvc.perform(head(publicPath(token))).andExpect(status().isOk());
    String otherUserHtml =
        mvc.perform(get(publicPath(token)).with(user("other-viewer")))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(otherUserHtml)
        .doesNotContain("Edit routine", "Create public link", "Disable public link");
  }

  @Test
  void sharingIsIdempotentAndUsesTheConfiguredPublicOrigin() throws Exception {
    String owner = owner();
    Routine routine = create(owner);
    UUID first = routines.enableSharing(owner, routine.id());
    assertThat(routines.enableSharing(owner, routine.id())).isEqualTo(first);
    String html =
        mvc.perform(
                get("/routines/{id}", routine.id())
                    .param("sharing", "true")
                    .with(user(owner))
                    .header("Host", "attacker.example"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(html)
        .contains("https://atlas.example" + publicPath(first), "Disable public link")
        .doesNotContain("attacker.example");
  }

  @Test
  void onlyTheOwnerCanChangeSharingAndPostsRequireCsrf() throws Exception {
    String owner = owner();
    Routine routine = create(owner);
    mvc.perform(post("/routines/{id}/share", routine.id()).with(user(owner)))
        .andExpect(status().isForbidden());
    mvc.perform(post("/routines/{id}/share", routine.id()).with(user("other-owner")).with(csrf()))
        .andExpect(status().isNotFound());
    assertThat(routines.get(owner, routine.id()).shareToken()).isNull();
    UUID token = routines.enableSharing(owner, routine.id());
    mvc.perform(post("/routines/{id}/unshare", routine.id()).with(user("other-owner")).with(csrf()))
        .andExpect(status().isNotFound());
    mvc.perform(
            post("/routines/{id}/edit", routine.id())
                .with(user("other-owner"))
                .with(csrf())
                .param("name", "Stolen"))
        .andExpect(status().isNotFound());
    assertThat(routines.get(owner, routine.id()).shareToken()).isEqualTo(token);
    mvc.perform(get("/api/routines/{id}", routine.id())).andExpect(status().isUnauthorized());
  }

  @Test
  void editsAreLiveButRevokedLinksStayRevokedAfterAnotherContentSave() throws Exception {
    String owner = owner();
    Routine routine = create(owner);
    UUID token = routines.enableSharing(owner, routine.id());
    var edited =
        new RoutineContent("Updated shared name", "New notes", routine.content().sections());
    routines.update(owner, routine.id(), edited);
    assertThat(routines.get(owner, routine.id()).shareToken()).isEqualTo(token);
    String html =
        mvc.perform(get(publicPath(token)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(html).contains("Updated shared name", "New notes");
    mvc.perform(post("/routines/{id}/unshare", routine.id()).with(user(owner)).with(csrf()))
        .andExpect(status().is3xxRedirection());
    routines.update(owner, routine.id(), routine.content());
    assertThat(routines.get(owner, routine.id()).shareToken()).isNull();
    mvc.perform(get(publicPath(token))).andExpect(status().isNotFound());
    UUID replacement = routines.enableSharing(owner, routine.id());
    assertThat(replacement).isNotEqualTo(token);
    mvc.perform(get(publicPath(token))).andExpect(status().isNotFound());
    mvc.perform(get(publicPath(replacement))).andExpect(status().isOk());
  }

  @Test
  void deletingTheRoutineInvalidatesItsLinkAndLinksStayOutOfTheSitemap() throws Exception {
    String owner = owner();
    Routine routine = create(owner);
    UUID token = routines.enableSharing(owner, routine.id());
    String sitemap =
        mvc.perform(get("/sitemap.xml"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(sitemap).doesNotContain(token.toString(), "/routines");
    routines.delete(owner, routine.id());
    mvc.perform(get(publicPath(token))).andExpect(status().isNotFound());
  }

  @Test
  void anEmptySharedRoutineDoesNotOfferPrivateEditingActions() throws Exception {
    String owner = owner();
    Routine routine =
        routines.create(owner, new RoutineContent("Empty shared practice", "", List.of()));
    UUID token = routines.enableSharing(owner, routine.id());
    String html =
        mvc.perform(get(publicPath(token)))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(html)
        .contains("No movements yet")
        .doesNotContain("Add sections and movements", routine.id().toString());
  }
}
