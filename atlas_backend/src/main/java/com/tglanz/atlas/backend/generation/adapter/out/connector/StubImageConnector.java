package com.tglanz.atlas.backend.generation.adapter.out.connector;

import com.tglanz.atlas.backend.generation.application.port.out.ImageConnector;
import com.tglanz.atlas.backend.generation.application.port.out.ImageGenerationException;
import com.tglanz.atlas.backend.generation.application.port.out.ImageRequest;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Emulates a provider: every image is the same placeholder, after a short random delay. */
@Component
@ConditionalOnProperty(name = "atlas.generation.provider", havingValue = "stub")
class StubImageConnector implements ImageConnector {

  private final String imageUrl;
  private final long minDelayMillis;
  private final long maxDelayMillis;

  StubImageConnector(
      @Value("${atlas.generation.stub.image-url}") String imageUrl,
      @Value("${atlas.generation.stub.min-delay}") Duration minDelay,
      @Value("${atlas.generation.stub.max-delay}") Duration maxDelay) {
    this.imageUrl = imageUrl;
    this.minDelayMillis = minDelay.toMillis();
    this.maxDelayMillis = Math.max(minDelayMillis, maxDelay.toMillis());
  }

  @Override
  public Submission submit(ImageRequest request) {
    return new Submission("stub", "stub", UUID.randomUUID().toString(), null, null, null);
  }

  @Override
  public String awaitImageUrl(Submission submission, Consumer<String> onProgress) {
    long total = ThreadLocalRandom.current().nextLong(minDelayMillis, maxDelayMillis + 1);
    onProgress.accept("In queue");
    sleep(total / 3);
    onProgress.accept("Generating");
    sleep(total - total / 3);
    return imageUrl;
  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ImageGenerationException("Interrupted", e);
    }
  }
}
