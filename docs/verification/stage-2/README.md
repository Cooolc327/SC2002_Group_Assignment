# Successful Stage 2 clean-candidate execution logs

Tested implementation commit: `f8a70258fe3300318a00cb913d45abefa1a552b5`.
The archive path contained spaces, commands ran from an external cwd and all
processes ran under the macOS `deny network*` policy. No tool stdout containing
failed exploratory runs is presented as a successful log.

[Verification narrative](../Stage_2_Pre_Tag_2026-11-08.md) explains the precise
scope, source identity, expected answers and environment limits. Logs are agent
execution, not an E/human signature. The subsequent evidence-only commit retains
the tested src/test/scripts/UML bytes; the stage-2 tag is deliberately held.

| File | Link | Bytes |
| --- | --- | --- |
| `README.txt` | [open](README.txt) | 266 |
| `clean-build.log` | [open](clean-build.log) | 111 |
| `execution.json` | [open](execution.json) | 11182 |
| `external-defeat.log` | [open](external-defeat.log) | 19980 |
| `external-victory.log` | [open](external-victory.log) | 19250 |
| `external-vip-wait-turn5.log` | [open](external-vip-wait-turn5.log) | 19228 |
| `java-version.log` | [open](java-version.log) | 195 |
| `javac-version.log` | [open](javac-version.log) | 14 |
| `network-policy.log` | [open](network-policy.log) | 355 |
| `regular-regression.log` | [open](regular-regression.log) | 1394 |
| `stage1-checks-cli-bom-victory.log` | [open](stage1-checks-cli-bom-victory.log) | 19202 |
| `stage1-checks-cli-chef-invalid-input-victory.log` | [open](stage1-checks-cli-chef-invalid-input-victory.log) | 19442 |
| `stage1-checks-cli-defeat.log` | [open](stage1-checks-cli-defeat.log) | 19781 |
| `stage1-checks-cli-invalid-input-victory.log` | [open](stage1-checks-cli-invalid-input-victory.log) | 19346 |
| `stage1-checks-cli-placed-order-abandonment.log` | [open](stage1-checks-cli-placed-order-abandonment.log) | 19330 |
| `stage1-checks-cli-ready-order-abandonment.log` | [open](stage1-checks-cli-ready-order-abandonment.log) | 19503 |
| `stage1-checks-cli-victory.log` | [open](stage1-checks-cli-victory.log) | 19202 |
| `stage1-checks-core-production-order.log` | [open](stage1-checks-core-production-order.log) | 628 |
| `stage1-checks-smoke-summary.log` | [open](stage1-checks-smoke-summary.log) | 639 |
| `stage2-checks-bill-calculator.log` | [open](stage2-checks-bill-calculator.log) | 289 |
| `stage2-checks-cli-bom-victory.log` | [open](stage2-checks-cli-bom-victory.log) | 19139 |
| `stage2-checks-cli-chef-retry-victory.log` | [open](stage2-checks-cli-chef-retry-victory.log) | 19379 |
| `stage2-checks-cli-defeat.log` | [open](stage2-checks-cli-defeat.log) | 19869 |
| `stage2-checks-cli-victory.log` | [open](stage2-checks-cli-victory.log) | 19139 |
| `stage2-checks-cli-waiter-retry-victory.log` | [open](stage2-checks-cli-waiter-retry-victory.log) | 19283 |
| `stage2-checks-controller-victory-timeline.tsv` | [open](stage2-checks-controller-victory-timeline.tsv) | 15239 |
| `stage2-checks-integration.log` | [open](stage2-checks-integration.log) | 901 |
| `stage2-checks-order-menu.log` | [open](stage2-checks-order-menu.log) | 352 |
| `stage2-regression.log` | [open](stage2-regression.log) | 1669 |
| `system.log` | [open](system.log) | 65 |
