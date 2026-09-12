#!/usr/bin/env python3
"""
git-safety-guard.py
Antigravity PreToolUse Hook:
- Hard denies any 'git push' commands from the agent.
- Forces interactive user approval ('force_ask') for any 'git commit' commands.
"""

import json
import re
import sys


def main():
    try:
        payload = json.load(sys.stdin)
    except Exception:
        # If input is not JSON, allow tool execution to avoid unexpected blockage
        print(json.dumps({"decision": "allow"}))
        return

    tool_call = payload.get("toolCall", {})
    tool_name = tool_call.get("name", "")

    if tool_name == "run_command":
        cmd = tool_call.get("args", {}).get("CommandLine", "")

        # 1. Require explicit user approval for 'git push'
        if re.search(r"\bgit\s+(?:-[^\s]+\s+)*push\b", cmd):
            result = {
                "decision": "force_ask",
                "reason": (
                    "The agent is attempting to run 'git push'. "
                    "Please review and approve before pushing to the remote repository."
                ),
            }
            print(json.dumps(result))
            return

        # 2. Force user prompt for 'git commit'
        if re.search(r"\bgit\s+(?:-[^\s]+\s+)*commit\b", cmd):
            result = {
                "decision": "force_ask",
                "reason": (
                    "The agent is attempting to run 'git commit'. "
                    "Please review staged changes before approving."
                ),
            }
            print(json.dumps(result))
            return

    # Default: allow the tool call to proceed normally
    print(json.dumps({"decision": "allow"}))


if __name__ == "__main__":
    main()
