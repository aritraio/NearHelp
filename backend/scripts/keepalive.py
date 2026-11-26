"""Periodic keep-alive pinger for the NearHelp Render deployment."""

from __future__ import annotations

import argparse
import sys
import time
import urllib.error
import urllib.request
from datetime import UTC, datetime

DEFAULT_HEALTH_URL = "https://nearhelp-backend-7sfj.onrender.com/health"


def ping_once(url: str, timeout: float = 65.0) -> bool:
    """Send a single GET request to the health endpoint and print the outcome."""
    timestamp = datetime.now(UTC).strftime("%Y-%m-%d %H:%M:%S UTC")
    req = urllib.request.Request(url, method="GET", headers={"User-Agent": "NearHelp-KeepAlive/1.0"})
    try:
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            body = resp.read(256).decode("utf-8", errors="replace").strip()
            print(f"[{timestamp}] {resp.status} OK -> {body}", flush=True)
            return 200 <= resp.status < 300
    except urllib.error.HTTPError as exc:
        print(f"[{timestamp}] HTTP {exc.code}: {exc.reason}", file=sys.stderr, flush=True)
        return False
    except urllib.error.URLError as exc:
        print(f"[{timestamp}] Request error: {exc.reason}", file=sys.stderr, flush=True)
        return False


def main() -> int:
    parser = argparse.ArgumentParser(description="Keep NearHelp Render backend awake via /health pings.")
    parser.add_argument("--url", default=DEFAULT_HEALTH_URL, help="Health endpoint URL to ping.")
    parser.add_argument("--interval", type=int, default=300, help="Seconds between pings (default: 300).")
    parser.add_argument("--once", action="store_true", help="Send a single ping and exit.")
    args = parser.parse_args()

    if args.interval < 30:
        parser.error("--interval must be at least 30 seconds.")

    if args.once:
        return 0 if ping_once(args.url) else 1

    print(f"Starting keep-alive pings to {args.url} every {args.interval}s (Ctrl+C to stop)...", flush=True)
    try:
        while True:
            ping_once(args.url)
            time.sleep(args.interval)
    except KeyboardInterrupt:
        print("\nKeep-alive stopped.")
        return 0


if __name__ == "__main__":
    raise SystemExit(main())
