# AI Agent Help — How to Implement a New Feature

This guide explains how to use the AI agent (Claude Code) to implement a new CRUD feature in `aero-ops-manager-service` using the established generic flow.

---

## Overview

The implementation workflow is:

```
1. Proto file is ready in aero-ops-manager-api/proto/
        ↓
2. Start Claude Code in aero-ops-manager-service/
        ↓
3. Give the agent the instruction prompt (below)
        ↓
4. Agent reads the proto + all docs and generates all files
        ↓
5. Review, adjust domain-specific rules, done
```

---

## Prerequisites

Before asking the agent to implement a feature:

- [ ] The proto file exists in `aero-ops-manager-api/proto/<Feature>.proto`
- [ ] The proto follows the conventions in `proto-definition-guideline.md`
- [ ] You know the next Flyway migration version number  
  (`ls src/main/resources/db/migration/` — use the next `V<n>` number)
- [ ] You know any domain-specific validation rules not obvious from the proto  
  (e.g. field must be unique, no spaces allowed, must match a specific format)

---

## Starting Claude Code

Open a terminal in the project root and start Claude Code:

```bash
cd /path/to/aero-ops-manager-service
claude
```

Or open the project in your IDE with the Claude Code extension active.

---

## The Instruction Prompt

Copy this prompt and paste it at the start of a new Claude Code session. Replace the placeholders.

```
Please implement a new feature for aero-ops-manager-service.

Read these files before generating any code:
- aero-ops-manager-service/aero-ops-common/docs/generic-flow-AI-Instruction.md
- aero-ops-manager-service/aero-ops-common/docs/Generic-Request-Flow.md
- aero-ops-manager-service/aero-ops-common/docs/Common-Types.md
- aero-ops-manager-service/aero-ops-common/docs/JPA-Type.md

Proto file to implement:
- aero-ops-manager-api/proto/<Feature>.proto

Next Flyway migration version: V<n>

Domain-specific rules:
- <list any validation or business rules not in the proto>
```

### Example — implementing a new feature from FuelSuppliers.proto

```
Please implement a new feature for aero-ops-manager-service.

Read these files before generating any code:
- aero-ops-manager-service/aero-ops-common/docs/generic-flow-AI-Instruction.md
- aero-ops-manager-service/aero-ops-common/docs/Generic-Request-Flow.md
- aero-ops-manager-service/aero-ops-common/docs/Common-Types.md
- aero-ops-manager-service/aero-ops-common/docs/JPA-Type.md

Proto file to implement:
- aero-ops-manager-api/proto/FuelSuppliers.proto

Next Flyway migration version: V28

Domain-specific rules:
- Supplier code must be unique across all carriers
- Supplier code must not contain spaces or special characters
```

---

## Asking for Specific Layers Only

If you only want the agent to generate a specific part, narrow the prompt:

```
Read aero-ops-manager-service/aero-ops-common/docs/Generic-Request-Flow.md
and aero-ops-manager-api/proto/<Feature>.proto.

Only generate the following files:
- <Entity>Mapper.java
- <Entity>Validator.java
```

---

## Asking the Agent to Check Its Own Work

After generation, ask the agent to verify against the checklist:

```
Review what you just generated against the checklist in:
aero-ops-manager-service/aero-ops-common/docs/generic-flow-AI-Instruction.md (Step 5 — Checklist)

Report any missing items.
```

---

## When the Feature Needs Carrier Security

If the entity is scoped by carrier, include the Carrier-Security doc:

```
Also read:
- aero-ops-manager-service/aero-ops-common/docs/Carrier-Security.md

The entity is carrier-scoped. Apply @AuthorizeCarriers and @FilterCarriers
as described in that document.
```

---

## When the Feature Needs Parallel Data Loading

If the gRPC endpoint needs to fetch from multiple sources simultaneously:

```
Also read:
- aero-ops-manager-service/aero-ops-common/docs/Async-Process.md

The getAll endpoint needs to assemble data from <source A> and <source B> in parallel.
Use AsyncProcessor as described in that document.
```

---

## When the Feature Needs a Feature Flag

```
Also read:
- aero-ops-manager-service/aero-ops-common/docs/Feature-Flags.md

Gate the <method name> method behind a feature flag named <FLAG_NAME>.
```

---

## Reference — All Docs and What They're For

| Doc | Use it when... |
|---|---|
| `generic-flow-AI-Instruction.md` | Starting any new feature — always include |
| `Generic-Request-Flow.md` | Agent needs full architecture context — always include |
| `proto-definition-guideline.md` | Agent needs to write or review a proto file |
| `Common-Types.md` | Feature uses validation, list merge, field diff, ranges |
| `JPA-Type.md` | Feature has complex search/filter logic or tuple queries |
| `Async-Process.md` | Endpoint loads data from multiple sources in parallel |
| `Carrier-Security.md` | Entity data is scoped by carrier code |
| `Feature-Flags.md` | Feature is behind a runtime toggle |
| `Accelaero-Logger.md` | Feature needs method level entry/exit/error logging with timing |
| `JOB_MONITORING.md` | Feature includes a scheduled background job |

---

## Tips for Best Results

- **Be specific about domain rules.** The agent derives structure from the proto but cannot infer business constraints (uniqueness, format, cross-field rules). State them explicitly.
- **One feature per session.** Start a fresh Claude Code session for each new feature to avoid context bleed.
- **Check the migration version.** The agent will ask or guess — always confirm the correct `V<n>` before the session ends.
- **Review generated constants.** The agent adds entries to `OpsPrivilegs`, `ErrorMessageFieldConstant`, and `AeroOpsConstants`. Check that the privilege key format matches the existing pattern (e.g. `aeroOps.<domain>.<action>`).
- **Audit fields in `byEntity`.** If `createdDate` or `lastUpdated` could be null (e.g. on a fresh entity not yet persisted), the mapper should guard against `NullPointerException`. Remind the agent if needed.
