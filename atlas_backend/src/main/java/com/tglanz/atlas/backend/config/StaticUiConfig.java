package com.tglanz.atlas.backend.config;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the built UI from the classpath. Paths without a file extension that match no file are
 * routes of the single-page app, so they get index.html. API paths are never answered this way.
 */
@Configuration
class StaticUiConfig implements WebMvcConfigurer {

  private static final String LOCATION = "classpath:/static/";

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Built files have content hashes in their names, so they can be cached for good.
    registry
        .addResourceHandler("/assets/**")
        .addResourceLocations(LOCATION + "assets/")
        .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic().immutable());

    registry
        .addResourceHandler("/**")
        .addResourceLocations(LOCATION)
        .setCacheControl(CacheControl.noCache())
        .resourceChain(true)
        .addResolver(new SpaResolver());
  }

  private static final class SpaResolver extends PathResourceResolver {
    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
      if (resourcePath.startsWith("api/")) {
        return null;
      }
      Resource file = location.createRelative(resourcePath);
      if (file.exists() && file.isReadable()) {
        return file;
      }
      boolean hasExtension =
          resourcePath.substring(resourcePath.lastIndexOf('/') + 1).contains(".");
      Resource index = new ClassPathResource("static/index.html");
      return hasExtension || !index.exists() ? null : index;
    }
  }
}
