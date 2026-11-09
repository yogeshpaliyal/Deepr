#!/usr/bin/env python3
"""Snapshot Deepr's GitHub stats into CSV files. Runs in GitHub Actions, not in the app.

Writes into OUT_DIR (the `stats` branch):
  downloads.csv   one row per release asset per snapshot
  repo.csv        stars / forks / watchers / open issues, one row per day
  views.csv       daily repo page views (needs TRAFFIC_TOKEN)
  clones.csv      daily clones (needs TRAFFIC_TOKEN)
  referrers.csv   top referring sites, one set per day (needs TRAFFIC_TOKEN)
  README.md       human-readable summary

Download totals survive release re-uploads: every upload gets a new asset id, so the
total is the sum of each asset id's highest count ever seen.

Env: GITHUB_TOKEN (required), REPO (owner/name), TRAFFIC_TOKEN (optional; GitHub's
traffic API needs a token with "Administration: read", which the Actions token lacks).
"""
import csv
import datetime as dt
import json
import os
import sys
import urllib.error
import urllib.request
from collections import defaultdict

API = "https://api.github.com"


def get(path, token):
    req = urllib.request.Request(
        API + path,
        headers={
            "Accept": "application/vnd.github+json",
            "Authorization": f"Bearer {token}",
            "X-GitHub-Api-Version": "2022-11-28",
            "User-Agent": "deepr-stats",
        },
    )
    with urllib.request.urlopen(req, timeout=30) as r:
        return json.load(r)


def paged(path, token):
    out, page = [], 1
    while True:
        sep = "&" if "?" in path else "?"
        items = get(f"{path}{sep}per_page=100&page={page}", token)
        out += items
        if len(items) < 100:
            return out
        page += 1


def read_rows(path):
    if not os.path.exists(path):
        return []
    with open(path, newline="") as f:
        return list(csv.DictReader(f))


def write_rows(path, fields, rows):
    with open(path, "w", newline="") as f:
        w = csv.DictWriter(f, fieldnames=fields)
        w.writeheader()
        w.writerows(rows)


def upsert(path, fields, key_fields, new_rows):
    """Replace rows with the same key, keep the rest, sort by key."""
    rows = {tuple(r[k] for k in key_fields): r for r in read_rows(path)}
    for r in new_rows:
        rows[tuple(str(r[k]) for k in key_fields)] = {k: str(r[k]) for k in fields}
    write_rows(path, fields, [rows[k] for k in sorted(rows)])


def main():
    token = os.environ["GITHUB_TOKEN"]
    repo = os.environ["REPO"]
    traffic_token = os.environ.get("TRAFFIC_TOKEN", "").strip()
    out = sys.argv[1] if len(sys.argv) > 1 else "."
    os.makedirs(out, exist_ok=True)
    now = dt.datetime.now(dt.timezone.utc).replace(microsecond=0)
    stamp, today = now.isoformat(), now.date().isoformat()

    # Downloads: append a snapshot of every asset's counter.
    dl_fields = ["recorded_at", "tag", "asset_id", "asset_name", "download_count"]
    dl_path = os.path.join(out, "downloads.csv")
    rows = read_rows(dl_path)
    for rel in paged(f"/repos/{repo}/releases", token):
        for a in rel.get("assets", []):
            rows.append({
                "recorded_at": stamp,
                "tag": rel["tag_name"],
                "asset_id": a["id"],
                "asset_name": a["name"],
                "download_count": a["download_count"],
            })
    write_rows(dl_path, dl_fields, rows)

    # Repo counters: one row per day (the latest run that day wins).
    info = get(f"/repos/{repo}", token)
    repo_fields = ["date", "stars", "forks", "watchers", "open_issues"]
    upsert(os.path.join(out, "repo.csv"), repo_fields, ["date"], [{
        "date": today,
        "stars": info["stargazers_count"],
        "forks": info["forks_count"],
        "watchers": info["subscribers_count"],
        "open_issues": info["open_issues_count"],
    }])

    # Traffic: GitHub keeps only 14 days, so store every day we see.
    traffic_ok = False
    if traffic_token:
        try:
            for kind in ("views", "clones"):
                data = get(f"/repos/{repo}/traffic/{kind}", traffic_token)
                upsert(os.path.join(out, f"{kind}.csv"), ["date", "count", "uniques"], ["date"], [
                    {"date": d["timestamp"][:10], "count": d["count"], "uniques": d["uniques"]}
                    for d in data[kind]
                ])
            refs = get(f"/repos/{repo}/traffic/popular/referrers", traffic_token)
            upsert(os.path.join(out, "referrers.csv"), ["date", "referrer", "count", "uniques"],
                   ["date", "referrer"],
                   [{"date": today, "referrer": r["referrer"], "count": r["count"], "uniques": r["uniques"]}
                    for r in refs])
            traffic_ok = True
        except (urllib.error.URLError, KeyError, ValueError) as e:
            print(f"::warning::Traffic API failed ({getattr(e, 'code', e)}); check TRAFFIC_TOKEN has Administration: read-only")
    else:
        print("::notice::TRAFFIC_TOKEN not set; skipping views, clones and referrers")

    write_summary(out, repo, stamp, traffic_ok)


