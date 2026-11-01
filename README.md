# SC2002 Restaurant Rush

Java command-line, turn-based restaurant management coursework.

## Current implementation

Member A handoff updated on 31 October 2026. Open the repository folder that
contains src, test, scripts and docs; run the checks below from that root.

The Stage 1 member-A core is implemented: turn coordination, restaurant domain
coordination, immutable views, statistics, and final victory/defeat evaluation.
It uses the existing customer, seating and menu modules.

Member C's production Order and member D's staff/CLI still need to be connected
through the documented contracts. This is not yet a complete playable Stage 1
release; do not create the stage-1 release tag until those integrations pass.

## Verify member A

Requirements: JDK 17 or later and Bash. No network access, external libraries,
Maven, or Gradle are required.

    bash scripts/test-member-a.sh

The script compiles all Java source and tests with Java 17 compatibility,
all compiler warnings enabled, and warnings treated as errors. It then runs
the member-A integration tests in a temporary build directory.

The tests use clearly identified test doubles for C's Order and D's staff.
They verify A's code; they do not verify C/D production implementations or
provide the final interactive game.

See [Member A integration and verification](docs/Member_A_Integration.md)
for ownership, exact interfaces, reproducible runs, and remaining integration.

Verified member-A results: 14 scenarios and 344 assertions pass. The reproducible
victory run completes 18 turns with 5 paid customers, $66.00 revenue and 79.20
average paid satisfaction. The all-WAIT run completes 18 turns in defeat.
These results use the test-only order and staff adapters described above.

## Design material

The earlier [Stage 1 design proposal](docs/Stage_1_Design_Discussion_EN.md)
records the proposed architecture. The member-A integration document describes
the implemented interfaces and differences from that proposal. The final UML
must be updated to match the final integrated code.
