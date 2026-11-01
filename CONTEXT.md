# PlanIt, Domain Glossary

## Core Entities

| Term | Definition |
|------|------------|
| **User** | Registered person. Has email, password, role. Can create plans. |
| **Place** | A location in Buenos Aires. Has name, description, category, address, coordinates, image, and a typical visit duration (`defaultDurationMinutes`, editable in the place admin form). |
| **Plan** | A curated collection of places with schedule. Owned by a User. Is public or private. Its schedule is a daily window: Day 1 date, start time, day end time and transport mode (`WALK`/`BIKE`/`BUS`/`CAR`). |
| **PlanPlace** | A place within a plan, with visit date, time, and order in the itinerary. Order is the source of truth: visit date/time are derived from it (stop duration, else the place's default duration, else its category default, plus the leg to the next stop) and re-derived whenever the itinerary changes, unless the User pins them by hand. |
| **Plan participant** | Registered User who joins a public Plan using its Short Code, or a private Plan after an accepted join request or invitation. Can read its detail and leave it; cannot edit or delete it unless assigned the Editor role. The plan detail shows their full name, role and profile link, never their email as visible text. The administrator owns the Plan and can delete it. |
| **Rating** | A 1–5 star score a logged-in User gives to a public Plan. One per User per Plan (updatable). Authorship is stored but never shown: only the average and count are public. |
| **Seguidor / Seguido** | Directed follow relation between two Users: one `follows` row per (follower, followed) pair. It grants no extra visibility — only the followed user's public plans reach the "Siguiendo" feed scope — and it sends no notifications. |

**Editor**: A Plan participant who can modify the plan and itinerary and invite people,
including proposing the Editor role. Only the administrator (owner) can change the roles
of existing participants or delete the Plan. Leaving the Plan removes the Editor role.

The owner and Editors can remove Participants from a Plan. Only the owner can remove
Editors. The owner cannot be removed. Removal revokes participation and the Editor
role for that Plan without deleting the User's account. Expulsion allows joining again.
A ban persists for that Plan and prevents joining by Short Code or invitation.
The same removal permissions apply to bans.

A banned User cannot access the Plan detail, public view or itinerary, clone the
Plan, or regain participation through its Short Code or an invitation. The ban
overrides public visibility for that authenticated User. My Plans displays a
persistent, collapsed sidebar notice below invitations with only the Plan name
and the access restriction; discovery
feeds exclude Plans that banned the current User. Anonymous visitors can still
read public Plans.

The owner and Editors can inspect the Plan's banned Users and lift any ban.
Lifting a ban does not restore participation or the Editor role. The User can
join again using the Short Code or a new invitation. The banned Users list is
only exposed to the owner and Editors, in the "Baneados" block of the unified
Plan configuration section in Plan detail.

**PlanManager**: Embedded component of a Plan that groups its Participants, Editors
and pending Invitations and Join Requests. It preserves the existing persistence tables and the Plan's
public methods for accessing these collections.

**Plan join request**: A registered User's pending request to participate in a private
Plan through its shared link or Short Code. It grants no access until the owner, or an Editor when
authorized, accepts it. Rejection removes the request without granting participation.
The visitor sees only the name, description, date, place count and generic cover.

**Plan settings**: Owner-managed permissions for inviting internally, inviting by email
and handling join requests, edited in the `Roles y permisos` block of the unified Plan
configuration section in Plan detail. Internal invitations default to Editors; other
capabilities default to Owner. Inviting can extend to all Participants; handling requests
can extend only to Editors. There is no in-app share-link control: Plan visibility governs
access. Only owners and Editors can propose the Editor role. Bans also prevent requesting
or accepting access to a private Plan.

**Schedule derivation**: visit date/time stay persisted in `visitDate`/`visitTime` even when
they are derived, so the public view, the plan listing and cloning read them unchanged.
`timePinned` marks what the user fixed by hand and breaks the chain of derivation for the
stops that follow, and the Plan setting `autoSchedule` (default on) turns the automatic
rescheduling off altogether. When the itinerary runs past the day end time it
continues on the next day, with no limit on the number of days.

**Plan configuration**: The single `#configuracion` section in Plan detail groups basic
data, role permissions and bans; join requests sit with participants, between the plan
summary and the invite form. `/plans/{id}/settings` is a
compatibility redirect to that section. The `/explore` wizard step 3 uses the same
permission selectors and defaults as the detail.

## Categories

| Term | Definition |
|------|------------|
| **RESTAURANT** | Dining establishment |
| **BAR** | Bar or pub |
| **CAFE** | Coffee shop |
| **MUSEUM** | Museum or gallery |
| **PARK** | Green space or plaza |
| **SHOPPING** | Shopping center or market |
| **NIGHTLIFE** | Club or late-night venue |
| **CULTURE** | Theater, cinema, cultural center |
| **SPORT** | Sports venue or activity |
| **OTHER** | Uncategorized |

## Visibility

Whether a plan can be read through its shared link. A boolean on the Plan, not a pair of states.

| Term | Definition |
|------|------------|
| **Public plan** | `isPublic` is true: the plan is accessible via shared URL without login |
| **Private plan** | `isPublic` is false: the plan is visible only to its owner and its participants |

## Bookmarks

A **Bookmark** (UI: "Guardados") is a private save of a public Plan that a registered User keeps
to clone later. It belongs to the saver, never to the Plan's owner, and it is not a way to access
a plan: removing it changes nothing about the Plan.

| Term | Definition |
|------|------------|
| **Bookmark** | A User's private save of a public Plan. Only public plans can be bookmarked; only the owner of the bookmark lists it, from the "Guardados" section of their own profile. |
| **Bookmark snapshot** | Copy of the plan's display data (name, description, date, place count, author, place references) taken when the bookmark is created. It is the fallback the card and the clone use once the live plan is no longer accessible. |
| **Bookmark status** | Computed per viewer on each read: `AVAILABLE` (the live plan is still reachable, with the live data), `PRIVATE` (the plan became private and the viewer is neither owner nor participant) or `DELETED` (the plan no longer exists). Unavailable states keep the card visible with a badge and a tooltip, and can still be cloned. |
| **Clone from bookmark** | Creates a private plan owned by the bookmark's user. With a live plan it reuses the clone flow; otherwise it rebuilds the plan from the snapshot, skipping places that no longer exist. When the live plan is reachable, its current data wins over the snapshot. |

## Key Concepts

**User calendar**: Monthly, read-only view at `/plans/calendar` of the User's owned
Plans and Plans they participate in. Entries use the Plan's start date and time,
link to its detail, and exclude pending invitations. Undated Plans appear separately.
See `docs/calendar-prototype.md` for the initial scope.

**Plan invitation**: A pending invitation sent by a registered User to another registered
User for a Plan, with the proposed Participant or Editor role and the inviter's identity.
Only the recipient can accept or reject it from My Plans. Email invitations also carry
a single-use link: GET opens confirmation after login, POST accepts with CSRF protection. Sending an invitation does not
grant participation or access to a private Plan. Acceptance grants the proposed role;
rejection leaves participation unchanged. Both responses remove the pending invitation.

| Term | Definition |
|------|------------|
| **Short Code** | Unique URL-friendly identifier for sharing plans, 8 characters (e.g., `a1b2c3d4`) |
| **Itinerary** | Ordered list of places in a plan, with dates and times derived from that order and grouped by day in the plan detail |
| **Feed** | The `/for-you` list of public plans a visitor discovers, in two scopes: "Para vos" (default) and "Siguiendo" (public plans of the authors the viewer follows). Never includes the viewer's own plans |
| **Popularity** | Feed ordering from ratings and participants (Bayesian score, participants/date/name/id as tiebreaks). Computed, never shown; the card still shows only average and count |
| **Marker** | Visual pin on the map representing a place |
| **Popup** | Info window that opens when clicking a marker |
