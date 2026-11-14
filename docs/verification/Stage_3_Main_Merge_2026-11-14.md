# Stage 3 integration into main - 14 November 2026

The user explicitly authorized merging the released Stage 3 implementation
into main. Main now runs Stage 3 by default; the --stage2 and --regular modes
remain available for regression. This record describes agent-run checks and
does not claim an independent teammate acceptance signature.

## Source and history

- Main before merge: 263a160da2acec82e39d873bddd5982bf14cccb6.
- Stage 3 branch: lichen/stage_3 at fa2d608f50c43f3ca3aabf8ddfb58063ff95af74.
- The annotated stage-3 tag peels to that same Stage 3 commit. Its tag object
  remains 7b6fbbf134b9c37f0e688a6be16b05e60f6179c6.
- The merge retains both histories, including Bernice's original Stage 3 work,
  the release evidence and the recent main README link updates.
- README was the only merge conflict. The resolution uses the Stage 3 build,
  run, feature, test and diagram documentation, while preserving main's entire
  AI-history explanation/table, including Lichen's and Bernice's Stage 3 links.
- Added merge-status links in the root README and documentation index. Apart
  from those two indexes and this new record, the merged tree matches the
  released Stage 3 tree exactly. No production code is altered by resolution.

The final merge commit has the original main commit as its first parent and
the released Stage 3 commit as its second parent. The push uses a normal
fast-forward update to main; no stage tags or personal branches are moved.

## Verification

The prepared merged tree is exported into a fresh directory with spaces in its
path. All tracked exported files are checked against the prepared index bytes.
All 61 source/script/test/replay files match the implementation manifest; all
91 files in the Stage 3 release's unchanged-payload manifest also match.

Build and tests use Temurin OpenJDK/javac 21.0.12, compiling for Java 17 with
--release 17 -Xlint:all -Werror. No native JDK 17 or Windows execution is claimed.

| Suite | Scenarios | Assertions |
| --- | --- | --- |
| Regular core, production Order | 14 | 344 |
| Production smoke and CLI | 9 | 94 |
| Billing, including VIP and Happy Hour | 8 | 48 |
| Menu and Order lifecycle | 7 | 65 |
| Stage 2 integration and --stage2 CLI | 14 | 506 |
| ComboMeal | 6 | 42 |
| Stage 3 integration and default Main/CLI | 18 | 550 |
| Unique total, repeated billing counted once | 76 | 1649 |

Independent default Main replays, started from outside the project directory:

| Replay | Turns | Paid | Revenue | Paid satisfaction | Departures | Result |
| --- | --- | --- | --- | --- | --- | --- |
| stage3-victory.txt | 18 | 5 | $83.70 | 87.20 | 0 | VICTORY |
| stage3-defeat.txt | 18 | 0 | $0.00 | N/A | 1 | DEFEAT |

Before pushing, committed blobs must match the reviewed file/hash allowlist
and the clean tested export. Publication is confirmed by matching the remote
main SHA with the local merge SHA and rechecking all historical tag objects.

See [Stage 3 release verification](Stage_3_Release_2026-11-14.md),
[feature and replay notes](../Stage3/Stage_3_Test_Notes.md), and
[AI history](../../ai-history/stage-3/README.md) for detailed existing evidence.
The final group report, video/timestamps, member declarations, ZIP packaging
and course submission remain separate deliverables.
