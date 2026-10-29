package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "atlas.analytics.measurement-id=G-ATLASTEST1",
      "atlas.coffee-url=https://buymeacoffee.com/atlas-test"
    })
@AutoConfigureMockMvc
class SiteIntegrationsTest {
  @Autowired MockMvc mvc;

  @Test
  void configuredPagesIncludeOneGoogleTagAndTheCoffeeLink() throws Exception {
    for (String path : new String[] {"/", "/movements", "/login"}) {
      String html =
          mvc.perform(get(path))
              .andExpect(status().isOk())
              .andReturn()
              .getResponse()
              .getContentAsString();
      assertThat(html)
          .containsOnlyOnce("https://www.googletagmanager.com/gtag/js?id=G-ATLASTEST1")
          .containsOnlyOnce("gtag('config', \"G-ATLASTEST1\");")
          .contains(
              "href=\"https://buymeacoffee.com/atlas-test\"",
              "rel=\"noopener noreferrer\"",
              "Buy me a coffee");
    }
  }

  @Test
  void partialUpdatesDoNotReinitializeAnalytics() throws Exception {
    String html =
        mvc.perform(get("/movements").header("X-Atlas-Fragment", "movements"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertThat(html).doesNotContain("googletagmanager.com", "gtag(");
  }
}
