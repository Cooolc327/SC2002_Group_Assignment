# Stage 2 release verification - 8 November 2026

Runnable release: [`stage-2`](https://github.com/Cooolc327/SC2002_Group_Assignment/tree/stage-2).
[Formal Stage 1 to Stage 2 comparison](https://github.com/Cooolc327/SC2002_Group_Assignment/compare/stage-1...stage-2).
The user explicitly authorized proceeding from the pre-tag hold to publication.
This is agent-run verification, not an E or unperformed human signature.

## Version identity

- Exact fully verified pre-release baseline: [`5402f4a2c53ff24530b81ffbd29d7171bad08f6f`](https://github.com/Cooolc327/SC2002_Group_Assignment/commit/5402f4a2c53ff24530b81ffbd29d7171bad08f6f).
- Stage 1 remains `ac445a8f956232232188fd765eed98a5e8b49c46`.
- This release commit changes status/link documentation and bounded full AI
  history only. src/test/scripts and the diagram artifacts are byte-identical
  to the verified baseline. The UML documentation index is the sole changed
  file within docs/UML; graphic/source exports are unchanged.
- [Payload SHA-256 manifest](stage-2/release-baseline/payload-identity.json) and
  [exact baseline execution](stage-2/release-baseline/execution.json).
- The exact release SHA is the commit peeled from the annotated stage-2 tag:
  `git rev-parse stage-2^{commit}`. A document cannot embed its own future SHA;
  the tag and atomic publication receipt supply that identity.

B's source commit 7d45e18 and C's 8e4f6a3 remain in ancestry. Neither main nor
other members' branches are changed by this release. Stage 1 tag/branch are
preserved. No Host, ComboMeal or Happy Hour implementation is included.

## Verification and publication procedure

The baseline was tested from a clean git archive under macOS sandbox policy
`(version 1) (allow default) (deny network*)`. A loopback connection was first
verified to fail with EPERM/EACCES; all Bash/javac/Java child processes inherited
the policy. Archive paths contain spaces and commands run from an external cwd.

After committing these release documents, the actual local annotated tag archive
is extracted and subjected to the same build, both full regression scripts and
three external Main replays **before** publication. Branch lichen/stage_2 and
tag stage-2 are pushed atomically only if these checks pass. Remote branch SHA,
annotated tag object and peeled commit must match the reviewed local values.
The final user-facing publication result reports that exact verified identity.

Environment: macOS 26.5.1, Temurin JDK/javac 21.0.12, compiling with
`--release 17 -Xlint:all -Werror`. Actual JDK 17 and Windows execution are not
claimed. Python/ReportLab are optional documentation tooling, not game dependencies.

| Check | Verified baseline result; required again for final tag archive |
| --- | --- |
| Production build | BUILD_STAGE2=PASS |
| Regular core, actual Order / recording staff | 14 scenarios / 344 assertions |
| Regular actual staff/Main/CLI smoke | 9 scenarios / 94 assertions |
| Real VIP billing | 5 scenarios / 26 assertions |
| Menu and order lifecycle | 7 scenarios / 65 assertions |
| Mixed customer integration / actual default Main | 14 scenarios / 506 assertions |

Both scripts finish with their DEVELOPMENT_CHECKS=PASS marker.
[Build](stage-2/release-baseline/clean-build.log),
[Regular regression](stage-2/release-baseline/regular-regression.log),
[Stage 2 regression](stage-2/release-baseline/stage2-regression.log).

| Actual Main replay | Turns | Paid | Revenue | Paid average | Departures | Result |
| --- | --- | --- | --- | --- | --- | --- |
| Normal priority | 18 | 5 | $64.50 | 80.40 | 0 | VICTORY |
| All WAIT | 18 | 0 | $0.00 | N/A | 1 | DEFEAT |
| Waiter WAIT only at turn 5 | 18 | 5 | $64.50 | 77.80 | 0 | VICTORY |

VIP #2 pays $13.50 on turn 8 at satisfaction 90, or turn 9 / 85 in the one-WAIT
variant. The default late Critic cannot pay before turn 18; a Critic-first
production fixture separately verifies full service, full-price payment and
cancellation from all three unfinished order states. There is no early ending.
[Victory](stage-2/release-baseline/external-victory.log),
[defeat](stage-2/release-baseline/external-defeat.log),
[delayed VIP](stage-2/release-baseline/external-vip-wait-turn5.log).

## Included evidence and boundaries

- [Acceptance checklist](../Stage2/Stage_2_Acceptance_Checklist_ZH.md),
  [test notes](../Stage2/Stage_2_Test_Notes.md),
  [change analysis](../Stage2/Stage_1_to_2_Changes.md).
- [Stage 2 class/sequence explanation and editable sources](../Stage2/Diagram_Notes.md).
  Source/visibility/relationships were checked and PDFs rendered/inspected in
  the drawing phase; their bytes are retained in the release.
- [Full personal AI history](../../ai-history/stage-2/README.md) retains original
  ordered prompts, replies and public progress updates, with a source-derived
  cutoff and per-message hashes. Original Stage 1 transcripts remain unchanged.
  This release updates the continuation through its declared export cutoff;
  later publication replies must be appended on the next archive update.
- [Earlier pre-tag verification](Stage_2_Pre_Tag_2026-11-08.md) remains historical.

This completes the Stage 2 runnable snapshot, not the entire group submission.
Stage 3, the final report/reflections/video, other members' history links and
signed declarations remain separate group deliverables.
