# Stage 3 runnable implementation verification

Date: 13 November 2026 (UTC+8). Branch: lichen/stage_3.
Baseline main: b02de7bef0fa237a693f5c84a9b0757d43dfb023.
Merged Bernice source: c013d2b29c78905f7431f564dc5048aafaab9ce0.

A fresh copy in a path containing spaces was built and checked independently
of the development build outputs. Source, script and test bytes matched the
development copy exactly. `build.sh`, `test-stage1.sh`, `test-stage2.sh`, and
`test-stage3.sh` all passed with release 17, all lint warnings treated as errors.

Unique results (the repeated billing suite is counted once):

| Suite | Scenarios | Assertions |
| --- | --- | --- |
| Stage 1 core | 14 | 344 |
| Stage 1 smoke | 9 | 94 |
| Billing | 8 | 48 |
| Order/menu | 7 | 65 |
| Stage 2 integration | 14 | 506 |
| ComboMeal | 6 | 42 |
| Stage 3 integration | 18 | 550 |
| Total | 76 | 1649 |

The default real-CLI victory completes 18 turns with five paid customers,
$83.70 revenue, average satisfaction 87.20, and no departures. The default
defeat completes 18 turns with zero paid, $0.00 revenue, N/A average and one
departure. The required mixed seed remains 24369.

See [test notes](../Stage3/Stage_3_Test_Notes.md) and
[source identity](../Stage3/Implementation_Source_Identity.json).
Generated logs remain under ignored build/stage1-checks, build/stage2-checks
and build/stage3-checks. These are agent-run checks, not a teammate signature.

The initial implementation had compact Mermaid models. A later documentation
update adds visually reviewed PDF/SVG/PNG and editable drawio exports; see
[diagram notes](../Stage3/Diagram_Notes.md) for exact source identity. Final
report, video, member declarations, group
acceptance and an explicitly authorized stage-3 release/tag remain separate
submission work. Existing historical tags are not moved.
