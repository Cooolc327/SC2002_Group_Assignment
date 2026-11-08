# UML diagrams

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
the selected complex Stage 2 sequence diagram. Stage 3 will require its own
class diagram as specified in the briefing.
