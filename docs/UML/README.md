# UML diagrams

## Stage 3 rendered design evidence

The full class diagram covers 45 classifiers and 65 relationships. Blue headers
identify Stage 3 additions/changes compared with the stage-2 tag. Key attributes,
operations, visibility, static members, inheritance, realization and relevant
multiplicities are shown. Principal entity/control/boundary classes and related
records/enums/interfaces are included.

| Diagram | PDF | Image | Vector | Editable nodes and arrows |
| --- | --- | --- | --- | --- |
| Full Stage 3 class diagram | [PDF](Stage3_UML_Diagram.pdf) | [PNG](Stage3_UML_Diagram.png) | [SVG](Stage3_UML_Diagram.svg) | [drawio](Stage3_UML_Diagram.drawio) |
| Happy Hour complete scenario | [PDF](Stage3_Happy_Hour_Sequence.pdf) | [PNG](Stage3_Happy_Hour_Sequence.png) | [SVG](Stage3_Happy_Hour_Sequence.svg) | [drawio](Stage3_Happy_Hour_Sequence.drawio) |

The sequence contains 96 messages and six combined fragments, following VIP #2
from turn-4 arrival, Host seating and order to READY, turn-5 serving, turn-6
eating, turn-7 payment and the turn-18 result. Happy Hour activation and
no-activation alternatives change its locked bill and paid satisfaction.
Both runs win; no defeat is attributed to this single choice.

- [Diagram notes and source identity](../Stage3/Diagram_Notes.md)
- [Stage 2 to 3 changes](../Stage3/Stage_2_to_3_Changes.md), [PDF](../Stage3/Stage_2_to_3_Changes.pdf)
- [Class layout/model JSON](stage3-class-layout.json)
- [Sequence events and fragments JSON](stage3-sequence-model.json)
- [Implementation SHA-256 provenance](stage3-diagram-source-identity.json)
- [Optional generator](generate_stage3_diagrams.py), reusing the Stage 2 vector primitives.
- Earlier compact Mermaid discussion views: [class](Stage3_UML_Diagram.mmd),
  [sequence](Stage3_Happy_Hour_Sequence.mmd). The full PDF/drawio exports include
  additional auxiliary types and explicit delegation/factory messages.

Open drawio files in diagrams.net to edit individual shapes and labels. PDF/SVG
are vector exports; PNGs are separate display assets. Large diagrams are
single continuous pages intended for zooming rather than unreadable A4 scaling.
The source implementation is 3f5e682; exported font glyphs are embedded in PDFs.

To regenerate with optional Python ReportLab installed:

```bash
python3 docs/UML/generate_stage3_diagrams.py
```

The generator checks source/script/test hashes first and refuses to silently
render an outdated model over changed gameplay. Regenerate PNGs separately
from the PDFs at 96 dpi with Poppler. Mandatory Java gameplay is independent of
all Python/export tooling. Existing Stage 1 and Stage 2 exports are preserved.

## Stage 2 released design

- [Class diagram PDF](Stage2_UML_Diagram.pdf), [SVG](Stage2_UML_Diagram.svg),
  [editable drawio](Stage2_UML_Diagram.drawio).
- [VIP sequence PDF](Stage2_VIP_Sequence_Diagram.pdf), [SVG](Stage2_VIP_Sequence_Diagram.svg),
  [editable drawio](Stage2_VIP_Sequence_Diagram.drawio).
- [Diagram notes and actual replay evidence](../Stage2/Diagram_Notes.md).
- [Optional documentation generator](generate_stage2_diagrams.py); Java gameplay
  does not depend on Python or ReportLab. Layout/event JSON exports accompany it.

These diagrams describe the stage-2 production code; the release
verification record confirms unchanged signatures/behaviour and records the
three whitespace-only file differences from the drawing-time hashes. The class
diagram retains the Stage 1 notation; blue headers mark Stage 2 changes. The
sequence is one continuous vector diagram with turn bands and a replayed
alternative. See [the release verification](../verification/Stage_2_Release_2026-11-08.md)
for source identity and tagged-snapshot validation.

## Stage 1 reference

[Open the current vector PDF](Stage1_UML_Diagram.pdf).

The diagram originates in team main commit `f24c186`. It replaces the earlier
10-class proposal and includes actual boundary/control/domain classes,
StaffGateway/StaffTeam, OrderPort/Factory, RegularCustomer, seating/queue,
records and enums. Static operations/fields are underlined; visibility and
multiplicities appear in the diagram. Association role names also represent
reference-valued fields, which are not duplicated in class boxes.

On 8 November 2026 the ARRIVAL_TURNS field type was corrected to Set<Integer>,
and the encapsulation note was aligned with applyWaitingPenalty(), require()
and state/turn guards. Vector drawing and layout are retained. WaitingQueue's
remove(Customer): boolean was already correct in the source PDF; no return-type
change was necessary. The earlier audit statement calling it void was an error.

The older Stage_1_Design_Discussion_EN Markdown/PDF are historical design
proposals; use [the implemented design](../Stage_1_Implemented_Design_EN.md) for
current responsibilities and trade-offs. The new Stage 2 files above provide the matching next-stage class diagram and
the selected complex Stage 2 sequence diagram. The Stage 3 review sources above describe the new code and must be visually
reviewed when exported for the final report.
