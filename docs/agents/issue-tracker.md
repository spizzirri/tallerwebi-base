# Issue tracker: Trello

Issues and specs for this repo live on Trello. Use the Trello MCP tools for all operations.

## Conventions

- **Read a card**: `trelloReadCard(action: "get", cardIdOrUrl: "...")` — accepts an ARI or a Trello
  card URL (convert URLs to ARIs before write actions: writes want the `id` returned by reads).
- **List cards**: `trelloReadCard` with `action: "list_by_board"` (grouped by list) or
  `list_by_list` (one list).
- **Create a card**: `trelloWriteCard(action: "create", listId: "...", name: "...")` — `desc`,
  `due` and `pos` optional.
- **Comment on a card**: `trelloWriteCard(action: "add_comment", cardId: "...", text: "...")`.
- **Checklists**: `trelloWriteChecklist` (`create` / `add_item` / `update_item`); read them back
  with `trelloReadChecklist(action: "list_by_card", cardId)` — items carry their checked state.
- **Labels**: use Trello labels to track triage state and card status.

## Board structure

- Board: **PlanIt - TP TW1** — https://trello.com/b/znkiwK1B/planit-tp-tw1
- Cards map 1:1 to the specs in `docs/specs/`: the spec's key is the card's tag
  (`16-CAL.md` -> `[CAL] Calificaciones de Planes Públicos`).
- Lists are a lifecycle, walked in order:
  `BACKLOG` -> `READY FOR DEV` -> `IN PROGRESS` -> `CODE REVIEW / TEST` -> `DONE` -> `CLOSED`.
  - `CODE REVIEW / TEST`: branch pushed to origin, gate green, hand-off comment on the card.
  - `DONE`: approved in review, merge pending.
  - `CLOSED`: merged into `main`. Cards keep their checklists there permanently — that is how
    past cards stay auditable.

## Card anatomy

The spec is the source of truth (ACs, test scenarios); the card tracks the state of the work with
two checklists, always in this order.

### Checklist 1 — "Implementacion"

- Items named `[CAL-001] short task description`; numbering continues the highest existing one.
- Names stay short — the spec describes the feature, the checklist describes the tasks, ordered as
  they landed. Each item implemented in a commit of this repo references that commit by hash in the
  name (`... (commit 2eb5ad3)`), so once merged the checklist doubles as the card's changelog.
- Items are ticked as each commit lands and its verification passes. Fully ticked == work done.

### Checklist 2 — "Tests"

Items named `[T-CAL-001] layer: class (spec ids)`, one per test layer, keyed to the scenarios in
the spec's "Escenarios de Test" section:

- `Unit`: one item per test class, with its `U-XX` range;
- `Integration`: its `I-XX` range, plus the UI/state test group when the run covers one;
- `Security`: its `S-XX` range;
- `E2E`: its `E-XX` flow;
- the last item is always the full gate: `pwsh -File scripts/gate.ps1 all` into green, with the
  counts written into the item (`490 unit+integration, 40/40 E2E`).

Items are ticked as the matching suite passes. Reviewer's rule of thumb: all green == done and
tested; what each `U-`/`I-`/`S-`/`E-` id covers is answered by the spec, never re-defined here.

### Status comments

At milestones (feature complete, hand-off to review) post one comment on the card activity:
branch, gate results, how to run it, pending steps. The same channel carries one-time local
actions a pull demands from the rest of the team (a schema reset, an `.env` flag), always with the
exact command to run — as on the `[CAL]` card of 2026-10-17, the `reset-db` note after the
`places.description` re-mapping.

### Live example

`[CAL] Calificaciones de Planes Públicos` (https://trello.com/c/37QoApLq) carries the full
pattern: both checklists fully ticked (13 implementation items, one hash per commit, plus 8
test-layer items closed by the gate counts) and its status and migration comments in the activity.

## When a skill says "publish to the issue tracker"

Create a Trello card in the appropriate list.

## When a skill says "fetch the relevant ticket"

Use Trello MCP tools to read the card by ID or search by name.

## PRs as a request surface

**No.** This project does not treat external PRs as a triage surface.

## Wayfinding operations

Used by `/wayfinder`. Adapted for Trello:

- **Map**: a single card labelled `wayfinder:map`, holding the Notes / Decisions-so-far / Fog body.
- **Child ticket**: a card linked to the map. Labels: `wayfinder:<type>` (`research`/`prototype`/`grilling`/`task`). Once claimed, the ticket is assigned to the driving dev.
- **Blocking**: Trello's due dates or custom fields, or a `Blocked by:` line in the card description.
- **Frontier**: open, unblocked, unclaimed child cards; first in map order wins.
- **Claim**: assign the card to yourself.
- **Resolve**: add a comment with the answer, then move the card to the Done list.
