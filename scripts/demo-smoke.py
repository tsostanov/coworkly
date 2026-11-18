#!/usr/bin/env python3
"""Exercise the documented demo against a running backend + real PostgreSQL.

Usage: python scripts/demo-smoke.py [--base-url http://localhost:8081]
Requires the backend's demo profile; uses only Python's standard library.
"""
import argparse
import json
import time
from datetime import datetime, timedelta, timezone
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode
from urllib.request import Request, urlopen


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--base-url", default="http://localhost:8081")
    args = parser.parse_args()

    def api(path, method="GET", data=None, token=None, expected=200):
        headers = {"Content-Type": "application/json"}
        if token:
            headers["Authorization"] = "Bearer " + token
        request = Request(args.base_url + "/api" + path, method=method, headers=headers,
                          data=json.dumps(data).encode() if data is not None else None)
        try:
            with urlopen(request, timeout=10) as response:
                status, body = response.status, response.read()
        except HTTPError as error:
            status, body = error.code, error.read()
        if status != expected:
            raise RuntimeError(f"{method} {path}: expected {expected}, got {status}: {body.decode()}")
        return json.loads(body) if body else None

    # Wait for both the HTTP server and demo CommandLineRunner to be ready.
    deadline = time.monotonic() + 120
    while True:
        try:
            admin = api("/auth/login", "POST", {"email": "admin@coworkly.test", "password": "DemoAdmin123!"})
            break
        except (URLError, RuntimeError, TimeoutError):
            if time.monotonic() >= deadline:
                raise
            time.sleep(1)
    resident = api("/auth/login", "POST", {"email": "resident@coworkly.test", "password": "DemoResident123!"})
    assert admin["role"] == "ADMIN" and resident["role"] == "RESIDENT"
    at, rt = admin["token"], resident["token"]
    locations = api("/locations", token=rt)
    location = next(item for item in locations if item["name"] == "\u0426\u0435\u043d\u0442\u0440")
    spaces = api(f"/spaces/location/{location['id']}", token=rt)
    desks = {item["id"] for item in spaces if item["type"] == "OPEN_DESK"}

    # A fresh slot also makes subsequent runs possible without deleting data.
    for offset in range(1, 31):
        start = datetime.now(timezone.utc).replace(microsecond=0) + timedelta(days=offset)
        end = start + timedelta(hours=2)
        query = urlencode({"locationId": location["id"], "from": start.isoformat(), "to": end.isoformat()})
        free = api("/spaces/free?" + query, token=rt)
        desk = next((item for item in free if item["spaceId"] in desks), None)
        if desk:
            break
    else:
        raise RuntimeError("No free demo desk found")

    created = api("/bookings", "POST", {
        "userId": resident["userId"], "spaceId": desk["spaceId"],
        "startsAt": start.isoformat(), "endsAt": end.isoformat(),
    }, rt, 201)
    booking_id = created["bookingId"]
    rows = api(f"/bookings/user/{resident['userId']}", token=rt)
    booking = next(item for item in rows if item["id"] == booking_id)
    assert booking["status"] == "PENDING" and booking["totalCents"] == 8000
    checkin = f"/admin/visits/checkin?bookingId={booking_id}"
    api(checkin, "POST", token=rt, expected=403)
    api(checkin, "POST", token=at, expected=400)
    api(f"/bookings/{booking_id}/confirm", "POST", token=at, expected=204)
    visit = api(checkin, "POST", token=at)
    assert visit["bookingId"] == booking_id and visit["userId"] == resident["userId"]
    assert visit["status"] == "ACTIVE" and visit["checkIn"]
    assert any(item["id"] == visit["id"] for item in api("/admin/visits/active", token=at))
    api(checkin, "POST", token=at, expected=400)
    completed = api(f"/admin/visits/{visit['id']}/checkout", "POST", token=at)
    assert completed["status"] == "COMPLETED" and completed["checkOut"]
    assert not any(item["id"] == visit["id"] for item in api("/admin/visits/active", token=at))
    print(json.dumps({"result": "PASS", "bookingId": booking_id, "visitId": visit["id"],
                      "checks": ["resident booking", "PostgreSQL price calculation", "role restrictions",
                                 "admin confirmation", "check-in", "duplicate rejected", "check-out"]}))


if __name__ == "__main__":
    main()