def write_summary(out, repo, stamp, traffic_ok):
    best = {}  # asset_id -> (tag, max count)
    for r in read_rows(os.path.join(out, "downloads.csv")):
        n = int(r["download_count"])
        if r["asset_id"] not in best or n > best[r["asset_id"]][1]:
            best[r["asset_id"]] = (r["tag"], n)
    per_tag = defaultdict(int)
    for tag, n in best.values():
        per_tag[tag] += n
    total = sum(per_tag.values())

    repo_rows = read_rows(os.path.join(out, "repo.csv"))
    latest = repo_rows[-1] if repo_rows else {}
    week_ago = (dt.date.fromisoformat(latest["date"]) - dt.timedelta(days=7)).isoformat() if latest else ""
    prev = next((r for r in reversed(repo_rows) if r["date"] <= week_ago), None)

    lines = [
        f"# Deepr stats",
        "",
        f"Updated {stamp} by the `Stats snapshot` workflow on [{repo}](https://github.com/{repo}).",
        "Collected from GitHub's APIs only; the Deepr app contains no tracking.",
        "",
        "| Metric | Value |",
        "| --- | --- |",
        f"| Total APK downloads (all releases) | {total} |",
        f"| Stars | {latest.get('stars', '?')}"
        + (f" ({int(latest['stars']) - int(prev['stars']):+d} in 7 days)" if prev else "") + " |",
        f"| Forks | {latest.get('forks', '?')} |",
        f"| Open issues and PRs | {latest.get('open_issues', '?')} |",
    ]
    if traffic_ok:
        views = read_rows(os.path.join(out, "views.csv"))[-14:]
        lines.append(f"| Repo views, last 14 days | {sum(int(v['count']) for v in views)} "
                     f"({sum(int(v['uniques']) for v in views)} unique-day visitors) |")
    lines += ["", "## Downloads by release", "", "| Release | Downloads |", "| --- | --- |"]
    lines += [f"| {tag} | {n} |" for tag, n in sorted(per_tag.items(), key=lambda kv: -kv[1])]
    if traffic_ok:
        refs = [r for r in read_rows(os.path.join(out, "referrers.csv")) if r["date"] == stamp[:10]]
        if refs:
            lines += ["", "## Top referrers (last 14 days)", "", "| Site | Views | Unique |", "| --- | --- | --- |"]
            lines += [f"| {r['referrer']} | {r['count']} | {r['uniques']} |"
                      for r in sorted(refs, key=lambda r: -int(r["count"]))]
    lines += ["", "Raw data: `downloads.csv`, `repo.csv`"
              + (", `views.csv`, `clones.csv`, `referrers.csv`" if traffic_ok else "") + "."]
    with open(os.path.join(out, "README.md"), "w") as f:
        f.write("\n".join(lines) + "\n")


if __name__ == "__main__":
    main()
