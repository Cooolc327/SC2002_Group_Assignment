# Project documentation

## Stage 3 runnable integration candidate

- [Interfaces and design](Stage3/Stage_3_Design_and_Interfaces_EN.md)
- [Stage 2 to 3 changes](Stage3/Stage_2_to_3_Changes.md)
- [Tests and replay timeline](Stage3/Stage_3_Test_Notes.md)
- [Class diagram PDF](UML/Stage3_UML_Diagram.pdf)
- [Happy Hour sequence PDF](UML/Stage3_Happy_Hour_Sequence.pdf)
- [Standalone images and editable drawio sources](UML/README.md)
- [Stage 2 to 3 change report PDF](Stage3/Stage_2_to_3_Changes.pdf)

Default Main runs Stage 3. Use `--stage2` for Stage 2 replay inputs and
`--regular` for Regular-only inputs. Stage 3 release/tag and final group
submission remain separate steps.


## Published Stage 2

- [Release verification](verification/Stage_2_Release_2026-11-08.md)
- [Acceptance checklist](Stage2/Stage_2_Acceptance_Checklist_ZH.md)
- [Test notes](Stage2/Stage_2_Test_Notes.md) and [stage changes](Stage2/Stage_1_to_2_Changes.md)
- [Class/sequence diagrams and evidence](Stage2/Diagram_Notes.md)
- [Integration explanation](Stage2/Integration_Notes.md)
- [Full personal AI history](../ai-history/stage-2/README.md)
- [Earlier pre-tag record](verification/Stage_2_Pre_Tag_2026-11-08.md), retained as history

The runnable snapshot is stage-2 on personal branch lichen/stage_2. The final
release validates its annotated tag archive. Stage 2 was later merged into
main as b02de7b; this branch extends that baseline for Stage 3.

## Published Stage 1 documentation

The runnable release is `stage-1` on personal branch `lichen/stage_1`.

- [Implemented design](Stage_1_Implemented_Design_EN.md): actual responsibilities,
  relationships, timing, design choices and trade-offs.
- [Current class diagram](UML/Stage1_UML_Diagram.pdf) and [diagram notes](UML/README.md).
- [Reproducible victory/defeat notes](Stage_1_Test_Notes.md).
- [8 November agent verification](verification/Stage_1_Release_2026-11-08.md):
  tested commit, actual results, scope, source/test identity and limitations.
- [Review handoff](Stage_1_E_Handoff.md): requirement matrix and optional human
  review form; agent execution is not an E signature.
- [Manual checklist in Chinese](Stage_1_Manual_Checklist_ZH.md).
- [A integration details](Member_A_Integration.md).
- [Lichen's full available Stage 1 AI history](../ai-history/stage-1/README.md).
- [Root README](../README.md): build/run, stages, tests and team evidence links.

On the shared current code, run `bash scripts/run.sh --regular` for Regular-only
regression, or `bash scripts/test-stage1.sh` for the baseline checks. Use the
`stage-1` tag for the actual historical Stage 1 version. Core: 14 scenarios/
344 assertions with real Order and recording staff. Production smoke: 9 scenarios/
94 assertions with real Order/staff/StaffTeam/Main/CLI. Mandatory gameplay and
checks require no external Java dependencies or network service.

## Historical materials

[Earlier English proposal](Stage_1_Design_Discussion_EN.md) and its
[PDF](Stage_1_Design_Discussion_EN.pdf) are retained as pre-implementation design
history, not current UML or verified execution evidence.
[6 November verification](verification/Stage_1_2026-11-06.md) describes that
older snapshot and remains historical evidence. The 8 November record controls
the current release. Other stages, individual reflections, group report/video
and signed declarations remain separate final-submission work.
