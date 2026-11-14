# Stage 3 release verification - 14 November 2026

Runnable release: [stage-3](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-3).
[Formal Stage 2 to Stage 3 comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-2...stage-3).
The user explicitly authorized preparing and publishing this tag. This is
agent-run verification, not a teammate acceptance signature.

## Version identity

- Verified candidate: [c322b0ff1451e413e35c0a2cd97c71c1966988e8](https://github.com/Cooolc327/SC2002_Group_Assignment/commit/c322b0ff1451e413e35c0a2cd97c71c1966988e8).
- Gameplay implementation: [3f5e682f775734a6a305fc159abd89aaf76257c3](https://github.com/Cooolc327/SC2002_Group_Assignment/commit/3f5e682f775734a6a305fc159abd89aaf76257c3).
- This release adds status/link documentation, bounded public AI conversation
  evidence and baseline verification records. Production source, scripts,
  tests, replay inputs and diagram exports are unchanged from the candidate.
- [Unchanged-payload SHA-256 manifest](stage-3/release-baseline/payload-identity.json)
  and [baseline execution results](stage-3/release-baseline/execution.json).
- The release commit is the commit peeled from the annotated tag:
  `git rev-parse 'stage-3^{commit}'`. A document cannot embed its own future
  commit SHA. The tag and final publication receipt identify that commit.

Bernice's original Stage 3 commits remain in the ancestry. The release branch
is `lichen/stage_3`. The publication updates that branch and creates only the
new `stage-3` tag; main and the historical stage-1/stage-2 tags are preserved.

## Clean-archive checks and publication procedure

The baseline was extracted using git archive into a fresh directory whose path
contains spaces. All 61 tracked source/script/test/replay files match the
implementation source manifest. Build and all three regression scripts ran
successfully. Separate default Main victory/defeat inputs were run from an
external working directory through the absolute run.sh path.

After the documentation commit, the local annotated stage-3 tag archive must
pass the same build, all three regression scripts and both external replays
again before publication. Its unchanged payload must match the baseline
manifest. The release branch and tag are pushed atomically. Publication is
reported successful only after the remote branch commit, tag object and peeled
tag commit match the local reviewed identities.

Environment: Temurin OpenJDK/javac 21.0.12 on macOS; compilation uses
`--release 17 -Xlint:all -Werror`. Native JDK 17 and Windows execution are not
claimed. Mandatory gameplay and Java checks use no third-party Java libraries
or network services; no OS-level network-denial test was performed this time.
Python/ReportLab are optional diagram tooling, not gameplay dependencies.

| Check | Verified result; required again for the final tag archive |
| --- | --- |
| Production build | BUILD_STAGE3=PASS |
| Regular core, actual Order | 14 scenarios / 344 assertions |
| Production smoke and CLI | 9 / 94 |
| Billing, including VIP and Happy Hour | 8 / 48 |
| Menu and Order lifecycle | 7 / 65 |
| Stage 2 integration and --stage2 CLI | 14 / 506 |
| ComboMeal | 6 / 42 |
| Stage 3 integration and default Main/CLI | 18 / 550 |
| Unique total; repeated billing counted once | 76 / 1649 |

[Build log](stage-3/release-baseline/clean-build.log),
[Stage 1 regression](stage-3/release-baseline/stage1-regression.log),
[Stage 2 regression](stage-3/release-baseline/stage2-regression.log),
[Stage 3 regression](stage-3/release-baseline/stage3-regression.log).

| External default Main replay | Turns | Paid | Revenue | Paid average | Departures | Result |
| --- | --- | --- | --- | --- | --- | --- |
| Happy Hour on turn 4, normal priorities | 18 | 5 | $83.70 | 87.20 | 0 | VICTORY |
| Never activate, all staff wait | 18 | 0 | $0.00 | N/A | 1 | DEFEAT |

[Victory log](stage-3/release-baseline/external-victory.log),
[defeat log](stage-3/release-baseline/external-defeat.log).
Both use the required mixed seed 24369 and complete all 18 turns. Feature
checks cover independent Host/Waiter limits, two-turn policy recovery and
expiry, VIP-then-Happy-Hour rounding, locked bills, combo preparation and
abandonment, phase/input retries, immutable snapshots, BOM and EOF.

## Included design evidence and remaining group work

- [Stage 3 interfaces and review ownership](../Stage3/Stage_3_Design_and_Interfaces_EN.md).
- [Stage 2 to 3 analysis](../Stage3/Stage_2_to_3_Changes.md) and
  [four-page PDF](../Stage3/Stage_2_to_3_Changes.pdf).
- [Class diagram](../UML/Stage3_UML_Diagram.pdf),
  [complete Happy Hour sequence](../UML/Stage3_Happy_Hour_Sequence.pdf), and
  [PNG/SVG/editable drawio sources](../UML/README.md).
- [Diagram provenance](../Stage3/Diagram_Notes.md) and
  [reproducible feature/replay notes](../Stage3/Stage_3_Test_Notes.md).
- [Available ordered Stage 3 AI conversation](../../ai-history/stage-3/README.md),
  with explicit export cutoffs. Later publication replies are outside the
  release archive unless included in a subsequent continuation.

The diagram/change PDFs and implementation record were created on 13 November
and retain the pre-tag wording from that date. They describe unchanged source;
this release record and root README control the current publication status.
The full class model has 45 classifiers and 65 relationships; the complete
sequence has 96 messages and six alt/loop fragments. Existing visual checks
remain applicable because diagram and production-source bytes are unchanged.

This tag completes the runnable Stage 3 snapshot. The final group PDF report,
individual reflections, approximately 15-minute video and README timestamps,
other members' AI evidence, signed original-work/GAI declarations, ZIP packaging
and course submission remain separate work. The deadline is 15 November 2026,
23:59 Singapore time (UTC+8).
