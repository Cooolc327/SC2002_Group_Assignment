# Stage 2 pre-tag verification, 8 November 2026

**Implementation verification: PASS. Tag creation: HELD, not executed.**
The user authorized completing acceptance evidence, commit/push and clean-copy
validation up to the tag step. This record documents agent execution; it does
not impersonate E or an unperformed independent human review.

## Exact tested version and lineage

- Tested implementation commit: [`f8a70258fe3300318a00cb913d45abefa1a552b5`](https://github.com/Cooolc327/SC2002_Group_Assignment/commit/f8a70258fe3300318a00cb913d45abefa1a552b5).
- Stage 1 baseline: `ac445a8f956232232188fd765eed98a5e8b49c46` (`stage-1`).
- Implementation merge parent: B's `7d45e18943d6fc521772a11b86cdd4ed2badbcc8`.
  C's `8e4f6a36c8b85f41b1b5a2f2bf747ef946f16283` is already its ancestor.
- [Immutable implementation comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-1...f8a70258fe3300318a00cb913d45abefa1a552b5).
- [Complete current candidate comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-1...lichen/stage_2).

This record/logs and the latest transcript cutoff are added in an evidence-only
follow-up commit. Its production source, tests, scripts and all files under
`docs/UML/` are checked byte-for-byte against the tested payload manifest in
[execution.json](stage-2/execution.json). This avoids falsely claiming a document
can embed its own as-yet-unknown commit SHA. Resolve the delivered branch head
with `git rev-parse HEAD` and compare it with the remote `lichen/stage_2` ref.

## Environment and isolation

- macOS 26.5.1 (build 25F80).
- Temurin OpenJDK / javac 21.0.12; compilation targets Java 17 with
  `--release 17 -Xlint:all -Werror`.
- The committed archive was extracted into a new directory whose path contains
  spaces, without `.git`, previous class files or untracked local helpers.
- Commands ran from a different, external working directory using absolute
  launcher paths. Build/run scripts locate their own project root.
- The entire runner inherited macOS sandbox profile
  `(version 1) (allow default) (deny network*)`. A loopback connect was first
  verified to fail with EPERM/EACCES, then all Java children ran under that policy.
- Actual JDK 17 runtime and Windows execution are not claimed. ReportLab/Python
  are optional documentation tooling, not game/build dependencies.

## Executed commands and observed markers

| Check | Actual result |
| --- | --- |
| `bash scripts/build.sh` | BUILD_STAGE2=PASS |
| Regular core, actual Order / recording staff | 14 scenarios, 344 assertions |
| Regular real staff/Main/CLI smoke | 9 scenarios, 94 assertions |
| Actual VIP pricing / rounding / lock | 5 scenarios, 26 assertions |
| Menu and Order lifecycle | 7 scenarios, 65 assertions |
| Mixed customer integration / actual default Main | 14 scenarios, 506 assertions |

The two scripts reported `STAGE1_DEVELOPMENT_CHECKS=PASS` and
`STAGE2_DEVELOPMENT_CHECKS=PASS`. [Aggregate Regular log](stage-2/regular-regression.log),
[aggregate Stage 2 log](stage-2/stage2-regression.log),
[build log](stage-2/clean-build.log), [network policy](stage-2/network-policy.log)
and [all successful logs](stage-2/README.md) are retained.
The Regular smoke explicitly uses Main --regular; it is a regression mode on
Stage 2 code, not a replacement for the immutable Stage 1 tag.

## Three complete external launcher replays

| Input | Turns | Paid | Revenue | Paid average | Departures | Actual result |
| --- | --- | --- | --- | --- | --- | --- |
| stage2-victory.txt | 18 | 5 | $64.50 | 80.40 | 0 | VICTORY |
| stage2-defeat.txt | 18 | 0 | $0.00 | N/A | 1 | DEFEAT |
| stage2-vip-wait-turn5.txt | 18 | 5 | $64.50 | 77.80 | 0 | VICTORY |

Each ran the actual `scripts/run.sh` / Main, displayed 18 turns, displayed both
VIP #2 and Critic #6, produced exactly one final result, and had no invalid input.
The normal VIP payment is turn 8 / $13.50 / satisfaction 90; the one-WAIT variant
pays on turn 9 / $13.50 / 85. These turn-local Cashier outputs were checked,
not just the final rounded average.

[Victory output](stage-2/external-victory.log),
[defeat output](stage-2/external-defeat.log),
[one-WAIT output](stage-2/external-vip-wait-turn5.log).
The assertion oracles are the documented expected answers in
[test notes](../Stage2/Stage_2_Test_Notes.md), not values blindly copied from
this execution. The separate Critic-first production fixture covers its full
service/payment flow; default Critic #6 is intentionally too late to pay.

## Requirements, design and identity

The [34-row acceptance checklist](../Stage2/Stage_2_Acceptance_Checklist_ZH.md)
links each baseline and Stage 2 requirement to its source, test or artifact.
No VIP/Critic `instanceof`/type chain occurs in GameController. Factory registration
selects types; Customer dispatches loss, food choice and discount. No Host,
ComboMeal or HappyHour source class is present. Win thresholds remain unchanged.

The Stage 2 class and complex sequence diagrams were checked against source and
rendered/inspected in the drawing step. Their PDF/SVG/drawio/generator/layout
bytes are unchanged in the tested candidate; the diagram-source identity confirms
all 31 production files and the unchanged Stage 1 reference PDF.
Three imported customer files had trailing whitespace removed before committing;
the identity records original drawing-time hashes, current hashes and normalized
line equivalence. `.gitattributes` marks PDFs binary to preserve their bytes.

[Change analysis](../Stage2/Stage_1_to_2_Changes.md),
[diagram explanation and provenance](../Stage2/Diagram_Notes.md),
[source identity](../Stage2/Diagram_Source_Identity.json).

## Full personal history and publication boundary

[Lichen's Stage 2 archive](../../ai-history/stage-2/README.md) contains the original
available continuation after the Stage 1 cutoff, including public progress,
corrections and the transition gap. Every body is compared with its raw source
record; cutoff/counts/hashes are explicit. Later messages must be appended in the
next update. Other members maintain their own full chats; their missing links,
final report/reflections, video and signed declarations remain final-submission
work and are not fabricated here.

Only the personal `lichen/stage_2` branch is to be published. `main`, `stage-1`,
`lichen/stage_1` and collaborator branches are preserved. Remote branch equality
is checked after push. The annotated `stage-2` tag remains the next held action;
this candidate is not represented as a completed stage-2 tag.
