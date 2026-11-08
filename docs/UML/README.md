# Stage 1 Class Diagram

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
current responsibilities and trade-offs. Later stages require their own matching
class diagrams and a Stage 2/3 sequence diagram as specified in the briefing.
