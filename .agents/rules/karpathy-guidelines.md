# Karpathy Coding Guidelines

These behavioral rules govern all coding, refactoring, and debugging tasks in this repository. They are derived from Andrej Karpathy's core insights on reducing common LLM coding pitfalls.

Bias toward caution over speed.

---

## 1. Think Before Coding
**Do not assume. Do not hide confusion. Surface tradeoffs.**
- State assumptions explicitly before writing code. If uncertain, ask.
- If multiple interpretations exist, present options—never pick one silently.
- If a simpler approach exists, say so and push back when warranted.
- If something is unclear, stop immediately, identify the ambiguity, and clarify.

## 2. Simplicity First
**Minimum code that solves the problem. Nothing speculative.**
- Implement only the features requested—no speculative extensions.
- Never introduce abstractions for single-use code.
- Avoid unnecessary "flexibility", generic hooks, or "configurability" that wasn't asked for.
- Do not write defensive error handling for impossible scenarios.
- If an implementation took 200 lines and could be done cleanly in 50, rewrite it.
- Senior engineer benchmark: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes
**Touch only what you must. Clean up only your own mess.**
- Do not modify adjacent code, comments, or formatting unless directly related.
- Do not refactor code that is not broken.
- Match existing code style, naming conventions, and architecture.
- If you notice unrelated dead code or bugs, note them—do not delete or fix without approval.
- Clean up orphans: remove imports, variables, or functions that your changes rendered unused.
- The test: Every single changed line must trace directly to the task.

## 4. Goal-Driven Execution
**Define success criteria. Loop until verified.**
- Frame every task around verifiable criteria before touching code:
  - Adding logic: write unit/integration tests or checks first, then implement.
  - Fixing a bug: reproduce with a test or verify with an exact assertion, then fix.
  - Refactoring: verify existing tests pass before and after.
- For multi-step tasks, lay out a brief plan with verifiable checkpoints:
  1. `[Step]` &rarr; verify: `[check]`
  2. `[Step]` &rarr; verify: `[check]`
