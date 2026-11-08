# Final verified baseline for Stage 2 release

These successful logs belong to exact commit `5402f4a2c53ff24530b81ffbd29d7171bad08f6f`, the final pre-tag
candidate, distinct from the earlier f8a7025 preparation logs in the parent
folder. Commands ran from an external cwd on a clean committed archive with a
path containing spaces, under a deny-network macOS sandbox.

- [Execution manifest and exact commands](execution.json)
- [Frozen implementation and diagram hashes](payload-identity.json)
- [Build](clean-build.log)
- [Regular regression](regular-regression.log)
- [Mixed Stage 2 regression](stage2-regression.log)
- [Victory](external-victory.log)
- [Defeat](external-defeat.log)
- [One delayed VIP order](external-vip-wait-turn5.log)
- [Java](java-version.log), [javac](javac-version.log), [macOS](system.log)

The release commit changes documentation/history only. The actual local
annotated stage-2 archive is built, tested and replayed again before atomic
branch/tag publication. Resolve the release commit using the stage-2 tag; a
release document cannot contain its own future commit SHA.
