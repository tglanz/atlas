package com.tglanz.atlas.backend.web;

import com.tglanz.atlas.backend.movement.application.port.in.MovementQueryUseCase;
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
            .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">");
    appendUrl(xml, "/");
    appendUrl(xml, "/movements");
    movements.names().forEach(movement -> appendUrl(xml, "/movements/" + movement.key().value()));
    return xml.append("</urlset>").toString();
  }

  @GetMapping(value = "/robots.txt", produces = "text/plain;charset=UTF-8")
  @ResponseBody
  String robots() {
    return "User-agent: *\nAllow: /\nSitemap: " + metadata.absolute("/sitemap.xml") + "\n";
  }

  private void appendUrl(StringBuilder xml, String path) {
    xml.append("<url><loc>")
        .append(HtmlUtils.htmlEscape(metadata.absolute(path)))
        .append("</loc></url>");
  }
}
