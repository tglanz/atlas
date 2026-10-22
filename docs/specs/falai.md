Verified against official fal documentation on 22 October 2026.

The target is image generation with one shared instruction, several distinct prompts, and an optional reference image for each prompt. The examples use **Nano Banana 2**, because its API supports a native `system_prompt`; its edit endpoint accepts reference images.

**Recommended application design:** accept one batch request in your own backend, then submit one fal generation per item. If one generation HTTP request directly to fal is a requirement, use an inline fal Workflow with a separate model node for every item. Both approaches still incur the cost of each generated output. [Text to image API](https://fal.ai/models/fal-ai/nano-banana-2/api), [Edit API](https://fal.ai/models/fal-ai/nano-banana-2/edit/api), [Workflows](https://fal.ai/docs/documentation/model-apis/workflows)

# What one request can contain

| Need | Supported approach |
| - | - |
| One prompt, one image | `prompt` with `num_images: 1` |
| One prompt, several variations | One model request with `num_images` from 1 to 4 |
| Different prompts with a shared instruction | Separate model calls using the same `system_prompt` |
| Different reference image for each prompt | Separate edit calls, each with its own `image_urls` |
| One client HTTP request for all items | Your backend accepts a batch and coordinates the model calls |
| One generation HTTP request directly to fal | A Workflow with one model node per item, delivered as an SSE stream |

The checked image schemas declare a single `prompt` string, not a `prompts[]` array or a per-output prompt map. `num_images` creates multiple outputs from that request’s prompt; it does not assign a different prompt to each output. `system_prompt` applies within each call, so send it on every item. [Generation schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2), [Edit schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2/edit)

# Setup

Create a secret key at [Dashboard keys](https://fal.ai/dashboard/keys), using the **API** permission preset for ordinary generation and file operations. Set it on the server:

```sh
export FAL_KEY='your-secret-key'
npm install @fal-ai/client
# Or for Python:
python -m pip install fal-client
```

SDKs read `FAL_KEY` automatically. HTTP requests use `Authorization: Key <secret>`, including the `Key ` prefix. Keep the key on your backend; your application’s public batch route needs its own authentication and usage limits. [Authentication](https://fal.ai/docs/documentation/model-apis/authentication/key-based), [Client setup](https://fal.ai/docs/documentation/model-apis/inference/client-setup), [Proxy configuration](https://fal.ai/docs/documentation/model-apis/inference/proxy-setup)

# Generate one image

## Without a reference image

A direct HTTP call returns model JSON in the same connection:

```sh
curl --fail-with-body \
  'https://fal.run/fal-ai/nano-banana-2' \
  -H "Authorization: Key $FAL_KEY" \
  -H 'Content-Type: application/json' \
  -d '{
    "system_prompt":"Use watercolor, muted blues and a white background.",
    "prompt":"A lighthouse overlooking a rocky bay.",
    "num_images":1,
    "aspect_ratio":"1:1",
    "resolution":"1K",
    "output_format":"png",
    "limit_generations":true,
    "enable_web_search":false
  }'
```

`run` and direct HTTP bypass the queue. For more durable jobs, use `subscribe` or `queue.submit` as described below. An SDK call may involve submission, status and result HTTP exchanges even when it looks like one function call. [Synchronous inference](https://fal.ai/docs/documentation/model-apis/inference/synchronous), [Direct HTTP API](https://fal.ai/docs/inference-api/sync/run)

## With a reference image

Change the endpoint to `fal-ai/nano-banana-2/edit` and include:

```json
{
  "system_prompt": "Use watercolor, muted blues and a white background.",
  "prompt": "Use the reference lighthouse and place it beside a rocky bay.",
  "image_urls": ["https://your-storage.example/reference-lighthouse.png"],
  "num_images": 1,
  "aspect_ratio": "1:1",
  "resolution": "1K",
  "output_format": "png",
  "limit_generations": true,
  "enable_web_search": false
}
```

Replace the example URL with a real reachable image. The reference field is `image_urls`, an array even when there is only one image. An item can have several references; the model page advertises up to 14. Describe what to preserve or change in the item’s prompt. [Edit endpoint](https://fal.ai/models/fal-ai/nano-banana-2/edit/api), [Reference image capacity](https://fal.ai/models/fal-ai/nano-banana-2/edit)

# Multiple prompts through one application request

This is a **proposed contract for your backend**, not JSON accepted directly by fal’s image endpoint:

```json
{
  "instruction": "Use watercolor, muted blues and a white background.",
  "items": [
    {
      "id": "lighthouse",
      "prompt": "A lighthouse overlooking a rocky bay.",
      "reference_urls": ["https://your-storage.example/lighthouse.png"]
    },
    {
      "id": "boat",
      "prompt": "A fishing boat beside a wooden pier.",
      "reference_urls": ["https://your-storage.example/boat.png"]
    },
    {
      "id": "seagull",
      "prompt": "A seagull in flight."
    }
  ]
}
```

Map each item to one model call. Choose the edit endpoint when references exist; otherwise use text to image. Preserve the item ID, endpoint and fal request ID together. This keeps the prompt and its references isolated and lets you retry or recover individual items without repeating successful ones.

The following Node.js function can be called by your authenticated HTTP handler. It returns per-item outcomes, including partial success. The ten-item cap is an application choice, not a fal limit.

```javascript
import { fal } from "@fal-ai/client";

export async function generateImages({ instruction, items }) {
  if (typeof instruction !== "string" || instruction.length > 50000) {
    throw new Error("Invalid instruction");
  }
  if (!Array.isArray(items) || items.length < 1 || items.length > 10) {
    throw new Error("Provide 1 to 10 items");
  }
  const ids = new Set();
  for (const item of items) {
    if (!item || typeof item.id !== "string" ||
        !/^[A-Za-z][A-Za-z0-9_-]{0,63}$/.test(item.id) ||
        ids.has(item.id) || typeof item.prompt !== "string" ||
        item.prompt.length < 3 || item.prompt.length > 50000) {
      throw new Error("Each item needs a unique ID and a valid prompt");
    }
    ids.add(item.id);
    if (item.reference_urls !== undefined &&
        (!Array.isArray(item.reference_urls) ||
         item.reference_urls.length > 14 ||
         item.reference_urls.some(url =>
           typeof url !== "string" || !url.startsWith("https://")))) {
      throw new Error("References must be HTTPS image URLs");
    }
  }

  const results = await Promise.all(items.map(async item => {
    const references = item.reference_urls ?? [];
    const endpoint = references.length
      ? "fal-ai/nano-banana-2/edit"
      : "fal-ai/nano-banana-2";
    let requestId;

    try {
      const submitted = await fal.queue.submit(endpoint, {
        input: {
          system_prompt: instruction,
          prompt: item.prompt,
          ...(references.length ? { image_urls: references } : {}),
          num_images: 1,
          aspect_ratio: "1:1",
          resolution: "1K",
          output_format: "png",
          limit_generations: true,
          enable_web_search: false,
        },
      });
      requestId = submitted.request_id;
      // Persist { itemId: item.id, endpoint, requestId } in your job store.
      await fal.queue.subscribeToStatus(endpoint, { requestId });
      const { data } = await fal.queue.result(endpoint, { requestId });
      if (!data.images?.length) throw new Error("No image returned");
      return { id: item.id, endpoint, requestId,
               status: "ok", images: data.images };
    } catch (error) {
      return { id: item.id, endpoint, requestId, status: "error",
               error: error instanceof Error ? error.message : String(error) };
    }
  }));
  return { results };
}
```

This calls fal’s queue concurrently, but actual processing is limited by the account and endpoint. New accounts start with two simultaneous running requests; queued requests wait. If your HTTP platform cannot wait long enough, persist the batch and return an application job ID with HTTP 202, then expose a status endpoint. [JavaScript queue SDK](https://fal.ai/docs/api-reference/client-libraries/javascript/queue), [Queue behavior](https://fal.ai/docs/documentation/model-apis/inference/queue), [Concurrency](https://fal.ai/docs/documentation/model-apis/concurrency-limits)

**Cost:** Three items with one output each mean three generations, even though your client sent one HTTP request. The code uses normal account retention defaults; the retention section explains how to make them explicit.

# One generation HTTP request directly to fal

An inline Workflow can group several model calls behind one streaming request. Build one node per item and collect each node’s image URL. This does not require a saved workflow.

The function below constructs the graph in your application. It does not rely on an undocumented server-side loop or a model `prompts[]` field. Inputs must first pass the same validation as the application example.

```javascript
import { fal } from "@fal-ai/client";

export async function generateAsWorkflow(instruction, items) {
  const values = { instruction };
  const defaults = { instruction: "" };
  const workflow = {
    input: { id: "input", type: "input", depends: [], input: defaults },
  };
  const depends = [];
  const fields = {};

  items.forEach((item, index) => {
    const nodeId = "image_" + index;
    const promptKey = "prompt_" + index;
    const references = item.reference_urls ?? [];
    values[promptKey] = item.prompt;
    defaults[promptKey] = "";

    workflow[nodeId] = {
      id: nodeId,
      type: "run",
      depends: ["input"],
      app: references.length
        ? "fal-ai/nano-banana-2/edit"
        : "fal-ai/nano-banana-2",
      input: {
        system_prompt: "$input.instruction",
        prompt: "$input." + promptKey,
        ...(references.length ? { image_urls: references } : {}),
        num_images: 1,
        aspect_ratio: "1:1",
        resolution: "1K",
        output_format: "png",
        limit_generations: true,
        enable_web_search: false,
      },
    };
    depends.push(nodeId);
    fields[item.id] = "$" + nodeId + ".images.0.url";
  });

  workflow.output = {
    id: "output", type: "display", depends, fields,
  };

  const stream = await fal.stream("workflows/execute", {
    input: { input: values, workflow },
    timeout: 120000,
  });
  let output;
  let workflowError;
  stream.on("data", event => {
    if (event.type === "output") output = event.output;
    if (event.type === "error") {
      workflowError = new Error("Workflow failed: " + event.message);
    }
  });
  await stream.done();
  if (workflowError) throw workflowError;
  if (output === undefined) {
    throw new Error("Workflow ended without a final output");
  }
  return output;
}
```

This adapts the documented workflow graph and whole-field reference syntax. Its final output maps item IDs to image URLs. The workflow definition is sent inline; uploads of local references and downloads of generated images remain additional HTTP operations. Independent nodes do not imply guaranteed simultaneous scheduling. [Custom Workflow tutorial](https://fal.ai/docs/examples/integrations/custom-workflow-ui) The stream timeout is measured between chunks, with a 15-second SDK default; this example allows 120 seconds between chunks. It is not an overall execution limit. [Streaming SDK](https://fal.ai/docs/api-reference/client-libraries/javascript/streaming)

The outer request is SSE over direct HTTP rather than a queue submission. A disconnected stream does not prove the child generations stopped. **Cost:** Each model node is billable according to its endpoint, and a failed workflow may already have completed billable steps. The SDK streaming interface does not expose the same arbitrary `headers` option as queue submission; do not assume a workflow propagates per-request retention controls to every child call. For strict per-item controls, use explicit queue calls or confirm workflow behavior with fal. [Streaming](https://fal.ai/docs/documentation/model-apis/inference/streaming), [Workflow execution and step IDs](https://fal.ai/docs/documentation/model-apis/workflows)

# Reference image transport

Model requests are JSON, not multipart attachments. Supply reference images using direct image URLs or supported data URIs.

For a local file, upload once, then use its returned URL:

```javascript
import { readFile } from "node:fs/promises";
import { fal } from "@fal-ai/client";

const referenceUrl = await fal.storage.upload(
  new Blob([await readFile("reference.png")], { type: "image/png" }),
  { lifecycle: { expiresIn: 3600 } }
);
```

Python equivalent:

```python
import fal_client
reference_url = fal_client.upload_file("reference.png")
# To embed a small image in the JSON instead of uploading:
reference_data_uri = fal_client.encode_file("reference.png")
```

Externally hosted or presigned URLs must return the file directly without extra authentication headers or cookies. Their validity must cover queue waiting and processing. Base64 data URIs can avoid a separate upload but enlarge the HTTP body; prefer URLs for ordinary image files. Reuse shared references instead of uploading identical bytes for every item. [fal CDN](https://fal.ai/docs/documentation/model-apis/fal-cdn), [JavaScript storage](https://fal.ai/docs/api-reference/client-libraries/javascript/storage), [Python client](https://fal.ai/docs/api-reference/client-libraries/python/fal_client)

Input uploads have their own expiration and ACL. An output lifecycle header does not modify an earlier reference upload. Set `initialAcl` in the upload lifecycle if references need restricted access. [File access controls](https://fal.ai/docs/documentation/model-apis/file-access-controls)

# Retrieve and save the generated image

| Interface | Image location |
| - | - |
| Direct HTTP or REST queue result | `images[i].url` |
| JavaScript SDK | `result.data.images[i].url` |
| Python SDK | `result["images"][i]["url"]` |
| Workflow above | Output field named after the item ID |

Nano Banana returns `images` and `description`. Each image’s `url` is required; dimensions, file size and other metadata can be null or absent. The generation response is JSON; the URL is the reference to the image bytes. [Generation output schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2)

For a publicly readable output, download with an ordinary GET without attaching `FAL_KEY`:

```javascript
import { writeFile } from "node:fs/promises";

async function saveImage(url, outputPath) {
  const response = await fetch(url);
  if (!response.ok) throw new Error("Download failed: " + response.status);
  await writeFile(outputPath, Buffer.from(await response.arrayBuffer()));
}

// Example after an SDK call:
// await saveImage(result.data.images[0].url, "generated.png");
```

Use a signed URL for private output, as described below. Save every needed image to storage you control, keeping the item ID and fal request ID. Saving the URL alone does not preserve the file. This helper buffers one image; use streaming and size limits for larger downloads.

For these endpoints, `sync_mode: true` returns a data URI and omits output data from request history. It is separate from direct versus queue transport, does not eliminate image-generation charges, and is not a complete zero-retention setting. [Nano Banana API](https://fal.ai/models/fal-ai/nano-banana-2/api)

# Queue operations and recovery

For REST, submit the model JSON to `https://queue.fal.run/fal-ai/nano-banana-2` or its `/edit` endpoint. Save the returned `request_id`, `status_url`, `response_url` and `cancel_url`.

| Operation | HTTP request |
| - | - |
| Submit | POST generation endpoint |
| Check status | GET returned `status_url` |
| Fetch model output | GET returned `response_url` |
| Cancel | PUT returned `cancel_url` |

Use key authentication on these operations. Follow returned URLs: edit request lookups use the app path without the `/edit` endpoint suffix. A URL constructed from the full edit endpoint plus `/requests` can return 405. [Queue reference](https://fal.ai/docs/documentation/model-apis/inference/queue)

Status moves through `IN_QUEUE`, `IN_PROGRESS` and `COMPLETED`. A terminal status can also mean failure or cancellation. Fetching the result returns the original model success or error status. After a client timeout, recover the known request rather than immediately generating the image again. [Status API](https://fal.ai/docs/inference-api/queue/status), [Result API](https://fal.ai/docs/inference-api/queue/result), [Reliability](https://fal.ai/docs/documentation/model-apis/inference/reliability)

In JavaScript, recover with `fal.queue.status(endpoint, { requestId })` and `fal.queue.result(endpoint, { requestId })`. For longer background jobs, webhooks are also supported; verify their signatures and deduplicate by request ID. [Queue SDK](https://fal.ai/docs/api-reference/client-libraries/javascript/queue), [Webhook reference](https://fal.ai/docs/documentation/model-apis/inference/webhooks)

# Retention and deletion

## Three independent retention layers

| Stored item | Documented lifetime or default | Relevant control |
| - | - | - |
| Temporary queue result JSON | One hour after completion; six minutes when the result is 10 KB or larger | Retrieve promptly; payload storage can extend availability |
| Request history inputs and outputs | 90 days by default | `X-Fal-Store-IO` or account policy |
| Uploaded and generated CDN files | Account lifecycle; current settings reference uses no expiry and public access when unconfigured | Upload lifecycle, inference lifecycle header, account storage settings |

The first row concerns the result JSON, not the image’s file size. When payload storage is enabled, the result remains available for that storage period. Turning payload storage off does not erase the short-lived queue result immediately. [Result retention](https://fal.ai/docs/inference-api/queue/result)

`X-Fal-Store-IO: 0` disables request-history input/output storage. It does not delete CDN files or configure deployment logs. Explicit request headers override account defaults. Zero Data Retention requires fal to confirm endpoint coverage and all content storage paths; it is not enabled by default or established by a single header. [Data retention and storage](https://fal.ai/docs/documentation/model-apis/media-expiration)

For CDN settings, `expiration_duration_seconds: null` disables automatic expiration. Expiration and access control are separate: a private file can still expire, and a public file can still have a short lifetime. [Account storage settings](https://fal.ai/docs/platform-apis/v1/storage/settings/get)

## Explicit output retention and access

This Python submission disables request-history payload storage and requests private output files that expire after one hour:

```python
import json
import fal_client

handle = fal_client.submit(
    "fal-ai/nano-banana-2",
    arguments={"prompt": "A ceramic lighthouse", "num_images": 1},
    headers={
        "X-Fal-Store-IO": "0",
        "X-Fal-Object-Lifecycle-Preference": json.dumps({
            "expiration_duration_seconds": 3600,
            "initial_acl": {"default": "forbid", "rules": []},
        }),
    },
)
print(handle.request_id)
```

Pass the same `headers` object to JavaScript `fal.queue.submit`, serializing lifecycle JSON with `JSON.stringify`. These are HTTP controls, separate from model `input` or `arguments`. Output lifecycle is applied when files are created; it does not modify previously uploaded inputs. Download private output using the access methods in the next section. [Platform headers](https://fal.ai/docs/documentation/model-apis/common-parameters), [File access controls](https://fal.ai/docs/documentation/model-apis/file-access-controls)

## Account defaults

Read current defaults:

```sh
curl --fail-with-body 'https://api.fal.ai/v1/storage/settings' \
  -H "Authorization: Key $FAL_KEY"
```

The response has `expiration_duration_seconds` and `initial_acl`; both are null for an account with no saved settings. Reading requires `account:settings:read`, included by the API preset. [Get storage settings](https://fal.ai/docs/platform-apis/v1/storage/settings/get)

To set defaults, `PUT` the complete desired JSON to the same URL. This replaces the configuration: omitted or null fields are cleared, and `{}` clears everything. Read first when changing only one setting. Updating needs `account:settings:write` through FULL or an appropriate custom key. The API explicitly applies to **new uploads**. [Update storage settings](https://fal.ai/docs/platform-apis/v1/storage/settings/update)

## Delete a completed request and its outputs

```sh
curl --fail-with-body -X DELETE \
  "https://api.fal.ai/v1/models/requests/$REQUEST_ID/payloads" \
  -H "Authorization: Key $FAL_KEY"
```

This irreversibly removes request input/output JSON and associated **output** CDN files. It requires both `models:requests:write` and `assets:write`; an ordinary API preset is insufficient. Use a completed request: an unfinished request returns 400. Inspect every `cdn_delete_results[].exception` even after HTTP 200, because individual file deletions can fail. Optional `Idempotency-Key` caches this deletion operation’s response for ten minutes. This is not an inference submission idempotency guarantee. [Delete request payloads API](https://fal.ai/docs/platform-apis/v1/models/requests/payloads)

Input files are retained because other requests may reuse them. To delete an owned input or individual output file, send `POST https://rest.fal.ai/storage/files/delete` with JSON `{"file_url":"<original CDN URL>"}` and key authentication; this needs `assets:write`. Account for every reference before deleting a reused upload. [CDN file deletion](https://fal.ai/docs/documentation/model-apis/fal-cdn)

**Cost:** Do not treat expiration or deletion as a refund mechanism. Retain needed bytes before deleting fal’s copy; regenerating lost output is another generation.

# Private images and signed URLs

An ACL’s default decision is `allow`, `forbid` or `hide`, producing anonymous access outcomes 200, 403 or 404 respectively. The owner retains access. ACL rules can grant named users access. These controls apply to fal CDN v3 files, not arbitrary external storage. [File access controls](https://fal.ai/docs/documentation/model-apis/file-access-controls)

## Share a private image temporarily

On your backend, obtain a signed URL:

```javascript
const originalUrl = image.url; // Original CDN URL, without query parameters.
const endpoint = new URL("https://api.fal.ai/v1/storage/files/sign");
endpoint.searchParams.set("url", originalUrl);

const response = await fetch(endpoint, {
  method: "POST",
  headers: {
    Authorization: "Key " + process.env.FAL_KEY,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({ expiration_seconds: 3600 }),
});
if (!response.ok) throw new Error("Signing failed: HTTP " + response.status);
const { signed_url } = await response.json();
```

This requires `assets:read`. The required duration is 1 to 604800 seconds, up to seven days. The returned `signed_url` grants access regardless of the file ACL; treat it as a credential. Send it to the client for display or normal download. A signature cannot extend the underlying file’s lifecycle or restore a deleted object. [Sign file URL API](https://fal.ai/docs/platform-apis/v1/storage/files/sign)

For an authenticated server download, exchange the API key for a CDN token using `POST https://rest.fal.ai/storage/auth/token?storage_type=fal-cdn-v3` with key authentication and JSON `{}`, then GET the original media URL using `Authorization: Bearer <token>`. Read `expires_at` and refresh as needed. The API key itself is not a CDN download bearer token. [CDN authentication](https://fal.ai/docs/documentation/model-apis/file-access-controls)

Inspect an existing file ACL with `GET https://api.fal.ai/v1/storage/files/acl?url=...`; replace it with `PUT` to the same path. These require `assets:read` and `assets:write` respectively. Verify the applied rules because nonexistent users are dropped. [Get ACL](https://fal.ai/docs/platform-apis/v1/storage/files/acl/get), [Set ACL](https://fal.ai/docs/platform-apis/v1/storage/files/acl/set)

# Cost and settings for this use case

## Published Nano Banana 2 prices

Both the text-to-image and edit model pages currently quote the following base prices:

| Output resolution | Published multiplier | USD per generated image |
| - | - | - |
| `0.5K` | 0.75 | 0.06 |
| `1K` | 1.00 | 0.08 |
| `2K` | 1.50 | 0.12 |
| `4K` | 2.00 | 0.16 |

These are public prices checked on 22 October 2026; your account terms and future rates can differ. Three successful one-image items at 1K cost **USD 0.24** at the quoted base rate; ten cost **USD 0.80**, excluding optional extras. One batch HTTP request does not make those items a single billed image. [Text to image pricing](https://fal.ai/models/fal-ai/nano-banana-2), [Edit pricing](https://fal.ai/models/fal-ai/nano-banana-2/edit)

| Setting or behavior | Cost implication |
| - | - |
| Item count and `num_images` | More output images increase cost; keep `num_images: 1` for one image per distinct prompt |
| `resolution` | Direct price multiplier shown above |
| `enable_web_search` | Additional USD 0.015 if web search is used |
| `thinking_level: "high"` | Additional USD 0.002 if high thinking is used |
| Shared `system_prompt` | No documented grouping or shared-instruction discount |
| Input reference images | These pricing pages do not specify a separate per-reference fee |
| `sync_mode` or file format | No documented reduction in inference price |
| Retry as a new submission | Another successful generation can produce another charge |
| Queue wait or HTTP 5xx failure | Not billed |
| HTTP 422 failure | Can be billed if GPU work occurred before detecting the error |

The model pages do not clearly specify how the search/thinking extras scale within a multi-image request; do not extrapolate a bulk surcharge formula. The examples disable search, omit thinking, and request one image per item. [Model pricing details](https://fal.ai/models/fal-ai/nano-banana-2), [Failure billing FAQ](https://fal.ai/docs/documentation/model-apis/faq)

## Input settings to use explicitly

| Field | Relevant contract |
| - | - |
| `prompt` | Required string, 3 to 50000 characters |
| `system_prompt` | Shared instruction, up to 50000 characters; send on every item |
| `image_urls` | Reference array on the edit endpoint |
| `num_images` | Integer 1 to 4; default 1 |
| `aspect_ratio` | Use `1:1` for square output; `auto` is the default |
| `resolution` | `0.5K`, `1K`, `2K` or `4K`; default `1K` |
| `output_format` | `png`, `jpeg` or `webp`; default `png` |
| `limit_generations` | Experimental, default true; limits generations within a prompting round |
| `enable_web_search` | Default false |
| `thinking_level` | Omit to disable; supported values `minimal` or `high` |
| `sync_mode` | Default false; true returns inline image data |

`limit_generations` is not a distinct-prompt batching mechanism or a billing cap. Do not replace item mapping with a long prompt asking for several unrelated images and assume the response will preserve one-to-one correspondence. [Edit OpenAPI](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2/edit), [Model API behavior](https://fal.ai/models/fal-ai/nano-banana-2/edit/api)

## Query prices and estimate before generating

```sh
curl --fail-with-body --get 'https://api.fal.ai/v1/models/pricing' \
  -H "Authorization: Key $FAL_KEY" \
  --data-urlencode 'endpoint_id=fal-ai/nano-banana-2' \
  --data-urlencode 'endpoint_id=fal-ai/nano-banana-2/edit'
```

Read `prices[].unit_price`, `unit` and `currency`. Model-specific resolution multipliers and optional extras still matter; a unit-price response is not a complete substitute for the endpoint’s pricing rules. [Pricing API](https://fal.ai/docs/platform-apis/v1/models/pricing)

```sh
curl --fail-with-body \
  'https://api.fal.ai/v1/models/pricing/estimate' \
  -H "Authorization: Key $FAL_KEY" \
  -H 'Content-Type: application/json' \
  -d '{
    "estimate_type":"unit_price",
    "endpoints":{
      "fal-ai/nano-banana-2/edit":{"unit_quantity":3}
    }
  }'
```

`unit_quantity` means billing units, not universally prompts or images. Supply the correct expected units and account for resolution/extras separately when needed. An estimate does not reserve spend or enforce a cap. [Estimate API](https://fal.ai/docs/platform-apis/v1/models/pricing/estimate)

For actual spend, reconcile saved request IDs with `cost_subtotal`, `cost_discount` and `cost_total` in billing events. BILLING/FULL or an appropriate custom key is needed for billing-event access. [Billing events](https://fal.ai/docs/platform-apis/v1/models/billing-events)

Model APIs use prepaid credits. Purchased credits expire after 365 days; promotional expiration varies. Cold starts and queue waiting are not billed for Model APIs. Client-side timeouts do not prove that the server stopped, and cancellation of running work carries no documented refund guarantee. [Pricing](https://fal.ai/docs/documentation/model-apis/pricing), [FAQ](https://fal.ai/docs/documentation/model-apis/faq), [Cancellation](https://fal.ai/docs/inference-api/queue/cancel)

The cited Model API pricing documents do not specify separate CDN retention or egress rates; confirm your commercial terms. Your own object storage and delivery provider can have separate storage and transfer charges. A normal image download is a file transfer rather than a new model generation.

# Documentation differences to account for

The FAQ describes CDN files as available for at least seven days, while current lifecycle settings describe no automatic expiration when unconfigured. Set explicit input and output lifetimes and save needed bytes. The ACL guide also describes account defaults as unavailable through self-service, whereas the current Platform API documents GET and PUT storage settings. Use the explicit API and verify saved values. [FAQ](https://fal.ai/docs/documentation/model-apis/faq), [Storage settings](https://fal.ai/docs/platform-apis/v1/storage/settings/get), [Update settings](https://fal.ai/docs/platform-apis/v1/storage/settings/update), [ACL guide](https://fal.ai/docs/documentation/model-apis/file-access-controls)

Generated model OpenAPI paths can include `/edit/requests`, while the current queue protocol requires request lookups without the endpoint suffix. Follow the actual `status_url` and `response_url` returned by submission. [Edit schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2/edit), [Queue protocol](https://fal.ai/docs/documentation/model-apis/inference/queue)

The code examples are original adaptations of the documented SDK and workflow contracts. No paid image generations were executed while preparing this reference.

# Complete reference index

## Model contracts and prices

- [Nano Banana 2 text to image API](https://fal.ai/models/fal-ai/nano-banana-2/api)

- [Text to image OpenAPI JSON](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2)

- [Text to image model and pricing](https://fal.ai/models/fal-ai/nano-banana-2)

- [Nano Banana 2 edit API](https://fal.ai/models/fal-ai/nano-banana-2/edit/api)

- [Edit OpenAPI JSON](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2/edit)

- [Edit model and pricing](https://fal.ai/models/fal-ai/nano-banana-2/edit)

## Calls and orchestration

- [User supplied starting link](https://fal.ai/docs/documentation/model-apis) and [Model APIs overview](https://fal.ai/docs/documentation/model-apis/overview)

- [Authentication and permissions](https://fal.ai/docs/documentation/model-apis/authentication/key-based)

- [Client setup](https://fal.ai/docs/documentation/model-apis/inference/client-setup)

- [JavaScript queue SDK](https://fal.ai/docs/api-reference/client-libraries/javascript/queue)

- [Python client SDK](https://fal.ai/docs/api-reference/client-libraries/python/fal_client)

- [Direct and queue backed calls](https://fal.ai/docs/documentation/model-apis/inference/synchronous)

- [Raw direct HTTP](https://fal.ai/docs/inference-api/sync/run)

- [Queue lifecycle](https://fal.ai/docs/documentation/model-apis/inference/queue)

- [Raw queue submission](https://fal.ai/docs/inference-api/queue/submit)

- [Status](https://fal.ai/docs/inference-api/queue/status), [result](https://fal.ai/docs/inference-api/queue/result), [cancellation](https://fal.ai/docs/inference-api/queue/cancel)

- [Inline workflow construction](https://fal.ai/docs/examples/integrations/custom-workflow-ui)

- [Workflow events and step IDs](https://fal.ai/docs/documentation/model-apis/workflows)

- [Streaming transport](https://fal.ai/docs/documentation/model-apis/inference/streaming)

- [JavaScript streaming SDK completion and timeouts](https://fal.ai/docs/api-reference/client-libraries/javascript/streaming)

- [Reliability](https://fal.ai/docs/documentation/model-apis/inference/reliability), [concurrency](https://fal.ai/docs/documentation/model-apis/concurrency-limits), [webhooks](https://fal.ai/docs/documentation/model-apis/inference/webhooks)

- [Browser proxy configuration](https://fal.ai/docs/documentation/model-apis/inference/proxy-setup)

## Images retention and access

- [fal CDN and file deletion](https://fal.ai/docs/documentation/model-apis/fal-cdn)

- [JavaScript upload lifecycle](https://fal.ai/docs/api-reference/client-libraries/javascript/storage)

- [Retention and ZDR](https://fal.ai/docs/documentation/model-apis/media-expiration)

- [Platform headers](https://fal.ai/docs/documentation/model-apis/common-parameters)

- [File access and CDN authentication](https://fal.ai/docs/documentation/model-apis/file-access-controls)

- [Get account storage settings](https://fal.ai/docs/platform-apis/v1/storage/settings/get)

- [Update account storage settings](https://fal.ai/docs/platform-apis/v1/storage/settings/update)

- [Delete request payloads and outputs](https://fal.ai/docs/platform-apis/v1/models/requests/payloads)

- [Sign a private file URL](https://fal.ai/docs/platform-apis/v1/storage/files/sign)

- [Read file ACL](https://fal.ai/docs/platform-apis/v1/storage/files/acl/get), [replace file ACL](https://fal.ai/docs/platform-apis/v1/storage/files/acl/set)

## Cost and billing

- [Model API pricing rules](https://fal.ai/docs/documentation/model-apis/pricing)

- [Live price lookup](https://fal.ai/docs/platform-apis/v1/models/pricing)

- [Cost estimates](https://fal.ai/docs/platform-apis/v1/models/pricing/estimate)

- [Actual billing events](https://fal.ai/docs/platform-apis/v1/models/billing-events)

- [Billing and storage FAQ](https://fal.ai/docs/documentation/model-apis/faq)

- [Full documentation index](https://fal.ai/docs/llms.txt)
