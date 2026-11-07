# Stage 1 Design Materials

## Implemented member A core

- [Member A implementation and integration](Member_A_Integration.md): implemented
  responsibilities, project location, C/D interface contracts, CLI call sequence,
  source changes, verified scenarios and reproducible victory/defeat runs.
- [Project README](../README.md): current integration status and test command.

Run `bash scripts/test-member-a.sh` from the repository root. The member-A core
suite verifies 14 scenarios and 344 assertions with test-only order/staff adapters.
Production Order, staff and CLI integration is still required.

## Earlier design proposal

English design proposal for team discussion before implementation.

- [PDF design document](Stage_1_Design_Discussion_EN.pdf) - includes class responsibilities, relationships, interface contracts, states, turn rules, acceptance scenarios, and the UML diagram.
- [Editable English text](Stage_1_Design_Discussion_EN.md).

The architecture and method contracts are proposed team decisions. Acceptance scenarios are manually derived specifications pending implementation and execution.