package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
import java.util.Set;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

@Controller
class DiscoveryPages {
  private final MovementQueryUseCase movements;
  private final PageMetadataFactory metadata;

  DiscoveryPages(MovementQueryUseCase movements, PageMetadataFactory metadata) {
    this.movements = movements;
    this.metadata = metadata;
  }

  @GetMapping(value = "/sitemap.xml", produces = "application/xml;charset=UTF-8")
  @ResponseBody
  String sitemap() {
    var xml =
        new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append(
                "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\" "
                    + "xmlns:image=\"http://www.google.com/schemas/sitemap-image/1.1\">");
    appendUrl(xml, "/", null);
    appendUrl(xml, "/movements", null);
    movements
        .search("", Set.of())
        .forEach(
            movement ->
                appendUrl(
                    xml,
                    "/movements/" + movement.key().value(),
                    movement.representativeIllustration()));
    return xml.append("</urlset>").toString();
  }

  @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
  @ResponseBody
  String robots() {
    return "User-agent: *\nUser-agent: OAI-SearchBot\nAllow: /routines/shared/\n"
        + "Disallow: /api/\nDisallow: /login\nDisallow: /register\n"
        + "Disallow: /settings/\nDisallow: /routines\nAllow: /\n"
        + "Sitemap: "
        + metadata.absolute("/sitemap.xml")
        + "\n";
  }

  private void appendUrl(StringBuilder xml, String path, String image) {
    xml.append("<url><loc>").append(HtmlUtils.htmlEscape(metadata.absolute(path))).append("</loc>");
    String picture = metadata.imageUrl(image);
    if (picture != null) {
      xml.append("<image:image><image:loc>")
          .append(HtmlUtils.htmlEscape(picture))
          .append("</image:loc></image:image>");
    }
    xml.append("</url>");
  }
}
