Verified against official fal documentation on 22 October 2026.

| Scope | Contract |
| - | - |
| Workload | Shared instruction + distinct item prompts + optional per-item references |
| Detailed runnable examples | Nano Banana 2; alternatives in model matrix |
| Shared instruction | Native `system_prompt` |
| Reference editing | `fal-ai/nano-banana-2/edit` + `image_urls` |

| Application requirement | Design |
| - | - |
| Explicit prompt/reference mapping | One model call per item; retain item ID ↔ request ID |
| One client request | Backend batch route coordinates calls |
| One generation request to fal | Inline Workflow; one node per item |
| Billing | Each generated output/model step remains billable |

[Generation API](https://fal.ai/models/fal-ai/nano-banana-2/api), [edit API](https://fal.ai/models/fal-ai/nano-banana-2/edit/api), [Workflows](https://fal.ai/docs/documentation/model-apis/workflows)

# Available image models and selection

**Catalog scope:** practical image-generation/editing options verified on 22 October 2026. Use discovery below for the complete, changing endpoint catalog. Existing runnable examples target **Nano Banana 2**; alternative schemas and prices are separate contracts.

## Model matrix

| Family | Text-to-image endpoint | Reference-edit endpoint | Shared instruction | Sizing |
| - | - | - | - | - |
| Nano Banana 2.1 | `google/nano-banana-2.1` | `google/nano-banana-2.1/edit` | `system_prompt` | `aspect_ratio` + `resolution` |
| Nano Banana 2 | `fal-ai/nano-banana-2` | `fal-ai/nano-banana-2/edit` | `system_prompt` | `aspect_ratio` + `resolution` |
| Nano Banana Pro | `fal-ai/nano-banana-pro` | `fal-ai/nano-banana-pro/edit` | `system_prompt` | `aspect_ratio` + `resolution` |
| GPT Image 2.5 Flare | `openai/gpt-image-2.5/flare/text-to-image` | `openai/gpt-image-2.5/flare/edit` | Compose into `prompt` | `image_size` + `quality` |
| GPT Image 2.5 Sunburst | `openai/gpt-image-2.5/sunburst/text-to-image` | `openai/gpt-image-2.5/sunburst/edit` | Compose into `prompt` | `image_size` + `quality` |
| FLUX.1 schnell | `fal-ai/flux/schnell` | No prompt+reference edit contract documented here | Compose into `prompt` | `image_size` |
| FLUX.2 klein 4B | `fal-ai/flux-2/klein/4b` | `fal-ai/flux-2/klein/4b/edit` | Compose into `prompt` | `image_size` |
| FLUX.2 klein 9B | `fal-ai/flux-2/klein/9b` | `fal-ai/flux-2/klein/9b/edit` | Compose into `prompt` | `image_size` |
| FLUX.2 Turbo | `fal-ai/flux-2/turbo` | `fal-ai/flux-2/turbo/edit` | Compose into `prompt` | `image_size` |

**Schema references:** [Nano 2.1](https://fal.ai/models/google/nano-banana-2.1/api), [Nano 2.1 edit](https://fal.ai/models/google/nano-banana-2.1/edit/api), [Nano 2](https://fal.ai/models/fal-ai/nano-banana-2/api), [Nano Pro edit](https://fal.ai/models/fal-ai/nano-banana-pro/edit/api), [GPT 2.5 Flare](https://fal.ai/models/openai/gpt-image-2.5/flare/edit/api), [GPT 2.5 Sunburst](https://fal.ai/models/openai/gpt-image-2.5/sunburst/edit/api), [schnell](https://fal.ai/models/fal-ai/flux/schnell/api), [klein 4B](https://fal.ai/models/fal-ai/flux-2/klein/4b/api), [klein 9B](https://fal.ai/models/fal-ai/flux-2/klein/9b/api), [Turbo](https://fal.ai/models/fal-ai/flux-2/turbo/api).

| Reference contract | Field / limit |
| - | - |
| Nano 2 / 2.1 / Pro edits | `image_urls`; up to 14 advertised |
| GPT 2.5 edits | `image_urls`; up to 16; optional `mask_url` |
| Klein edits | `image_urls`; up to 4 |
| Turbo edit | `image_urls`; first 4 used |

[Google reference limits](https://fal.ai/nano-banana-2.1), [Pro](https://fal.ai/nano-banana-pro), [GPT schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=openai/gpt-image-2.5/flare/edit), [klein edit](https://fal.ai/models/fal-ai/flux-2/klein/4b/edit/api), [Turbo edit](https://fal.ai/models/fal-ai/flux-2/turbo/edit/api).

**Models without a system field:** repeat the common instruction inside each prompt. This is ordinary prompt composition; it has no system-message priority.

```javascript
const prompt = [sharedInstruction, "Scene:", item.prompt].join("\n\n");
```

## Published price comparison

| Model | Generation price | Reference-edit difference |
| - | - | - |
| Nano 2.1 | Token based; representative 1K ≈ $0.040, 2K ≈ $0.059, 4K ≈ $0.134 | Input image tokens add cost |
| Nano 2 | $0.06 / $0.08 / $0.12 / $0.16 at 0.5K / 1K / 2K / 4K | Same published output tiers; no separate reference fee listed |
| Nano Pro | $0.15 at 1K/2K; $0.30 at 4K | Check endpoint extras |
| GPT 2.5, either variant | 1024-square output only: low $0.00588; medium $0.01317; high $0.05268 | Add image/text inputs; one small-reference total ≈ $0.0142 / $0.0215 / $0.0610 |
| Schnell | $0.003/MP; whole-MP rounding | Separate reference model required |
| Klein 4B | $0.005/MP | $0.01/MP; page does not clarify input/output denominator |
| Klein 9B | $0.006/MP | $0.011/input+output MP; inputs resized to 1 MP |
| Turbo | $0.008/MP | $0.008/input+output MP; inputs resized to 1 MP |

**Price sources:** [Nano 2.1 token rates/examples](https://fal.ai/models/google/nano-banana-2.1), [Nano 2](https://fal.ai/models/fal-ai/nano-banana-2), [Pro](https://fal.ai/nano-banana-pro), [GPT 2.5 breakdown](https://fal.ai/models/openai/gpt-image-2.5/flare/edit), [schnell](https://fal.ai/models/fal-ai/flux/schnell), [klein 4B generation](https://fal.ai/models/fal-ai/flux-2/klein/4b), [4B edit](https://fal.ai/models/fal-ai/flux-2/klein/4b/edit), [klein 9B generation](https://fal.ai/models/fal-ai/flux-2/klein/9b), [9B edit](https://fal.ai/models/fal-ai/flux-2/klein/9b/edit), [Turbo generation](https://fal.ai/models/fal-ai/flux-2/turbo), [Turbo edit](https://fal.ai/models/fal-ai/flux-2/turbo/edit).

- Nano 2.1 examples: short prompt + medium thinking; representative totals, not fixed quotes.

- GPT totals: one 1024-square output + one small reference + short prompt; larger/more references add cost.

- MP denominator/minimum/rounding: endpoint-specific; do not apply a universal pixel-to-price formula.

- Published price comparisons do not establish equal image quality.

## Other available text-to-image endpoints

| Endpoint | Published rate | Size control |
| - | - | - |
| `fal-ai/flux-2` (dev) | $0.012/MP | `image_size`; custom edges 512–2048 |
| `fal-ai/flux-2-pro` | $0.03 first output MP; $0.015/additional input or output MP | Custom edges 256–2560; multiples of 16; area ≤4194304 |
| `fal-ai/bytedance/seedream/v5/lite/text-to-image` | $0.035/image | `image_size`; common presets + `auto_2K/auto_3K/auto_4K` |
| `fal-ai/bytedance/seedream/v4.5/text-to-image` | $0.04/image | `image_size`; common presets + `auto_2K/auto_4K` |

- **FLUX Pro MP:** explicitly 1024×1024, rounded upward; documented 1920×1080 output = $0.045.

- **Seedream count controls:** set both `num_images: 1` and `max_images: 1`.

- **Shared instruction:** compose into `prompt`; these schemas lack `system_prompt`.

- **Reference editing:** requires the appropriate separate endpoint/schema.

[Dev price/API](https://fal.ai/models/fal-ai/flux-2/api), [Dev price](https://fal.ai/models/fal-ai/flux-2), [Pro schema](https://fal.ai/models/fal-ai/flux-2-pro/api), [Pro price/MP rules](https://fal.ai/models/fal-ai/flux-2-pro), [Seedream 5 Lite schema](https://fal.ai/models/fal-ai/bytedance/seedream/v5/lite/text-to-image/api), [5 Lite price](https://fal.ai/models/fal-ai/bytedance/seedream/v5/lite/text-to-image), [4.5 schema](https://fal.ai/models/fal-ai/bytedance/seedream/v4.5/text-to-image/api), [4.5 price](https://fal.ai/models/fal-ai/bytedance/seedream/v4.5/text-to-image).

## Discover the complete available catalog

```sh
curl --fail-with-body --get 'https://api.fal.ai/v1/models' \
  -H "Authorization: Key $FAL_KEY" \
  --data-urlencode 'category=text-to-image' \
  --data-urlencode 'status=active' \
  --data-urlencode 'limit=50'
```

| Discovery control | Usage |
| - | - |
| Editing category | Repeat with `category=image-to-image` |
| Search | `q=...` |
| Specific endpoints | Repeated `endpoint_id`; 1–50 IDs |
| Full schema | `expand=openapi-3.0` |
| Ordering | `sort=relevant` or `recent` |
| Pagination | Follow `next_cursor` while `has_more`; preserve filters/order/limit |
| Return | `models[].endpoint_id` + metadata |
| Limit | 50 is this example, not a universal API maximum |
| Snapshot | Cursor pages are not an immutable catalog snapshot |

[Model discovery API](https://fal.ai/docs/platform-apis/v1/models)

# Aspect ratio and resolution

## Nano Banana family

| Parameter | Nano 2 | Nano 2.1 | Nano Pro |
| - | - | - | - |
| `aspect_ratio` default | `auto` | `auto` | `auto` |
| Fixed ratios | Base + extreme list below | Base + extreme list below | Base list |
| `resolution` | `0.5K`, `1K`, `2K`, `4K` | `1K`, `2K`, `4K` | `1K`, `2K`, `4K` |
| Resolution default | `1K` | `1K` | `1K` |

```text
Base:    21:9, 16:9, 3:2, 4:3, 5:4, 1:1, 4:5, 3:4, 2:3, 9:16
Extreme: 4:1, 1:4, 8:1, 1:8
```

[2 API](https://fal.ai/models/fal-ai/nano-banana-2/edit/api), [2.1 API](https://fal.ai/models/google/nano-banana-2.1/edit/api), [Pro API](https://fal.ai/models/fal-ai/nano-banana-pro/edit/api).

**Nano 2 — square draft:**

```json
{
  "system_prompt": "Use watercolor and muted blues.",
  "prompt": "A lighthouse above a rocky bay.",
  "aspect_ratio": "1:1",
  "resolution": "0.5K",
  "num_images": 1,
  "limit_generations": true,
  "enable_web_search": false
}
```

**Nano 2.1 — portrait with reference:** endpoint `google/nano-banana-2.1/edit`.

```json
{
  "system_prompt": "Use watercolor and muted blues.",
  "prompt": "Preserve this lighthouse; place it beside a rocky bay.",
  "image_urls": ["https://your-domain.example/lighthouse.png"],
  "aspect_ratio": "4:5",
  "resolution": "1K",
  "thinking_level": "minimal",
  "enable_web_search": false,
  "num_images": 1,
  "limit_generations": true
}
```

- Set the ratio explicitly for a fixed placement.

- Resolution labels select tiers; exact output pixel dimensions depend on model and ratio.

- Read returned dimensions when present; otherwise inspect downloaded bytes.

- Nano 2.1 has no `0.5K`; omitted thinking defaults to `medium`, unlike Nano 2.

[Version differences](https://fal.ai/learn/devs/nano-banana-2-1-vs-nano-banana-2), [output schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2)

## FLUX presets and explicit pixels

| `image_size` preset | Width × height | Ratio |
| - | - | - |
| `square` | 512 × 512 | 1:1 |
| `square_hd` | 1024 × 1024 | 1:1 |
| `portrait_4_3` | 768 × 1024 | 3:4 |
| `portrait_16_9` | 576 × 1024 | 9:16 |
| `landscape_4_3` | 1024 × 768 | 4:3 |
| `landscape_16_9` | 1024 × 576 | 16:9 |

[Common size presets](https://fal.ai/docs/documentation/model-apis/model-arguments)

**Explicit landscape:** use `image_size`; do not send Nano Banana's ratio/resolution fields.

```json
{
  "prompt": "Watercolor in muted blues. A lighthouse above a rocky bay.",
  "image_size": { "width": 1280, "height": 720 },
  "num_images": 1
}
```

| Endpoint sizing detail | Contract |
| - | - |
| Schnell / klein text-to-image | Default `landscape_4_3`; custom dimensions supported |
| Turbo | Each custom edge 512–2048 |
| Klein edit | Omitted size follows input size |
| Turbo edit | Default `square_hd` |

[Schnell](https://fal.ai/models/fal-ai/flux/schnell/api), [klein](https://fal.ai/models/fal-ai/flux-2/klein/4b/edit/api), [Turbo](https://fal.ai/models/fal-ai/flux-2/turbo/api), [Turbo edit](https://fal.ai/models/fal-ai/flux-2/turbo/edit/api)

## GPT Image 2.5 dimensions and quality

| Control | Values / constraints |
| - | - |
| `image_size` | Six preset names above, `auto`, or `{width,height}` |
| Custom dimensions | Both divisible by 16 |
| Long edge | ≤3840 |
| Aspect ratio | ≤3:1 |
| Pixel area | 655360–8294400 |
| `quality` | `auto`, `low`, `medium`, `high`, `xhigh`, `max` |
| Quality default | `high` |
| `auto` quality | Can select `max` |
| Output compression | 0–100 for JPEG/WebP; file-size control |

**Near Full HD:** 1920 × 1072 satisfies the divisibility rule; 1920 × 1080 is adjusted by the service. Preset names do not guarantee FLUX pixel dimensions; GPT enforces its own minimum area. [Dimensions](https://fal.ai/gpt-image-2.5), [API](https://fal.ai/models/openai/gpt-image-2.5/flare/edit/api), [adjustments and pricing](https://fal.ai/models/openai/gpt-image-2.5/flare/edit).

```json
{
  "prompt": "Use watercolor and muted blues. Preserve the reference lighthouse beside a rocky bay.",
  "image_urls": ["https://your-domain.example/lighthouse.png"],
  "image_size": { "width": 1920, "height": 1072 },
  "quality": "low",
  "num_images": 1
}
```

**Endpoint:** `openai/gpt-image-2.5/flare/edit`. **Count discrepancy:** landing page says up to 4; current edit OpenAPI permits 10. Examples request 1. [Current schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=openai/gpt-image-2.5/flare/edit)

# Reducing generation costs

| Lever | Concrete configuration | Price effect / tradeoff |
| - | - | - |
| Cheaper draft model | Schnell; klein; Turbo | Lower advertised MP rates; evaluate quality for your prompts |
| Native shared instruction + lower-cost candidate | Evaluate Nano 2.1 at 1K | Token based; representative short-prompt rate below Nano 2 |
| Smaller Nano 2 output | `resolution: "0.5K"` | $0.06 vs $0.08 at 1K: 25% lower base cost |
| Smaller MP-billed output | Reduce `image_size` area | Savings subject to endpoint rounding/minimums |
| Lower GPT quality | Explicit `low` / `medium` | Lower image-token cost; less detail; avoid cost-unpredictable `auto` |
| Fewer outputs | `num_images: 1`; Nano `limit_generations: true` | Avoid unnecessary variants/intermediate generations |
| Disable unnecessary grounding | `enable_web_search: false` | Nano 2 avoids $0.015 when used; 2.1 avoids search-input tokens |
| Reduce thinking | Nano 2: omit; Nano 2.1: `minimal` | Different version semantics; 2.1 thinking tokens are billable |
| Fewer references | Keep only useful references | Saves input tokens/MP on GPT, Nano 2.1 and relevant FLUX edits |
| Reuse accepted outputs | Persist bytes + request/item IDs | Avoid paid regeneration |
| Recover before retrying | Fetch known request's status/result | Avoid duplicate successful generations |
| Disable unneeded prompt expansion | `enable_prompt_expansion: false` where supported | Avoid additional LLM step |
| Attribute and reconcile | Tags + billing events | Detect waste; no direct rate discount |

[Cost levers: Nano 2.1](https://fal.ai/models/google/nano-banana-2.1), [Nano 2](https://fal.ai/models/fal-ai/nano-banana-2), [GPT](https://fal.ai/models/openai/gpt-image-2.5/flare/edit), [MP billing](https://fal.ai/models/fal-ai/flux/schnell), [prompt expansion](https://fal.ai/docs/documentation/model-apis/model-arguments), [recovery](https://fal.ai/docs/documentation/model-apis/inference/reliability).

| Example workload | Published-rate illustration |
| - | - |
| 1000 Nano 2 outputs at 1K | $80 base |
| 1000 Nano 2 outputs at 0.5K | $60 base; saves $20 |
| 1000 Schnell outputs at one billable MP each | $3 |
| One Turbo edit: one input + one output at quoted 1-MP units | $0.016 |

**Comparison limits:** same image count does not establish equal detail/quality; extras and account terms can change totals. [Nano price](https://fal.ai/models/fal-ai/nano-banana-2), [Schnell price](https://fal.ai/models/fal-ai/flux/schnell), [Turbo edit example](https://fal.ai/models/fal-ai/flux-2/turbo/edit).

**No documented inference discount from:** one batch HTTP request, shared system prompt, Workflow grouping, queue priority, seed, image-file format, shorter retention or faster acceleration. Token-billed models still charge repeated instruction tokens. Fewer diffusion steps may reduce latency without reducing an output-unit price. Validate the endpoint's pricing before relying on either optimization.

**Application spend control:** enforce per-user/item limits, choose endpoint/quality/resolution server-side, estimate before submission, persist request IDs, reconcile actual billing. An estimate, start timeout or cancellation is not a hard dollar cap.

# What one request can contain

| Need | Supported approach |
| - | - |
| One prompt, one image | `prompt` with `num_images: 1` |
| One prompt, several variations | One model request with `num_images` from 1 to 4 |
| Several scenes under one shared instruction | One combined `prompt` with `limit_generations: false`; model-directed results |
| Different prompts with explicit per-item mapping | Separate model calls using the same `system_prompt` |
| Different reference image for each prompt | Separate edit calls, each with its own `image_urls` |
| One client HTTP request for all items | Your backend accepts a batch and coordinates the model calls |
| One generation HTTP request directly to fal | A Workflow with one model node per item, delivered as an SSE stream |

- `prompt`: one string; no `prompts[]` or per-output prompt map.

- `num_images`: output count for that prompt.

- `system_prompt`: send the shared instruction in every model call.

[Generation schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2), [edit schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2/edit)

## Several images from one combined prompt

**Native multi-scene request:** combine scenes in one `prompt`; set `limit_generations: false` to allow additional generations within the prompting round.

```json
{
  "system_prompt": "Use watercolor and muted blues for every image.",
  "prompt": "Generate two separate images, not a collage. Image 1: a lighthouse. Image 2: a fishing boat.",
  "num_images": 1,
  "limit_generations": false,
  "resolution": "1K",
  "output_format": "png",
  "enable_web_search": false
}
```

| Property | Combined-prompt behavior |
| - | - |
| Exact count/order | Not documented as guaranteed |
| Prompt ↔ output mapping | Model-directed |
| Reference ↔ output mapping | No structured per-item contract |
| Predictable pairing | Separate calls or Workflow nodes |
| Cost cap | `num_images: 1` is insufficient when additional generations are allowed |

[Generation controls](https://fal.ai/models/fal-ai/nano-banana-2/api)

# Setup

**Prerequisite:** server-side secret from [Dashboard keys](https://fal.ai/dashboard/keys); API permission preset for ordinary inference/file operations.

```sh
export FAL_KEY='your-secret-key'
npm install @fal-ai/client
# Or for Python:
python -m pip install fal-client
```

| Authentication surface | Configuration |
| - | - |
| SDK | Reads `FAL_KEY` |
| HTTP | `Authorization: Key <secret>` |
| Your batch route | Application authentication + usage limits |
| Browser | Keep fal key on backend; use proxy where needed |

[Authentication](https://fal.ai/docs/documentation/model-apis/authentication/key-based), [client setup](https://fal.ai/docs/documentation/model-apis/inference/client-setup), [proxy](https://fal.ai/docs/documentation/model-apis/inference/proxy-setup)

# Common parameters

## Configuration layers

| Layer | JavaScript | Python | HTTP |
| - | - | - | - |
| Model inputs | `input` | `arguments` | JSON body |
| Platform controls | `headers` where supported | `headers` | Request headers |
| SDK controls | Call options | Keyword arguments | Client behavior or generated headers |

**Rule:** `prompt`, `system_prompt` and `image_urls` belong to the model body. Routing, retention, retries and tags belong to platform controls. SDK availability depends on the calling method. [Platform parameters](https://fal.ai/docs/documentation/model-apis/common-parameters)

## Request header matrix

| Header | Values and default | Effect |
| - | - | - |
| `X-Fal-Request-Start-Timeout` | Seconds >0.1; unset | Pre-execution deadline |
| `X-Fal-Runner-Hint` | String; automatic | Best-effort runner affinity |
| `X-Fal-Queue-Priority` | `normal` or `low`; `normal` | Queue priority |
| `X-Fal-Store-IO` | `0` or `1`; account setting | Request-history payload storage |
| `X-Fal-Object-Lifecycle-Preference` | JSON; account setting | Output expiry and ACL |
| `X-Fal-No-Retry` | `1`, `true`, `yes`; off | Disable queue retries |
| `X-Fal-Retry-Config` | JSON; app/platform policy | Owned-app retry budgets |
| `x-app-fal-disable-fallback` | `true`; off | Request no model fallback |
| `X-Fal-Tags` | `key=value,...`; none | Spend attribution |
| `X-Fal-Workflow-Envelope` | `true`; `false` | Workflow step request IDs |

[Header definitions](https://fal.ai/docs/documentation/model-apis/common-parameters), [HTTP queue controls](https://fal.ai/docs/inference-api/queue/submit), [direct HTTP controls](https://fal.ai/docs/inference-api/sync/run)

- **Start deadline:** includes queueing, routing and earlier failed attempts; stops applying once an attempt runs.

- **Affinity:** soft hint; another runner may execute the request.

- **Priority:** `low` affects your requests for that endpoint; does not buy higher account priority.

- **History:** platform default is enabled for 90 days; `0` disables history payloads, not CDN media or deployment logs.

- **Lifecycle:** inference header controls outputs; reference uploads need their own policy. Explicit JSON `null` and omitted configuration differ; see expiration table.

- **Fallback:** model fallback and retry are separate controls.

[Queue contract](https://fal.ai/docs/inference-api/queue/submit), [retention](https://fal.ai/docs/documentation/model-apis/media-expiration), [file ACL scope](https://fal.ai/docs/documentation/model-apis/file-access-controls)

## Lifecycle and retry shapes

```json
{
  "expiration_duration_seconds": 3600,
  "initial_acl": { "default": "forbid", "rules": [] }
}
```

Serialize the object above into `X-Fal-Object-Lifecycle-Preference`.

```json
{
  "timeout": { "retries": 0 },
  "connection_error": { "retries": 2 },
  "server_error": { "retries": 3 }
}
```

Serialize the object above into `X-Fal-Retry-Config`.

| Retry rule | Contract |
| - | - |
| Applicability | Only when you own the app; ignored on public/shared models you do not own |
| Per-condition `retries` | 0 to 10 additional attempts |
| Global ceiling | 10 total execution attempts |
| Precedence | No-retry header → owned-app request config → app config → platform policy |
| Omitted conditions | Platform defaults |
| Invalid config | Invalid JSON/conditions/shorthand can be silently ignored |
| Direct HTTP | No server queue execution retries |
| SDK retries | Separate client layer; can repeat HTTP attempts |

The dedicated retry policy gives a ten-total-attempt ceiling; some overview text says “ten retries.” Use the explicit ceiling for planning. [Retry policy and precedence](https://fal.ai/docs/documentation/serverless/reliability/retries), [HTTP retry header](https://fal.ai/docs/inference-api/queue/submit)

## Queue admission query

```text
POST https://queue.fal.run/fal-ai/nano-banana-2?fal_max_queue_length=10
```

| Parameter | Scope | Result |
| - | - | - |
| `fal_max_queue_length` | Endpoint queue across callers; unset = no guard | 429 when admission guard triggers |

Use the URL query, not a header. The docs differ at the equality boundary (`>N` versus `≥N`); do not use this as an exact batch-count or budget limit. [Queue admission](https://fal.ai/docs/inference-api/queue/submit), [parameter reference](https://fal.ai/docs/documentation/model-apis/common-parameters#fal_max_queue_length)

## Response header matrix

| Header | Meaning |
| - | - |
| `x-fal-request-id` | Request identity |
| `X-Fal-Billable-Units` | Billed quantity; not currency |
| `X-Fal-Served-From` | Serving runner |
| `X-Fal-Request-Timeout-Type: user` | User start deadline exceeded |
| `X-Fal-Error-Type` | Machine-readable failure class |
| `x-fal-runner-hints` | Routing hints |
| `X-Fal-Tags-Dropped: 1` | Some tag input was discarded |

- Request alias `X-Fal-Request-Timeout` is deprecated; `X-Fal-Request-Start-Timeout` wins if both are sent.

- Response headers total at most 16 KB, including application headers.

- Inspect raw headers with `curl -i` or your HTTP client; SDK results do not necessarily expose them.

[Response headers](https://fal.ai/docs/documentation/model-apis/common-parameters#response-headers)

# Request tags

## Wire contract

```http
X-Fal-Tags: team=design,env=prod,feature=image-generation
```

| Rule | Value |
| - | - |
| Maximum entries | 10 before deduplication |
| Key | Up to 64 characters; letters, digits, `.`, `_`, `-`; normalized lowercase |
| Value | Up to 256 printable ASCII characters; no comma/control characters |
| Combined key/value size | 1 KB |
| Separator | Comma between entries; first `=` divides key/value |
| Whitespace | Surrounding whitespace trimmed |
| Duplicate keys | Last value wins |
| Reserved keys | `fal.` prefix |
| Invalid entries | Dropped individually; inference continues |
| Drop indicator | Response `X-Fal-Tags-Dropped: 1` |

- **Transport:** authenticated HTTP queue, direct and SSE calls; WebSocket tags are not captured.

- **Effect:** billing metadata; no model input or price change.

- **Reporting:** activate account reporting and desired key/value pairs through fal; captured historical tags can become reportable after activation.

- **Own apps:** captured tags lack per-request cost attribution.

- **Data:** tags remain with billing records; do not put secrets or personal data in values.

- **SDK:** raw header where supported; no dedicated `tags` option.

- **Cost:** tags do not reduce rates; use them to attribute spend.

[Request Tags](https://fal.ai/docs/documentation/model-apis/request-tags)

## Tagged reporting APIs

| API | Source selector | Output |
| - | - | - |
| `GET /v1/models/usage` | `tagged-estimate` | Estimated usage with `tags` |
| `GET /v1/models/billing-events` | `tagged-billed` | Per-request billing events with `tags` |
| `GET /v1/account/focus` | `tagged-estimate` | FOCUS CSV; `Tags` column; enterprise enablement |

```sh
curl --fail-with-body --get 'https://api.fal.ai/v1/models/usage' \
  -H "Authorization: Key $FAL_KEY" \
  --data-urlencode 'source=tagged-estimate' \
  --data-urlencode 'tag=env=prod' \
  --data-urlencode 'tag=team=design'
```

- **Filters:** 1 to 10; different keys AND; repeated same key OR; `(untagged)` matches a missing key.

- **Permissions:** `billing:usage:read` + `auth:keys:read` + `account:users:read`; BILLING/FULL or custom key.

- **Availability:** activated tagged reporting required; recent tagged rows are delayed.

- **Actual spend:** sum billing-event `cost_total`; retain `request_id` for reconciliation.

[Usage API](https://fal.ai/docs/platform-apis/v1/models/usage), [Billing Events API](https://fal.ai/docs/platform-apis/v1/models/billing-events), [FOCUS export](https://fal.ai/docs/platform-apis/v1/account/focus)

**Workflow attribution:** do not assume tag inheritance. Use `X-Fal-Workflow-Envelope: true` on queue submission to obtain node `endpoint` and `request_id`, then reconcile step billing events. Envelope result: `{"output":...,"nodes":...}`. [Workflow step request IDs](https://fal.ai/docs/documentation/model-apis/workflows#step-request-ids)

# Synchronous requests and progress

## Execution paths

```text
run       → POST fal.run/{endpoint} → final model JSON
subscribe → queue submit → request_id → monitor status → fetch final JSON
submit    → queue submit → request_id returned immediately
```

| Capability | Direct run | Subscribe |
| - | - | - |
| Durable queue | No | Yes |
| Final output returned | Yes | Yes |
| Server queue retries | No | Yes |
| SDK transport retries | Possible | Possible |
| Queue position and log callbacks | No | Yes |
| Guaranteed partial image previews | No | No |
| Saved-ID recovery | No queue recovery contract | Yes |
| Immediate job-ID return | Use another method | Use `submit` |
| Language async API | JS Promise; Python `run_async` | JS Promise; Python `subscribe_async` |
| Cancel through queue API | No queue handle | Save request ID first |

**Synchronous interface** means waiting for the final result. It does not establish one HTTP exchange. Language async functions do not establish use of the queue. [Synchronous methods](https://fal.ai/docs/documentation/model-apis/inference/synchronous), [JavaScript client](https://fal.ai/docs/api-reference/client-libraries/javascript/client)

## SDK capability matrix

| Control | JavaScript | Python |
| - | - | - |
| Model body | `input`; both | `arguments`; both |
| Start deadline | `startTimeout` seconds; both | `start_timeout` seconds; both |
| Client HTTP timeout | No `run.timeout` option | `run(timeout=...)` seconds |
| Subscription wait limit | `subscribe.timeout` milliseconds | `subscribe(client_timeout=...)` seconds |
| Poll interval | `pollInterval` milliseconds | `interval` seconds |
| Status transport | `mode: "polling"` or `"streaming"` | Status polling |
| Include logs | `logs: true` | `with_logs=True` |
| Enqueued callback | `onEnqueue(requestId)` | `on_enqueue(request_id)` |
| Status callback | `onQueueUpdate(update)` | `on_queue_update(update)` |
| Custom headers | `subscribe` / `queue.submit`; not `run` | `run` / `subscribe` / `submit` |
| Named affinity | Use `queue.submit({hint})` | `hint` |
| Priority | Queue `priority` | Queue `priority` |
| Object lifecycle | `storageSettings` or supported raw headers | Raw `headers` |
| Local abort | `abortSignal` | Stop/cancel the waiting task |

**JS precedence:** named submission options override their corresponding duplicate headers. Prefer one representation per control. Current docs use both `objectLifecycle` and `storageSettings` in different type comments; these examples use the raw lifecycle header on queue calls. [RunOptions](https://fal.ai/docs/api-reference/client-libraries/javascript/types.common#runoptions), [queue options](https://fal.ai/docs/api-reference/client-libraries/javascript/queue), [Python client](https://fal.ai/docs/api-reference/client-libraries/python/fal_client)

## Direct SDK calls

JavaScript:

```javascript
import { fal } from "@fal-ai/client";

const direct = await fal.run("fal-ai/nano-banana-2", {
  input: {
    system_prompt: "Use watercolor and muted blues.",
    prompt: "A lighthouse above a rocky bay.",
    num_images: 1,
    output_format: "png",
  },
  startTimeout: 30,
});
console.log(direct.requestId, direct.data.images[0].url);
```

Python:

```python
import fal_client

direct = fal_client.run(
    "fal-ai/nano-banana-2",
    arguments={
        "system_prompt": "Use watercolor and muted blues.",
        "prompt": "A lighthouse above a rocky bay.",
        "num_images": 1,
        "output_format": "png",
    },
    start_timeout=30,
    timeout=120,
)
print(direct["images"][0]["url"])
```

**Direct custom headers:** use raw HTTP or Python `headers`. JavaScript `fal.run` does not expose arbitrary headers, `hint` or a `timeout` option. Raw direct HTTP has no server queue retry; SDK transport retries are separate. [Direct call parameters](https://fal.ai/docs/documentation/model-apis/inference/synchronous), [HTTP run contract](https://fal.ai/docs/inference-api/sync/run)

## Subscribe with progress

JavaScript; `timeout` and `pollInterval` are milliseconds:

```javascript
import { fal } from "@fal-ai/client";

let requestId;
const result = await fal.subscribe("fal-ai/nano-banana-2", {
  input: {
    system_prompt: "Use watercolor and muted blues.",
    prompt: "A lighthouse above a rocky bay.",
    num_images: 1,
    resolution: "1K",
    enable_web_search: false,
  },
  headers: { "X-Fal-Tags": "team=design,env=prod" },
  logs: true,
  mode: "polling",
  pollInterval: 1000,
  startTimeout: 30,
  timeout: 120000,
  onEnqueue: id => {
    requestId = id; // Persist before relying on recovery.
  },
  onQueueUpdate: update => {
    if (update.status === "IN_QUEUE") {
      console.log("Queue position:", update.queue_position);
    } else {
      console.log(update.status, update.logs ?? []);
    }
  },
});
console.log(result.requestId, result.data.images[0].url);
```

Python; `interval`, `start_timeout` and `client_timeout` are seconds:

```python
import fal_client

def on_update(update):
    if isinstance(update, fal_client.Queued):
        print("Queue position:", update.position)
    elif isinstance(update, fal_client.InProgress):
        print("Running:", update.logs)
    elif isinstance(update, fal_client.Completed):
        print("Completed:", update.metrics)

result = fal_client.subscribe(
    "fal-ai/nano-banana-2",
    arguments={
        "system_prompt": "Use watercolor and muted blues.",
        "prompt": "A lighthouse above a rocky bay.",
        "num_images": 1,
    },
    headers={"X-Fal-Tags": "team=design,env=prod"},
    with_logs=True,
    interval=1.0,
    start_timeout=30,
    client_timeout=120,
    on_enqueue=lambda request_id: print("Save ID:", request_id),
    on_queue_update=on_update,
)
print(result["images"][0]["url"])
```

[Progress examples](https://fal.ai/docs/documentation/model-apis/inference/synchronous#with-progress-updates), [Python callback types](https://fal.ai/docs/api-reference/client-libraries/python/fal_client), [JavaScript queue types](https://fal.ai/docs/api-reference/client-libraries/javascript/queue)

## Progress and completion semantics

| State | Python object | JavaScript value | Useful data |
| - | - | - | - |
| Waiting | `Queued` | `IN_QUEUE` | `position` / `queue_position` |
| Running | `InProgress` | `IN_PROGRESS` | `logs` |
| Finished | `Completed` | `COMPLETED` | `logs`; optional `metrics` |

- Logs are model-dependent snapshots; repeated callbacks may include already observed entries.

- No universal percentage, ETA, partial-image stream or per-image completion signal is promised.

- `COMPLETED` also covers failed/cancelled requests; final result retrieval determines success.

- For actual model-output SSE, use `stream` only on a supported streaming endpoint.

[Status shapes](https://fal.ai/docs/api-reference/client-libraries/javascript/types.common), [HTTP status contract](https://fal.ai/docs/inference-api/queue/status), [model streaming](https://fal.ai/docs/documentation/model-apis/inference/streaming)

## Timeout and cancellation rules

| Trigger | Server effect |
| - | - |
| Start deadline | Prevents starting after deadline; does not cap an executing attempt |
| Python `client_timeout` | Stops waiting; also supplies `start_timeout` if it was omitted |
| JS polling `timeout` | Current SDK attempts queue cancellation; best effort |
| JS local abort | Stops local work/monitoring; not a server-stop guarantee |
| Explicit queue cancel | Removes queued work or signals running work |

```javascript
await fal.queue.cancel("fal-ai/nano-banana-2", { requestId });
```

```python
fal_client.cancel("fal-ai/nano-banana-2", request_id)
```

**Cost:** timeout, abort and cancel are not refund mechanisms. Recover the saved request ID before resubmitting. [Timeout behavior](https://fal.ai/docs/documentation/model-apis/inference/synchronous), [current JS cancellation implementation](https://raw.githubusercontent.com/fal-ai/fal-js/main/libs/client/src/queue.ts), [cancel API](https://fal.ai/docs/inference-api/queue/cancel)

# Generate one image

## Without a reference image

**Transport:** direct HTTP; response body = model JSON.

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

- Direct HTTP / `run`: bypasses durable queue.

- `subscribe`: submits, monitors, retrieves; multiple HTTP exchanges.

- `queue.submit`: returns job handle immediately.

[Synchronous inference](https://fal.ai/docs/documentation/model-apis/inference/synchronous), [HTTP API](https://fal.ai/docs/inference-api/sync/run)

## With a reference image

**Endpoint:** `fal-ai/nano-banana-2/edit`. **Body:**

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

| Reference control | Value |
| - | - |
| Field | `image_urls`: array, including for one reference |
| Capacity | Up to 14 advertised references |
| URL | Replace example with a reachable image |
| Prompt | Specify what to preserve/change |

[Edit API](https://fal.ai/models/fal-ai/nano-banana-2/edit/api), [capacity](https://fal.ai/models/fal-ai/nano-banana-2/edit)

# Multiple prompts through one application request

**Application request schema:** consumed by your backend; map to individual fal calls.

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

| Per-item step | Action |
| - | - |
| Select endpoint | References present → edit; otherwise → text to image |
| Build model body | Shared instruction + item prompt + item references |
| Persist identity | `item_id`, endpoint, fal `request_id` |
| Recovery | Retry/recover individual failed items |
| Success | Preserve completed results |

**Node.js handler helper:** per-item results; partial success supported. **App limit:** ten items (chosen here).

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

| Runtime condition | Handling |
| - | - |
| Concurrent submissions | Account/endpoint limits still apply |
| New-account running concurrency | Initially two |
| HTTP handler deadline | Persist batch; return 202 + application job ID |
| Background completion | Status endpoint or webhook |

[Queue SDK](https://fal.ai/docs/api-reference/client-libraries/javascript/queue), [queue](https://fal.ai/docs/documentation/model-apis/inference/queue), [concurrency](https://fal.ai/docs/documentation/model-apis/concurrency-limits)

- **Cost:** 3 items × 1 image = 3 generations.

- **Retention:** helper inherits account defaults; configure explicit headers when required.

# One generation HTTP request directly to fal

**Workflow graph:** one model node per item → collect item-ID/image-URL outputs. **Transport:** one SSE inference request; workflow sent inline.

**Precondition:** validate inputs as in the batch helper. **Graph construction:** performed by your application.

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

| Workflow detail | Contract |
| - | - |
| Output | Item ID → image URL |
| Definition | Inline graph + whole-field references |
| Upload/download | Additional HTTP operations |
| Independent nodes | Simultaneous scheduling not guaranteed |
| Stream timeout | Between chunks; SDK default 15s; example 120s |
| Total execution deadline | Not established by stream timeout |

[Graph tutorial](https://fal.ai/docs/examples/integrations/custom-workflow-ui), [streaming SDK](https://fal.ai/docs/api-reference/client-libraries/javascript/streaming)

| Workflow constraint | Consequence |
| - | - |
| Outer request | Direct SSE; not queue submission |
| Stream disconnect | Child execution may continue |
| Billing | Per model node; completed steps can cost even when workflow fails |
| JS stream arbitrary headers | Not exposed as on queue submission |
| Child control inheritance | Do not assume lifecycle/tag propagation |
| Strict per-item controls | Explicit queue calls; verify Workflow behavior with fal |

[Streaming](https://fal.ai/docs/documentation/model-apis/inference/streaming), [Workflow steps](https://fal.ai/docs/documentation/model-apis/workflows)

# Reference image transport

**Model body:** JSON. **Reference value:** direct URL or supported data URI.

**Local reference:** upload once → reuse returned URL.

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

| Input transport | Requirements / tradeoff |
| - | - |
| External/presigned URL | Direct file response; no extra headers/cookies; valid through queue + execution |
| Base64 data URI | No separate upload; larger JSON body |
| Reused reference | Reuse URL; avoid duplicate uploads |

[CDN](https://fal.ai/docs/documentation/model-apis/fal-cdn), [JS storage](https://fal.ai/docs/api-reference/client-libraries/javascript/storage), [Python](https://fal.ai/docs/api-reference/client-libraries/python/fal_client)

- **Input upload lifecycle/ACL:** configured when uploading.

- **Inference lifecycle:** applies to generated outputs.

- **Restricted references:** configure upload `initialAcl`.

[File ACLs](https://fal.ai/docs/documentation/model-apis/file-access-controls)

# Retrieve and save the generated image

| Interface | Image location |
| - | - |
| Direct HTTP or REST queue result | `images[i].url` |
| JavaScript SDK | `result.data.images[i].url` |
| Python SDK | `result["images"][i]["url"]` |
| Workflow above | Output field named after the item ID |

| Output field | Meaning |
| - | - |
| `images[].url` | Required image location |
| Width/height/file metadata | May be null/absent |
| `description` | Model text output |
| Response body | JSON; retrieve image bytes from URL |

[Output schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2)

**Public output download:** ordinary GET; no fal key needed.

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

- Private output → signed URL or CDN bearer token.

- Preserve image bytes in owned storage; saving only the URL does not preserve the file.

- Retain item ID + request ID with the file.

- Helper buffers one image; use streaming/size limits for larger downloads.

| `sync_mode: true` | Behavior |
| - | - |
| Image return | Data URI |
| Request history | Output data omitted |
| Transport selection | Independent of direct/queue |
| Generation cost | Still applies |
| Full zero retention | Not established |

[Model API](https://fal.ai/models/fal-ai/nano-banana-2/api)

# Queue operations and recovery

**REST submission:** POST model JSON to `https://queue.fal.run/{endpoint}`. **Persist:** `request_id`, `status_url`, `response_url`, `cancel_url`.

| Operation | HTTP request |
| - | - |
| Submit | POST generation endpoint |
| Check status | GET returned `status_url` |
| Fetch model output | GET returned `response_url` |
| Cancel | PUT returned `cancel_url` |

- Authenticate all queue operations.

- Follow returned URLs.

- Edit request lookups omit the `/edit` suffix; constructing `/edit/requests` can return 405.

[Queue protocol](https://fal.ai/docs/documentation/model-apis/inference/queue)

| Queue event | Interpretation |
| - | - |
| `IN_QUEUE` | Waiting |
| `IN_PROGRESS` | Executing |
| `COMPLETED` | Terminal; may be success, failure or cancellation |
| Result fetch | Returns original model success/error status |
| Client timeout | Recover known request before submitting another |

[Status](https://fal.ai/docs/inference-api/queue/status), [result](https://fal.ai/docs/inference-api/queue/result), [reliability](https://fal.ai/docs/documentation/model-apis/inference/reliability)

| Recovery | JavaScript |
| - | - |
| Status | `fal.queue.status(endpoint, { requestId })` |
| Result | `fal.queue.result(endpoint, { requestId })` |
| Background notification | Webhook; verify signature; deduplicate request IDs |

[Queue SDK](https://fal.ai/docs/api-reference/client-libraries/javascript/queue), [webhooks](https://fal.ai/docs/documentation/model-apis/inference/webhooks)

# Retention and deletion

## Three independent retention layers

| Stored item | Documented lifetime or default | Relevant control |
| - | - | - |
| Temporary queue result JSON | One hour after completion; six minutes when the result is 10 KB or larger | Retrieve promptly; payload storage can extend availability |
| Request history inputs and outputs | 90 days by default | `X-Fal-Store-IO` or account policy |
| Uploaded and generated CDN files | Account lifecycle; current settings reference uses no expiry and public access when unconfigured | Upload lifecycle, inference lifecycle header, account storage settings |

- Queue result lifetime concerns JSON size, not image-file size.

- Enabled payload storage extends result availability to its retention period.

- Disabling history does not immediately delete temporary queue results.

[Result retention](https://fal.ai/docs/inference-api/queue/result)

| Control | Scope |
| - | - |
| `X-Fal-Store-IO: 0` | Disables history input/output payload storage |
| CDN files | Separate lifecycle/deletion |
| Deployment logs | Separate policy |
| Explicit header | Overrides account default |
| Zero Data Retention | Requires fal-confirmed endpoint/storage-path coverage; not enabled by default |

[Retention](https://fal.ai/docs/documentation/model-apis/media-expiration)

## Expiration values and defaults

**Null semantics are interface-specific:** explicit JSON `null` ≠ omitted configuration.

| Interface | Value | Effect |
| - | - | - |
| Raw request lifecycle header | `{"expiration_duration_seconds": null}` | Explicitly disables automatic expiration |
| Raw request | Omit the lifecycle header | Uses account defaults |
| Account storage settings API | `expiration_duration_seconds: null` | Disables automatic expiration for new uploads |
| Python upload SDK | Omit `expires_in`, use its default `None`, or use `"never"` | Preserves the backend default retention policy |

**Raw request override — no automatic expiration:**

```http
X-Fal-Object-Lifecycle-Preference: {"expiration_duration_seconds":null}
```

**Python exception:** `expires_in="never"` preserves backend defaults; it is not the raw-header override. [Expiration](https://fal.ai/docs/documentation/model-apis/media-expiration), [header defaults](https://fal.ai/docs/documentation/model-apis/common-parameters), [account defaults](https://fal.ai/docs/platform-apis/v1/storage/settings/get), [Python StorageSettings](https://fal.ai/docs/api-reference/client-libraries/python/fal_client#storagesettings)

**Duration unit:** seconds. **Selection:** custom values supported; presets optional.

| Lifetime | `expiration_duration_seconds` |
| - | - |
| 10 minutes | `600` |
| 1 hour | `3600` |
| 1 day | `86400` |
| 7 days | `604800` |
| 30 days | `2592000` |
| No automatic expiration | `null` |

| Duration constraint | Value |
| - | - |
| Account schema | Integer ≥1 or `null` |
| Documented maximum | None; arbitrary-large acceptance not guaranteed |
| Python presets | `1h`, `1d`, `7d`, `30d`, `1y` |
| Python `"immediate"` | Approximately 60 seconds |
| `0` | Does not mean immediate deletion |

[Settings schema](https://fal.ai/docs/platform-apis/v1/storage/settings/update), [Python presets](https://fal.ai/docs/api-reference/client-libraries/python/fal_client#storagesettings)

**Independent controls:** file expiry, ACL, signed-URL expiry. The signed-URL seven-day maximum does not cap the file lifetime.

## Explicit output retention and access

**Example policy:** history payloads disabled; output ACL private; output expiry one hour.

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

- JS queue equivalent: same `headers`; serialize lifecycle with `JSON.stringify`.

- Place platform controls outside model `input` / `arguments`.

- Lifecycle applies when output files are created; existing input uploads keep their own policy.

- Private downloads require signed access or CDN authentication.

[Headers](https://fal.ai/docs/documentation/model-apis/common-parameters), [ACLs](https://fal.ai/docs/documentation/model-apis/file-access-controls)

## Account defaults

Read current defaults:

```sh
curl --fail-with-body 'https://api.fal.ai/v1/storage/settings' \
  -H "Authorization: Key $FAL_KEY"
```

| GET response / access | Contract |
| - | - |
| Fields | `expiration_duration_seconds`, `initial_acl` |
| No saved settings | Both null |
| Permission | `account:settings:read`; API preset |

[Get settings](https://fal.ai/docs/platform-apis/v1/storage/settings/get)

| PUT storage settings | Contract |
| - | - |
| URL | Same as GET |
| Body | Complete desired JSON; replaces configuration |
| Omitted/null fields | Cleared |
| Empty `{}` | Clears all settings |
| Cleared expiration | No automatic expiration |
| Partial change | GET first; include settings to retain |
| Permission | `account:settings:write`; FULL or custom key |
| Scope | New uploads |

[Update settings](https://fal.ai/docs/platform-apis/v1/storage/settings/update)

## Delete a completed request and its outputs

```sh
curl --fail-with-body -X DELETE \
  "https://api.fal.ai/v1/models/requests/$REQUEST_ID/payloads" \
  -H "Authorization: Key $FAL_KEY"
```

| Payload deletion | Contract |
| - | - |
| Removes | Request input/output JSON + associated output CDN files |
| Permissions | `models:requests:write` + `assets:write`; API preset insufficient |
| Request state | Completed; unfinished → 400 |
| Per-file failures | Check `cdn_delete_results[].exception`, including after HTTP 200 |
| `Idempotency-Key` | Caches deletion response for 10 minutes |
| Inference idempotency | Not established by deletion idempotency |

[Delete payloads](https://fal.ai/docs/platform-apis/v1/models/requests/payloads)

| Individual file deletion | Contract |
| - | - |
| Endpoint | `POST https://rest.fal.ai/storage/files/delete` |
| Body | `{"file_url":"<original CDN URL>"}` |
| Auth | Key; `assets:write` |
| Input files | Retained by request-payload deletion; may be reused |
| Reused uploads | Account for dependent requests before deleting |

[CDN deletion](https://fal.ai/docs/documentation/model-apis/fal-cdn)

**Cost:** expiry/deletion does not refund generation. Preserve needed bytes before deletion; regeneration costs again.

# Private images and signed URLs

| ACL default | Anonymous result |
| - | - |
| `allow` | 200 |
| `forbid` | 403 |
| `hide` | 404 |

- Owner retains access; rules can grant named users.

- Scope: fal CDN v3 files.

[ACL contract](https://fal.ai/docs/documentation/model-apis/file-access-controls)

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

| Signed URL | Contract |
| - | - |
| Permission | `assets:read` |
| Duration | 1–604800 seconds; maximum 7 days |
| Returned field | `signed_url` |
| Access | Bypasses file ACL; credential to protect |
| Use | Client display or ordinary download |
| Underlying file | Must still exist; signature cannot extend lifecycle |

[Sign URL](https://fal.ai/docs/platform-apis/v1/storage/files/sign)

| Authenticated server download | Operation |
| - | - |
| Exchange key | POST `https://rest.fal.ai/storage/auth/token?storage_type=fal-cdn-v3` |
| Exchange request | Key auth + JSON `{}` |
| Download | GET original media URL; `Authorization: Bearer <token>` |
| Refresh | Read `expires_at` |
| API key | Not a CDN download bearer token |

[CDN authentication](https://fal.ai/docs/documentation/model-apis/file-access-controls)

| ACL operation | Endpoint / permission |
| - | - |
| Read | GET `https://api.fal.ai/v1/storage/files/acl?url=...`; `assets:read` |
| Replace | PUT same path; `assets:write` |
| Verification | Inspect saved rules; nonexistent users are dropped |

[Get ACL](https://fal.ai/docs/platform-apis/v1/storage/files/acl/get), [replace ACL](https://fal.ai/docs/platform-apis/v1/storage/files/acl/set)

# Cost and settings for this use case

## Published Nano Banana 2 prices

**Public base rates:** generation and edit endpoints; checked 22 October 2026.

| Output resolution | Published multiplier | USD per generated image |
| - | - | - |
| `0.5K` | 0.75 | 0.06 |
| `1K` | 1.00 | 0.08 |
| `2K` | 1.50 | 0.12 |
| `4K` | 2.00 | 0.16 |

| Example | Base cost |
| - | - |
| 3 items × 1 image × 1K | USD 0.24 |
| 10 items × 1 image × 1K | USD 0.80 |
| One outer HTTP request | No per-image grouping discount |
| Account terms / optional extras | Can change total |

[Generation price](https://fal.ai/models/fal-ai/nano-banana-2), [edit price](https://fal.ai/models/fal-ai/nano-banana-2/edit)

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

- Search/thinking surcharge scaling within multi-image calls is not clearly specified.

- Examples disable search, omit thinking and request one image per item.

[Model pricing](https://fal.ai/models/fal-ai/nano-banana-2), [failure billing](https://fal.ai/docs/documentation/model-apis/faq)

## Model inputs

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

**Unbounded model-directed generation:** `limit_generations: false` enables multi-image instructions; no `prompts[]` mapping or billing cap. [Edit OpenAPI](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2/edit), [API behavior](https://fal.ai/models/fal-ai/nano-banana-2/edit/api)

## Query prices and estimate before generating

```sh
curl --fail-with-body --get 'https://api.fal.ai/v1/models/pricing' \
  -H "Authorization: Key $FAL_KEY" \
  --data-urlencode 'endpoint_id=fal-ai/nano-banana-2' \
  --data-urlencode 'endpoint_id=fal-ai/nano-banana-2/edit'
```

**Pricing response:** `prices[].unit_price`, `unit`, `currency`. **Adjustment:** endpoint resolution multipliers/extras still apply. [Pricing API](https://fal.ai/docs/platform-apis/v1/models/pricing)

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

| Estimate behavior | Meaning |
| - | - |
| `unit_quantity` | Endpoint billing units; not universally prompts/images |
| Resolution/extras | Include according to model pricing |
| Spending cap | Estimate does not reserve/enforce one |

[Estimate API](https://fal.ai/docs/platform-apis/v1/models/pricing/estimate)

**Reconciliation:** request ID → `cost_subtotal`, `cost_discount`, `cost_total`. **Key:** BILLING/FULL or custom billing permissions. [Billing events](https://fal.ai/docs/platform-apis/v1/models/billing-events)

| Billing condition | Rule |
| - | - |
| Funding | Prepaid credits |
| Purchased-credit expiry | 365 days |
| Promotional expiry | Varies |
| Model API cold starts / queue wait | No charge |
| Client timeout | Does not establish server completion/cancellation |
| Running cancellation | No documented refund guarantee |

[Pricing](https://fal.ai/docs/documentation/model-apis/pricing), [FAQ](https://fal.ai/docs/documentation/model-apis/faq), [cancel](https://fal.ai/docs/inference-api/queue/cancel)

| Storage cost | Published position |
| - | - |
| fal storage / CDN delivery | No separate fees for ordinary image-generation use |
| Retention-duration surcharge | None published |
| No automatic expiry | No permanent/unlimited-storage guarantee |
| Your storage / CDN | Provider charges can apply |

[Storage/CDN pricing statement](https://fal.ai/learn/tools/ai-image-generators)

# Documentation differences to account for

| Documentation mismatch | Implementation decision |
| - | - |
| FAQ: files available ≥7 days; current settings: no unconfigured auto-expiry | Set explicit lifetimes; preserve needed bytes |
| ACL guide: account defaults require support; current API: GET/PUT settings | Use API; verify saved values |

[FAQ](https://fal.ai/docs/documentation/model-apis/faq), [GET defaults](https://fal.ai/docs/platform-apis/v1/storage/settings/get), [PUT defaults](https://fal.ai/docs/platform-apis/v1/storage/settings/update), [ACL guide](https://fal.ai/docs/documentation/model-apis/file-access-controls)

**Queue-path mismatch:** generated OpenAPI can show `/edit/requests`; current queue protocol omits endpoint suffix. Follow returned `status_url` / `response_url`. [Edit schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=fal-ai/nano-banana-2/edit), [protocol](https://fal.ai/docs/documentation/model-apis/inference/queue)

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

- [fal storage and CDN pricing statement](https://fal.ai/learn/tools/ai-image-generators)

- [Full documentation index](https://fal.ai/docs/llms.txt)

## Additional parameter, model and reporting references

- [Retry policy and precedence](https://fal.ai/docs/documentation/serverless/reliability/retries)

- [parameter reference](https://fal.ai/docs/documentation/model-apis/common-parameters#fal_max_queue_length)

- [Response headers](https://fal.ai/docs/documentation/model-apis/common-parameters#response-headers)

- [Request Tags](https://fal.ai/docs/documentation/model-apis/request-tags)

- [Usage API](https://fal.ai/docs/platform-apis/v1/models/usage)

- [FOCUS export](https://fal.ai/docs/platform-apis/v1/account/focus)

- [Workflow step request IDs](https://fal.ai/docs/documentation/model-apis/workflows#step-request-ids)

- [JavaScript client](https://fal.ai/docs/api-reference/client-libraries/javascript/client)

- [RunOptions](https://fal.ai/docs/api-reference/client-libraries/javascript/types.common#runoptions)

- [Progress examples](https://fal.ai/docs/documentation/model-apis/inference/synchronous#with-progress-updates)

- [Status shapes](https://fal.ai/docs/api-reference/client-libraries/javascript/types.common)

- [current JS cancellation implementation](https://raw.githubusercontent.com/fal-ai/fal-js/main/libs/client/src/queue.ts)

- [Nano 2.1](https://fal.ai/models/google/nano-banana-2.1/api)

- [Nano 2.1 edit](https://fal.ai/models/google/nano-banana-2.1/edit/api)

- [Nano Pro edit](https://fal.ai/models/fal-ai/nano-banana-pro/edit/api)

- [GPT 2.5 Flare](https://fal.ai/models/openai/gpt-image-2.5/flare/edit/api)

- [GPT 2.5 Sunburst](https://fal.ai/models/openai/gpt-image-2.5/sunburst/edit/api)

- [schnell](https://fal.ai/models/fal-ai/flux/schnell/api)

- [klein 4B](https://fal.ai/models/fal-ai/flux-2/klein/4b/api)

- [klein 9B](https://fal.ai/models/fal-ai/flux-2/klein/9b/api)

- [Turbo](https://fal.ai/models/fal-ai/flux-2/turbo/api)

- [Google reference limits](https://fal.ai/nano-banana-2.1)

- [Pro](https://fal.ai/nano-banana-pro)

- [GPT schema](https://fal.ai/api/openapi/queue/openapi.json?endpoint_id=openai/gpt-image-2.5/flare/edit)

- [klein edit](https://fal.ai/models/fal-ai/flux-2/klein/4b/edit/api)

- [Turbo edit](https://fal.ai/models/fal-ai/flux-2/turbo/edit/api)

- [Nano 2.1 token rates/examples](https://fal.ai/models/google/nano-banana-2.1)

- [GPT 2.5 breakdown](https://fal.ai/models/openai/gpt-image-2.5/flare/edit)

- [schnell](https://fal.ai/models/fal-ai/flux/schnell)

- [klein 4B generation](https://fal.ai/models/fal-ai/flux-2/klein/4b)

- [4B edit](https://fal.ai/models/fal-ai/flux-2/klein/4b/edit)

- [klein 9B generation](https://fal.ai/models/fal-ai/flux-2/klein/9b)

- [9B edit](https://fal.ai/models/fal-ai/flux-2/klein/9b/edit)

- [Turbo generation](https://fal.ai/models/fal-ai/flux-2/turbo)

- [Turbo edit](https://fal.ai/models/fal-ai/flux-2/turbo/edit)

- [Dev price/API](https://fal.ai/models/fal-ai/flux-2/api)

- [Dev price](https://fal.ai/models/fal-ai/flux-2)

- [Pro schema](https://fal.ai/models/fal-ai/flux-2-pro/api)

- [Pro price/MP rules](https://fal.ai/models/fal-ai/flux-2-pro)

- [Seedream 5 Lite schema](https://fal.ai/models/fal-ai/bytedance/seedream/v5/lite/text-to-image/api)

- [5 Lite price](https://fal.ai/models/fal-ai/bytedance/seedream/v5/lite/text-to-image)

- [4.5 schema](https://fal.ai/models/fal-ai/bytedance/seedream/v4.5/text-to-image/api)

- [4.5 price](https://fal.ai/models/fal-ai/bytedance/seedream/v4.5/text-to-image)

- [Model discovery API](https://fal.ai/docs/platform-apis/v1/models)

- [Version differences](https://fal.ai/learn/devs/nano-banana-2-1-vs-nano-banana-2)

- [Common size presets](https://fal.ai/docs/documentation/model-apis/model-arguments)

- [Dimensions](https://fal.ai/gpt-image-2.5)

- [JavaScript HTTP retry policy](https://fal.ai/docs/api-reference/client-libraries/javascript/retry)

- [Current JS subscribe implementation](https://raw.githubusercontent.com/fal-ai/fal-js/main/libs/client/src/client.ts)

- [API key dashboard](https://fal.ai/dashboard/keys)

- [Python StorageSettings](https://fal.ai/docs/api-reference/client-libraries/python/fal_client#storagesettings)
