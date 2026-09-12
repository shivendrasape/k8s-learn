# Agent Skills Workflow & Lifecycle Rules

This project uses modular engineering skills from `agent-skills` (located in `.agents/skills/`) to execute development workflows systematically.

---

## 1. Skill Execution Model

- When a task matches an available skill, consult and apply the skill instructions.
- Do not bypass workflows with rationalizations like "this is too small" or "I'll quickly do it".
- Workflows must follow structured phases: Spec &rarr; Plan &rarr; Build &rarr; Verify &rarr; Review.

## 2. Intent to Skill Mapping

Automatically map development intents to corresponding skills:

| Intent | Skill | Antigravity Action |
| :--- | :--- | :--- |
| New feature / major capability | `spec-driven-development` | Draft specification before implementation |
| Breaking down complex task | `planning-and-task-breakdown` | Create ordered, verifiable task breakdown |
| Implementation / feature coding | `incremental-implementation` & `test-driven-development` | Small steps, red-green-refactor cycle |
| Bug / regression / test failure | `debugging-and-error-recovery` | Root-cause analysis, reproduction test first |
| Code review & quality audit | `code-review-and-quality` | 5-axis review (correctness, readability, architecture, security, performance) |
| Refactoring & simplification | `code-simplification` | Eliminate dead code/complexity without behavior changes |
| Quality contract & standards | `constraint-driven-development` | Enforce test/quality floors, reject regressions |
| API / Interface design | `api-and-interface-design` | Design robust contracts, backward compatibility |
| Frontend / UI changes | `frontend-ui-engineering` | Accessible, responsive, modern web standards |
| Production preparation | `shipping-and-launch` | Checklist, rollback plan, pre-flight checks |

## 3. Specialist Subagents

For complex reviews or audits, leverage specialized personas defined in `.agents/agents/`:
- `code-reviewer`: 5-axis code quality and architecture review.
- `security-auditor`: Vulnerability detection, sanitization, threat review.
- `test-engineer`: Test strategy, edge case coverage, test writing.
- `web-performance-auditor`: Loading, network, and rendering performance.

## 4. Project Stack Notes
- Adhere to project guidelines, architectural boundaries, and idiomatic patterns.
- Always verify changes against cluster/container constraints before marking work complete.
