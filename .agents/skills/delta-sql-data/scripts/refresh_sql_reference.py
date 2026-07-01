#!/usr/bin/env python3
"""
Rebuild a schema-only index from a Navicat/MySQL dump.

This script intentionally extracts only:
- table names
- table comments
- columns
- indexes
- INSERT statement counts

It never copies INSERT values, passwords, tokens, contact details, IPs, or logs.
"""

from pathlib import Path
from collections import Counter
import argparse
import json
import re


def build_index(sql_text: str, source_name: str):
    create_pat = re.compile(
        r"CREATE TABLE `([^`]+)`\s*\((.*?)\)\s*ENGINE\s*=\s*[^;]+?;",
        re.S | re.I,
    )
    tables = {}

    for match in create_pat.finditer(sql_text):
        table_name = match.group(1)
        body = match.group(2)
        statement = match.group(0)
        columns = []
        indexes = []

        for raw_line in body.splitlines():
            line = raw_line.strip().rstrip(",")
            col_match = re.match(r"`([^`]+)`\s+([^\s,]+(?:\([^)]+\))?)(.*)", line)
            if col_match:
                name, col_type, rest = col_match.groups()
                comment_match = re.search(r"COMMENT\s+'([^']*)'", rest, re.I)
                columns.append(
                    {
                        "name": name,
                        "type": col_type,
                        "nullable": "NOT NULL" not in rest.upper(),
                        "comment": comment_match.group(1) if comment_match else "",
                    }
                )
            elif line.upper().startswith(
                ("PRIMARY KEY", "UNIQUE INDEX", "UNIQUE KEY", "INDEX ", "KEY ")
            ):
                indexes.append(line)

        table_comment_match = re.search(r"COMMENT\s*=\s*'([^']*)'", statement, re.I)
        tables[table_name] = {
            "comment": table_comment_match.group(1) if table_comment_match else "",
            "columns": columns,
            "indexes": indexes,
        }

    insert_counts = Counter(
        re.findall(r"^INSERT INTO `([^`]+)`", sql_text, flags=re.M)
    )

    return {
        "metadata": {
            "source_file": source_name,
            "table_count": len(tables),
            "note": "INSERT counts are snapshot statements, not live database counts.",
        },
        "tables": {
            name: {
                **info,
                "snapshot_insert_count": insert_counts.get(name, 0),
            }
            for name, info in sorted(tables.items())
        },
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("dump", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()

    sql_text = args.dump.read_text(encoding="utf-8", errors="replace")
    index = build_index(sql_text, args.dump.name)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(
        json.dumps(index, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    print(f"Wrote {args.output} ({index['metadata']['table_count']} tables)")


if __name__ == "__main__":
    main()
