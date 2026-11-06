# Lichen Yin Stage 1 AI conversation history

This folder records Lichen Yin's (Member A) project conversations with OpenAI
Codex. It contains full user/assistant message text from the three available
related conversation logs, in the original order within each conversation.
It is a conversation archive, not a rewritten project summary or a personal
reflection. Original Chinese and English are retained.

## Conversation index

Times below are Singapore time (UTC+08:00). The first conversation spans the
main planning, implementation, integration and manual-checking work; related
design and audit sessions are kept separately rather than interleaved.

| File | Coverage | User messages | Assistant messages | Screenshots | Topic |
| --- | --- | --- | --- | --- | --- |
| [chat-01.md](chat-01.md) | 2026-10-25 13:19:35 to 2026-11-06 15:52:37 | 30 | 70 | 8 | Planning implementation integration code walkthroughs and manual testing |
| [chat-02.md](chat-02.md) | 2026-10-25 14:09:51 to 2026-10-27 23:21:22 | 5 | 17 | 0 | Stage 1 class responsibilities interfaces UML design and explanation |
| [chat-03.md](chat-03.md) | 2026-10-27 23:52:13 to 2026-10-28 00:00:22 | 2 | 7 | 0 | Member A scope requirements and initial repository audit |

Total: **131 messages** (37 user, 94 assistant) and **8 user screenshots**.
Screenshots are recovered as stored in the original conversation, retained
under `assets/`, and linked at the corresponding messages. Their stored
resolution may differ from the original screen-capture resolution.

## Requirements checked against the supplied documents

The project briefing adds individual evidence requirements to the assignment
manual. The files reviewed are `SC2002_Group_Project_Briefing.pdf` (12 pages)
and `SC2002_Restaurant_Rush_Group_Assignment(1).docx`.

| Source | Requirement | How this branch addresses it |
| --- | --- | --- |
| Briefing page 7 | Upload all project-related prompts, AI replies and follow-up exchanges, in order; a summary alone is insufficient | Full text from the available related conversations, including visible progress replies; no paraphrased replacement |
| Briefing page 7 | Put the history on your own branch in the group GitHub repository | Personal branch `lichen/stage_1` |
| Briefing page 8 | Each member's AI chats are on their own branch; README links every stage and member's AI history | Root README links Lichen's history on this branch; other members and unpublished stage tags remain pending |
| Briefing page 8 | Example path `ai-history/stage-1/chat-01.md` | This folder adopts that example convention; the briefing does not prescribe an exact personal branch name or transcript template |
| Manual section 5 | Runnable stage tags and actual code comparisons | Separate group evidence; creating a personal branch does not create `stage-1` |
| Manual section 6.1; briefing pages 9-10 | Original-work/GAI declarations and individual named reflections | Separate submission items; this archive does not replace signed forms or the member's reflection |

## Export scope and fidelity

- Message bodies come from actual conversation records, not memory summaries.
  Wording, code, corrections and historical claims are preserved. Only line
  endings and trailing whitespace are normalized for Markdown.
- Each message has its speaker and original timestamp. User-facing assistant
  progress messages are included. System/developer instructions, automatic
  environment/page metadata, hidden reasoning and tool calls/results are not
  user/assistant chat replies and are excluded.
- `chat-01.md` ends at the user's 6 November 2026, 15:52:37 request to consolidate
  the personal branch and save this history. The current export operation's
  assistant reply and later conversations should be appended in a later update;
  they are not claimed to be present in this snapshot.
- `chat-02.md` and `chat-03.md` contain the available related design/UML and
  initial-repository-audit conversations through their recorded final replies.
  Their source conversation IDs are recorded in the transcript headers.
- The main chat includes assignment-wide planning and an ambiguous training
  question, retained in context rather than edited out. References to future
  stages in planning are historical conversation content, not implemented
  Stage 2/3 features in this code snapshot.
- These are the three related Codex conversations recovered locally. If Lichen
  used other project-related chats or AI services, add those full histories as
  well; this export does not certify that no other conversations exist.
- Some original replies link to absolute local files or earlier branch names.
  These are retained as spoken historical text. Use the current root README and
  [manual checklist](../../docs/Stage_1_Manual_Checklist_ZH.md) for current paths,
  branch status and uncompleted acceptance work.

## How to maintain the history

Append complete later exchanges in chronological order or add a new numbered
chat file and update this index. Keep screenshots with their corresponding
messages. Keep original errors and subsequent corrections; do not rewrite past
messages to make the development appear different. Update root README links
when personal branches change. Each member maintains their own evidence.

This branch contains the prior Member A commits and integrated Stage 1
candidate. The current manual walk-through confirmed C1's lifecycle through
payment at the start of turn 6; Chef invalid-input checks and the later manual
Pasta/win/lose checks were not completed in that walk-through. Existing automated
results are recorded separately from E's independent acceptance.
