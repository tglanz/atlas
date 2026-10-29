package com.tglanz.atlas.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class PageMetadataFactoryTest {
  private final PageMetadataFactory metadata = new PageMetadataFactory("https://atlas.example/");

  @Test
  void generatesAbsoluteMetadataAndSafelyEmbeddedStructuredData() {
    var page =
        metadata.publicPage(
            "Plank </script>",
            "A body hold.\n Keep straight.",
            "/movements/plank",
            "/images/plank.png");
    assertThat(page.title()).isEqualTo("Plank </script> | Form Atlas");
    assertThat(page.description()).isEqualTo("A body hold. Keep straight.");
    assertThat(page.canonical()).isEqualTo("https://atlas.example/movements/plank");
    assertThat(page.image()).isEqualTo("https://atlas.example/images/plank.png");
    assertThat(page.imageAlt()).isEqualTo("Plank </script>");
    assertThat(page.withRobots("noindex,follow").imageAlt()).isEqualTo(page.imageAlt());
    assertThat(page.structuredData()).doesNotContain("</script>");
    var parsed = JsonMapper.builder().build().readTree(page.structuredData());
    assertThat(parsed.get("name").asString()).isEqualTo("Plank </script>");
    assertThat(parsed.get("url").asString()).isEqualTo(page.canonical());
  }

  @Test
  void privatePagesHaveNoCanonicalOrStructuredData() {
    var page = metadata.privatePage("My routine");
    assertThat(page.robots()).isEqualTo("noindex,nofollow");
    assertThat(page.canonical()).isNull();
    assertThat(page.structuredData()).isNull();
  }

  @Test
  void rejectsUntrustedOriginsAndUnsafeImageSchemes() {
    Stream.of(
            "//evil.example",
            "javascript:alert(1)",
            "https://atlas.example/path",
            "https://user:password@atlas.example",
            "https://atlas.example?query=true")
        .forEach(
            origin ->
                assertThatThrownBy(() -> new PageMetadataFactory(origin))
                    .isInstanceOf(IllegalArgumentException.class));
    assertThat(
            metadata
                .publicPage("Plank", "A hold", "/movements/plank", "javascript:alert(1)")
                .image())
        .isNull();
    assertThatThrownBy(() -> metadata.absolute("//evil.example"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
