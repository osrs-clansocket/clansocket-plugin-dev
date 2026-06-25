# ClanSocket
[![](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/rank/plugin/clansocket)](https://runelite.net/plugin-hub/show/clansocket)
[![](https://img.shields.io/endpoint?url=https://api.runelite.net/pluginhub/shields/installs/plugin/clansocket)](https://runelite.net/plugin-hub/show/clansocket)

Streams real-time OSRS clan and gameplay telemetry over a single WebSocket to a clan dashboard at [clansocket.com](https://clansocket.com). Live world map, member roster, per-stream privacy gates, GDPR-grade data rights.

The plugin targets whichever in-game clan you are currently a member of. Telemetry only reaches a dashboard if your clan has been claimed there; for unclaimed clans the server drops events silently.

---

## Repository & Role

GitHub repository: [`osrs-clansocket/clansocket-plugin-dev`](https://github.com/osrs-clansocket/clansocket-plugin-dev). This is the **development** repository for the RuneLite client plugin; the Plugin Hub listing is built from a generated submission snapshot in a separate repository.

It is a standalone Git repository that checks out as a flat sibling under the `clansocket-workspace` umbrella. Unlike the Node packages, it builds with Gradle and is not driven by the workspace-root `npm` orchestrator.

The plugin is the data producer for the ClanSocket system. It connects to one endpoint — `wss://ws.clansocket.com/data` (the constant `SERVER_URL` in `src/main/java/com/clansocket/ClanSocketConstants.java`) — and the server resolves the clan tenant from the in-game clan name in the identity frame. Telemetry and clan chat ride the same connection. The companion server, dashboard, and Discord bot live in the sibling `clansocket-app` repository (`clansocket-app-dev`).

---

## Setup

1. Install **ClanSocket** from the Plugin Hub (`Configure` → `Plugin Hub` → search "ClanSocket").
2. Enable the plugin.
3. Log into RuneScape on a character that is a member of your clan.

If your clan is not yet claimed, ask your Owner or Deputy Owner to claim it from clansocket.com — see the [Claim your clan](https://github.com/osrs-clansocket/clansocket-plugin/wiki/Claim-Your-Clan) wiki page for the full walkthrough.

---

## Side Panel

The plugin adds a side panel to the RuneLite right-rail. The panel is the primary UI for everything except the WebSocket endpoint (which lives in the RuneLite Configuration tab).

![panel](https://github.com/osrs-clansocket/clansocket-plugin/wiki/screenshots/plugin-active.png)

- **Header** — connection state (Online / Offline / Reconnect), clan status (`clan: name` green when registered, `(unclaimed)` orange if not), and a reset button for the per-account event counters.
- **Presets** — Manual or Clan mode toggle, plus 7 save slots.
- **Telemetry cards** — 24 cards, one per data stream. Click to toggle off; click again to re-enable. Frequent streams render a 60-second sparkline; infrequent streams show a `Last: Nm ago` indicator.
- **Footer** — links to clansocket.com and the GitHub repo.

When the dashboard sends a consent request, a green-pulsing banner appears above the cards with confirm + reject buttons and a countdown.

![consent banner](https://github.com/osrs-clansocket/clansocket-plugin/wiki/screenshots/ingame-panel-notif-clan-claim.png)

---

## Configuration

`Configuration → ClanSocket → Network customization` (collapsed by default) — one field, in the RuneLite Configuration tab:

| Setting | Effect |
| --- | --- |
| WebSocket URL | `wss://` URL of your clan's dashboard endpoint. Blank uses clansocket.com. A bare host (no scheme) is auto-prefixed with `wss://`. |

All other settings live in the side panel.

---

## What It Streams

When the plugin is enabled and you are logged in to a clan-member character, events flow over a single WebSocket connection as per-tick batches.

**Game telemetry (your character only).** Identity (RSN, account hash, account type, world, world types, current activity, clan name + rank + join date + member counts, session start); clan roster snapshots; skills snapshot + XP gains + level-ups; combat (hitsplats dealt and taken, current target); player deaths; slayer state; vitals (run energy, weight, special attack); active prayers; stat boosts; status effects (poison, venom, disease, cold); location (x, y, plane, region, area); inventory + equipment + seed vault (baseline snapshot per session, then per-change deltas); bank open/close snapshots; rune pouch slot contents; loot drops; pet drops; quest snapshots and completions; diary snapshots and completions; clue scroll opens and completions; collection log per-item notifications and full snapshots; combat achievement catalog and per-task completions; farming patch changes; menu actions on game objects and NPCs.

**Clan chat.** Every message and Jagex-generated broadcast visible in the configured clan channel — same pattern as the long-running [clan-chat-webhook](https://github.com/pascalla/clan-chat-webhook) plugin. Scoped strictly to your configured clan; never any other channel.

## What It Does NOT Stream

- Public chat, private messages, friends-list chat, guest chat, or any clan channel other than the configured one.
- Other players' character data (only their messages in your clan channel, and Jagex's broadcasts about them).
- Any data when you are not logged in to RuneScape.
- Any data when your in-game clan has not been claimed on the dashboard.

---

## Privacy

Three layers of control:

- **24 per-stream toggles** in the side panel — click any card to disable a stream.
- **Explicit consent** dialogs on Location re-enable and on the Manual → Clan mode switch.
- **Data rights** on clansocket.com — browse every stored row, export a GDPR-style portable zip, or remove all data tied to your account.

Location streams real-time coordinates (x, y, plane, region, area) to your clan dashboard while you are logged in, visible to clan members only. The explicit YES/NO consent dialog fires when you toggle Location from OFF back to ON, so you see it any time you re-enable after disabling. On a fresh install no dialog appears; Location streams from the moment you log in. To gate this before any coordinates leave your client, toggle Location off from the panel right after install and re-enable when you are ready.

For the full privacy model and the data-rights surface, see [Privacy overview](https://github.com/osrs-clansocket/clansocket-plugin/wiki/Privacy-Overview) and [Data rights](https://github.com/osrs-clansocket/clansocket-plugin/wiki/Data-Rights) in the wiki.

---

# Developer Guide

## Building & Running

| Tool | Version |
| --- | --- |
| JDK | 11 (`options.release = 11`) |
| Gradle | Use the bundled wrapper (`./gradlew`). |
| RuneLite client | `latest.release` from `repo.runelite.net` (compile-only). |
| Lombok | 1.18.30 (compile-only + annotation processor). |

The Gradle tasks defined in `build.gradle`:

| Command | What it does |
| --- | --- |
| `./gradlew run` | Launches a RuneLite developer client with the plugin loaded. Main class `com.clansocket.ClanSocketPluginTest`, run with `-ea --developer-mode --debug`. |
| `./gradlew build` | Standard Java build (compile + test + assemble). |
| `./gradlew test` | Runs the JUnit test suite. |
| `./gradlew shadowJar` | Builds a multi-release fat jar (`clansocket-<version>-all.jar`) from main + test output and the test runtime classpath. |

The plugin entry class is `com.clansocket.ClanSocketPlugin` (declared in `runelite-plugin.properties` and annotated `@PluginDescriptor(name = "ClanSocket")`). The JVM that runs the plugin client must trust the dev server's mkcert root CA — the sibling `clansocket-app` repository provides `npm run trust:jvm` for this.

---

## Layout

```
clansocket-plugin/
├── build.gradle                build config: run, build, test, shadowJar tasks
├── settings.gradle             rootProject.name = 'clansocket'
├── runelite-plugin.properties  Plugin Hub manifest (displayName, plugins entry, version)
├── buildSrc/                   javaparser-based custom Gradle task types (quality toolchain)
│   └── src/main/java/com/clansocket/tools/
├── config/                     checkstyle, pmd, spotless, duplication exclusions
├── scripts/                    deploy.mjs + sync-to-submission.mjs
└── src/
    ├── main/java/com/clansocket/
    │   ├── ClanSocketPlugin.java       @PluginDescriptor entry point
    │   ├── CSTrackerRegistry.java      central registry of trackers
    │   ├── ClanSocketConstants.java    SERVER_URL and other constants
    │   ├── bus/                        tracker base classes + primitive collections + batching queue
    │   ├── chat/                       clan-chat capture
    │   ├── config/                     plugin config + presets
    │   ├── panel/                      side-panel UI (sections, widgets, swingfactory, verify)
    │   ├── protocol/                   payload DTOs, one package per domain
    │   ├── tracking/                   per-domain trackers (combat, xp, loot, farming, ...)
    │   ├── transport/                  WebSocket opener, reconnect, session store, consent
    │   ├── util/                       small pure helpers (Strings, Money, NumberInput)
    │   └── world/                      world constants
    └── test/java/com/clansocket/
        └── ClanSocketPluginTest.java   developer-client launcher + test entry
```

---

## Architecture & Key Concepts

- **Trackers.** Each game domain has a tracker under `tracking/` extending a base in `bus/` (`AbstractTracker`, `AbstractStateTracker`, `AbstractWarmupSnapshotTracker`). `CSTrackerRegistry` registers them centrally. Trackers subscribe to RuneLite events, gate on per-stream config, and enqueue payloads — they never block inside a `@Subscribe` handler.
- **Protocol DTOs.** Every payload type is a DTO under `protocol/<domain>/`. These fields mirror the server's `client-telemetry.ts` types; drift between the two is a build-time error in the server-side schema-contract check. The batch envelope is `{ seq, tick, events: [...] }`; the server stamps `event_received_at` on ingest.
- **Transport.** `transport/` opens and maintains the single WebSocket (`WsOpener`, `ReconnectScheduler`, `SessionStore`). There is exactly one endpoint — never per-tenant ingest URLs. `consent/` handles dashboard-initiated consent requests.
- **Panel.** `panel/` builds the side panel from RuneLite's bundled Swing components. `PluginPanel` subclasses are injected via `Provider.get()` inside `startUp()`, never constructor-injected.
- **Bus primitives.** `bus/primitive/` holds allocation-conscious primitives (`IntIntMap`, `IntObjectMap`, `WarmupCounter`, latched snapshot trackers) used on the hot per-tick path.

For RuneLite API lookups, read the local client source at `A:\Varietyz\runelite-client` rather than fetching documentation.

---

## Code Quality & Conventions

The repository carries a custom Java code-quality toolchain. The configuration lives in `config/` — Checkstyle (`config/checkstyle/checkstyle.xml`), a PMD ruleset (`config/pmd/ruleset.xml`), a Spotless Eclipse formatter (`config/spotless/eclipse-formatter.xml`), and duplication exclusions (`config/duplication/signature-exclusions.txt`). The custom enforcement task types live under `buildSrc/src/main/java/com/clansocket/tools/`:

| Task class | Concern |
| --- | --- |
| `DuplicationCheckTask` | Signature-shape duplication detection. |
| `UnusedCodeCheckTask` | Cross-class dead-code detection. |
| `BlockingCallCheckTask` | Bans blocking I/O reachable from `@Subscribe` handlers. |
| `RuneliteRulesCheckTask` | RuneLite Plugin Hub submission-ruleset checks. |
| `PrivacyTraceScannerTask` | Traces every protocol field back to a classified data source. |
| `StripCommentsTask` | Removes comments for submission. |
| `SubmissionReadinessCheckTask` | Verifies submission hygiene (test and config stripping). |
| `AddBracesTask` / `AddFinalsTask` / `DupMerger` | Source normalization helpers. |

The binding conventions: reuse RuneLite-bundled components (`FontManager`, `ColorScheme`, `DynamicGridLayout`) instead of re-implementing UI; no `System.out` or `printStackTrace()` (use the RuneLite logger); no blocking I/O on event handlers; extract any literal used twice into a `*Constants.java`. The canonical quality and testing doctrine lives at `../clansocket-docs/PLUGIN/PLUGIN-QUALITY.md` and `../clansocket-docs/PLUGIN/PLUGIN-TESTING.md`.

**ToS and payload constraints.** Every payload's subject is the installing player's own data; observed third-party character data is never streamed (NPC `id` + `name` and clan-chat sender names are the allowed exceptions). The recipient is user-configured — no open public endpoints. No gameplay-unfairness features. Read `../clansocket-docs/PLUGIN/RUNELITE/RUNELITE-PLUGIN-GUIDELINES.md` before adding telemetry.

---

## Safety & Compliance

All plugins successfully merged into the [Plugin Hub](https://github.com/runelite/plugin-hub) are reviewed and verified by the RuneLite development team, ensuring they are safe to use. For more information, see the [Plugin Hub readme](https://github.com/runelite/plugin-hub#Reviewing).

Furthermore, [RuneLite itself has been confirmed as fully compliant by Jagex](https://secure.runescape.com/m=news/a=13/another-message-about-unofficial-clients?oldschool=1).

---

## Creator Tag
![Smoke](https://i.ibb.co/PTYfzqB/Rune-LITE-By-Smoke.png)

For additional support or questions, reach out via [Discord](https://discord.gg/RQ9H9naf7E).
