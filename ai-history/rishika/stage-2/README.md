# Rishika Garg Stage 2 AI conversation history

This folder records Rishika Garg's (Member E) Stage 2 project conversations with
Claude Code. It contains the full user/assistant message text, in the original
order within each conversation. It is a conversation archive, not a rewritten
project summary or a personal reflection.

## Conversation index

| File | Date | User messages | Assistant messages | Topic |
| --- | --- | --- | --- | --- |
| [chat-01.md](chat-01.md) | 2026-11-14 | 3 | 30 | Stage 2 testing: stage-1 to stage-2 change analysis, Stage 1 regression, plain-Java Stage 2 rule tests and scripted victory/defeat runs |

Total: **33 messages** (3 user, 30 assistant).

## Export scope and fidelity

- Message bodies are copied from the conversation itself, not summarized.
  Wording, tables, code and claims are kept as originally written. Only line
  endings and trailing whitespace are normalized for Markdown.
- Each message has its speaker. Per-message timestamps are not included.
  User-facing assistant progress messages are included. System/developer
  instructions, environment metadata, internal reasoning and tool calls/results
  are not user/assistant chat replies and are excluded.
- One decision was made through a tool question dialog rather than a chat
  message, so it is not a transcript message. After Message 019 ("Here's the
  diff for you to approve before I apply it"), the assistant showed the one-line
  `Stage1RulesTest.java` change (adding `"--regular"` to the `Main` command in
  `runMain`) next to a system-property alternative, and the user chose
  "Apply it (Recommended)". Message 020 continues from that answer.
- `chat-01.md` ends at the Phase 3 report. The later request to export the
  conversation into this folder is not included. Phase 4 (the report) had not
  started at the time of export.
- File links in the assistant replies point to a temporary `stage-2` worktree
  (`../SC2002_stage2_test`) on the author's machine, and some replies name
  temporary scratchpad paths. They are kept as written. The test files described
  there (`test/qa/`) were not committed to the repository as part of this
  conversation.

## How to maintain the history

Append complete later exchanges in order or add a new numbered chat file and
update this index. Keep original errors and later corrections; do not rewrite
past messages.

[Stage 1 history](../stage-1/README.md) · [Personal history index](../README.md)
