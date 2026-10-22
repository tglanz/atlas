package com.tglanz.atlas.backend.generation.adapter.out.connector;

import com.tglanz.atlas.backend.generation.application.port.out.ImageConnector;
import com.tglanz.atlas.backend.generation.application.port.out.ImageGenerationException;
import com.tglanz.atlas.backend.generation.application.port.out.ImageRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** fal.ai through its queue: submit, poll the status URL, fetch the result URL. */
@Component
@ConditionalOnProperty(name = "atlas.generation.provider", havingValue = "fal")
class FalImageConnector implements ImageConnector {

  private static final Logger log = LoggerFactory.getLogger(FalImageConnector.class);

  private static final String QUEUE = "https://queue.fal.run/";
  private static final Duration POLL_INTERVAL = Duration.ofSeconds(2);
  private static final Duration HTTP_TIMEOUT = Duration.ofSeconds(30);

  private final HttpClient http = HttpClient.newHttpClient();
  private final JsonMapper json = JsonMapper.builder().build();
  private final String apiKey;
  private final String model;
  private final String imageSize;
  private final String quality;
  private final Duration timeout;

  FalImageConnector(
      @Value("${atlas.generation.fal.api-key:}") String apiKey,
      @Value("${atlas.generation.fal.model}") String model,
      @Value("${atlas.generation.fal.image-size}") String imageSize,
      @Value("${atlas.generation.fal.quality}") String quality,
      @Value("${atlas.generation.fal.timeout}") Duration timeout) {
    this.apiKey = apiKey;
    this.model = model;
    this.imageSize = imageSize;
    this.quality = quality;
    this.timeout = timeout;
  }

  @Override
  public Submission submit(ImageRequest request) {
    if (apiKey.isBlank()) {
      throw new ImageGenerationException("The fal.ai API key is not configured");
    }
    // This model has no system prompt field, so the system prompt leads the image prompt.
    Map<String, Object> body =
        Map.of(
            "prompt",
            request.systemPrompt() + "\n\n" + request.imagePrompt(),
            "num_images",
            1,
            "image_size",
            imageSize,
            "quality",
            quality,
            "output_format",
            "png");
    String payload = json.writeValueAsString(body);
    HttpRequest.Builder builder =
        base(QUEUE + model)
            .header("Content-Type", "application/json")
            // Fal does not start the request once we would have given up on it.
            .header("X-Fal-Request-Start-Timeout", String.valueOf(timeout.toSeconds()))
            // No expiry, so an approved image never disappears from under a Movement.
            .header("X-Fal-Object-Lifecycle-Preference", "{\"expiration_duration_seconds\":null}")
            .POST(HttpRequest.BodyPublishers.ofString(payload));
    JsonNode node = send(builder.build(), payload);
    return new Submission(
        "fal",
        model,
        text(node, "request_id"),
        text(node, "status_url"),
        text(node, "response_url"),
        text(node, "cancel_url"));
  }

  @Override
  public String awaitImageUrl(Submission submission, Consumer<String> onProgress) {
    long deadline = System.nanoTime() + timeout.toNanos();
    String reported = null;
    while (true) {
      JsonNode status = send(base(submission.statusUrl() + "?logs=1").GET().build(), null);
      String state = text(status, "status");
      if ("COMPLETED".equals(state)) {
        break;
      }
      String progress = describe(state, status);
      if (!progress.equals(reported)) {
        onProgress.accept(progress);
        reported = progress;
      }
      if (System.nanoTime() > deadline) {
        cancel(submission);
        throw new ImageGenerationException("The provider did not finish in time");
      }
      sleep();
    }
    JsonNode result = send(base(submission.resultUrl()).GET().build(), null);
    JsonNode url = result.path("images").path(0).path("url");
    if (!url.isString() || url.asString().isBlank()) {
      throw new ImageGenerationException("The provider returned no image");
    }
    return url.asString();
  }

  /** Best effort: stops fal from working on, and billing, a request nobody waits for. */
  private void cancel(Submission submission) {
    try {
      send(base(submission.cancelUrl()).PUT(HttpRequest.BodyPublishers.noBody()).build(), null);
    } catch (RuntimeException e) {
      log.warn("Could not cancel fal request {}: {}", submission.requestId(), e.getMessage());
    }
  }

  private static String describe(String state, JsonNode status) {
    if ("IN_QUEUE".equals(state)) {
      JsonNode position = status.path("queue_position");
      return position.isNumber() ? "In queue, position " + position.asInt() : "In queue";
    }
    JsonNode logs = status.path("logs");
    if (logs.isArray() && !logs.isEmpty()) {
      JsonNode message = logs.get(logs.size() - 1).path("message");
      if (message.isString() && !message.asString().isBlank()) {
        return message.asString();
      }
    }
    return "Generating";
  }

  private HttpRequest.Builder base(String url) {
    return HttpRequest.newBuilder(URI.create(url))
        .timeout(HTTP_TIMEOUT)
        .header("Authorization", "Key " + apiKey);
  }

  /** Sends the request; payload is only what the debug log shows of its body. */
  private JsonNode send(HttpRequest request, String payload) {
    try {
      // The Authorization header is never logged.
      log.debug(
          "fal request: {} {} {}", request.method(), request.uri(), payload == null ? "" : payload);
      HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
      log.debug("fal response: {} {}", response.statusCode(), abbreviate(response.body()));
      if (response.statusCode() / 100 != 2) {
        throw new ImageGenerationException(
            "The provider answered %d: %s"
                .formatted(response.statusCode(), abbreviate(response.body())));
      }
      return json.readTree(response.body());
    } catch (IOException e) {
      throw new ImageGenerationException("The provider could not be reached", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ImageGenerationException("Interrupted while waiting for the provider", e);
    }
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.path(field);
    if (!value.isString()) {
      throw new ImageGenerationException("The provider response lacks " + field);
    }
    return value.asString();
  }

  private static String abbreviate(String body) {
    return body.length() > 300 ? body.substring(0, 300) : body;
  }

  private static void sleep() {
    try {
      Thread.sleep(POLL_INTERVAL);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ImageGenerationException("Interrupted while waiting for the provider", e);
    }
  }
}
