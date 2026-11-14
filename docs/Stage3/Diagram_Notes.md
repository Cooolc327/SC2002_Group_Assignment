# Stage 3 diagram notes and provenance

Implementation: 3f5e682f775734a6a305fc159abd89aaf76257c3.
Stage 2 baseline: stage-2 / ad6f39ba20102ef3fa20778cb19e70ab3cb69c21.
Export and visual-check date: 13 November 2026 (UTC+8).

## Full class diagram

45 classifiers, 65 relationships. The complete current principal classes,
interfaces, records and relevant enums are shown. Blue headers identify Stage 3
additions/modifications. Public, private and protected visibility are marked;
static fields/operations are underlined. Solid hollow triangles are inheritance;
dashed hollow triangles are realization. Filled diamonds are composition;
hollow diamonds are aggregation. Plain associations include role names and
multiplicities where relevant. Only key members are shown; association roles
may represent reference-valued fields.

Menu owns three base items and three combos. Each ComboMeal refers to two
shared menu items, so its component edge is an association, not exclusive
composition. It inherits the MenuItem contract used by Order. StaffTeam owns
four independent staff objects. Restaurant owns HappyHourPolicy; activation
state is not duplicated in the CLI. Immutable snapshots expose Host options
and policy state, with copied lists and record accessors.

## Complete sequence

96 messages, six alt/loop fragments. The normal replay activates Happy Hour on
turn 4; the alternative leaves it unused with identical service priorities.
VIP #2 arrives, is seated by Host and orders Burger on turn 4. Chef completes
its one preparation unit that turn. Waiter serves on turn 5; eating completes
on turn 6; payment occurs on turn 7 after policy expiration. Order bill and
satisfaction at payment are 1080 cents / 100 in the normal branch and
1350 cents / 95 in the alternative. The remaining-turn loop reaches turn 18.

The normal whole-game result is five paid, $83.70, average 87.20. The alternative
is five paid, $86.40, average 86.20. Both win. Stage 2's stored victory ($64.50,
80.40) has different stage-specific service/menu behaviour and is not treated
as a controlled single-feature comparison.

Method-reference objects are explicit: IntFunction binds CustomerFactory's
createCustomer, while OrderPort.Factory binds Order::new. CustomerFactory is
not drawn as implementing IntFunction. Constructor messages start the VIP and
Order lifelines at their creation rows. Solid calls, dashed returns, guards,
loops and ref frames distinguish actual messages from abbreviated repeated
subinteractions. Recovery is called for registered customers; the Customer
method itself guards WAITING/SEATED/ORDERED eligibility.

## Files and checks

See [the UML index](../UML/README.md) for PDF, PNG, SVG and native drawio files.
Class and sequence layout/event JSON files make the exported model inspectable.
[Source SHA-256 provenance](../UML/stage3-diagram-source-identity.json) records
all production Java files. The optional generator verifies the implementation's
source/script/test identity before rendering. Existing Java gameplay is not
changed by this documentation update; its 76 unique scenarios / 1649 assertions
remain the implementation verification baseline.

All PDF pages and final image exports were visually inspected. PDF fonts are
embedded. Class boxes and annotation labels are checked for bounds/overlap by
the generator. XML nodes/edges, PDF text, replay values and file links are
validated separately. PNGs are independent display images; drawio files retain
editable shapes, labels and arrows. The compact Mermaid files remain earlier
discussion views; the full rendered model adds auxiliary types and messages.

The group still integrates these figures into its final report, verifies
member contributions, records its video and signs declarations. Documentation
exports do not constitute independent group acceptance or publish stage-3.
