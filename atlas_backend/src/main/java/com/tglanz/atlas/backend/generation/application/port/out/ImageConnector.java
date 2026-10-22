package com.tglanz.atlas.backend.generation.application.port.out;

import java.util.function.Consumer;

/** One image provider. Neither call retries; a failure throws ImageGenerationException. */
public interface ImageConnector {

  /** Identifies a provider request, so it can be followed up and traced. */
  record Submission(
      String provider,
      String model,
      String requestId,
      String statusUrl,
      String resultUrl,
      String cancelUrl) {}

  /** Submits one provider request for one image. */
  Submission submit(ImageRequest request);

  /**
   * Waits for the provider request to complete and returns the image URL of its image. Calls
   * onProgress with a short text whenever what the provider reports changes.
   */
  String awaitImageUrl(Submission submission, Consumer<String> onProgress);
}
