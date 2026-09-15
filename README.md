# The Campus Puzzle

Java implementation of the M603 Advanced Algorithms individual project: a four-stage university timetabler that produces a best-effort semester schedule under room, professor and student-group constraints.

**Repository:** https://github.com/devsruthi/the-campuz-puzzle-algorithm-project

## Project layout

```
data/constraints.json
src/Main.java
src/GreedySolver.java
src/GraphEngine.java
src/Optimizer.java
src/Backtracker.java
src/DataLoader.java
src/JsonParser.java
src/Course.java
src/Room.java
src/Booking.java
```

## How to run

```bash
javac -d out src/*.java
java -cp out Main
```

Optional path to a constraints file:

```bash
java -cp out Main data/constraints.json
```

## Input (`data/constraints.json`)

| Resource | Count |
| --- | --- |
| Classes | 16 |
| Rooms | 7 (40–260 seats) |
| Timeslots | 5 (Mon–Fri 09:00) |
| Student groups | 5 |

Six classes enrol 230 students, but only **R-101 (260 seats)** can host them. With five timeslots the pigeonhole principle forbids a complete timetable.

## Algorithm justification

### Stage 1 — Greedy baseline (`GreedySolver.java`)

Classes are sorted by enrolment, largest first, then placed in the first free room-and-slot pair that has enough seats.

We used **first-fit decreasing** because its **O(C log C + C · T · R)** time lets the solver scale to hundreds of classes, while still protecting the scarce large halls. The baseline does **not** check student-group or professor clashes; it is a packing start, not a publishable timetable.

On this instance: **15 / 16** booked, **510** empty seats, **WEB01** left unscheduled. DSA101, MATH101 and JAVA101 all land on Monday — illegal for Year1_CS and Year1_DS.

### Stage 2 — Conflict graph and Welsh–Powell colouring (`GraphEngine.java`)

Each class is a vertex. An edge is added when two classes share a professor or a student group. Timeslots are colours. Vertices are coloured highest-degree-first (Welsh–Powell).

We used **graph colouring** because **O(C²)** adjacency work prevents student “two places at once” conflicts *before* rooms are assigned, and that cost still scales to a few hundred modules.

On this instance: **16 vertices, 15 edges**, **16 / 16** classes receive a legal hour using three colours. Isolated 230-student lectures all receive Monday, so nine classes share seven rooms — the room shortage is deferred to Stage 3.

### Stage 3 — Dynamic programming room allocation (`Optimizer.java`)

For each timeslot independently, rooms are assigned to minimise empty seats. State `dp[i][S]` is the minimum waste for classes `i…n−1` when the free-room set is bitmask `S`. A class may be skipped at a large penalty so the hour can still complete.

We used **DP** because **O(n · 2^m · m)** per hour (exponential only in the number of rooms *m*) avoids enumerating `m!` matchings, and with *m* ≤ 7 (`2^7 = 128`) it stays practical even when the hall stock grows toward the brief’s 50 rooms (the code falls back to greedy if *m* > 16).

On this instance: **12 / 16** placed, empty seats **370** (down from 510). Leftovers: STAT01, NET01, OS01, SEC01.

### Stage 4 — Best-effort backtracking (`Backtracker.java`)

Leftover classes are tried on any remaining legal (hour, room) pair. Search is pruned by capacity, occupancy, professor/group clashes, a cardinality bound, and a 4,000-node cap. The champion is the largest legal partial timetable.

We used **bounded backtracking** because a full search is exponential in leftovers *k*, but here *k* is 4, pruning keeps the tree finite, and the output is a guaranteed-legal partial schedule rather than a crash.

On this instance: recovers STAT01, NET01 and OS01. **15 / 16 (93.8%)** scheduled.

## Conflict report

```
Scheduled    AI101       Mon-09:00     R-104   Wasted 120 seats
Scheduled    AI102       Mon-09:00     R-107   Perfect Fit
Scheduled    DB01        Mon-09:00     R-105   Wasted 30 seats
Scheduled    DS01        Mon-09:00     R-106   Wasted 10 seats
Scheduled    DSA101      Tue-09:00     R-101   Wasted 30 seats
Scheduled    DSA102      Tue-09:00     R-103   Wasted 50 seats
Scheduled    JAVA101     Wed-09:00     R-103   Wasted 10 seats
Scheduled    MATH101     Wed-09:00     R-102   Wasted 10 seats
Scheduled    MATH102     Wed-09:00     R-104   Wasted 20 seats
Scheduled    NET01       Thu-09:00     R-101   Wasted 30 seats
Scheduled    OS01        Fri-09:00     R-101   Wasted 30 seats
Scheduled    PM101       Wed-09:00     R-105   Wasted 10 seats
Scheduled    PYTHON101   Tue-09:00     R-104   Wasted 50 seats
Scheduled    STAT01      Wed-09:00     R-101   Wasted 30 seats
Scheduled    WEB01       Mon-09:00     R-101   Wasted 30 seats
Unscheduled  SEC01       N/A           N/A
```

**SEC01:** only 1 hall seats 230 students, and it is already taken in every timeslot.

## Manual-fix log

A university manager would use the leftover list as follows:

1. **Add capacity** — a second 230-seat hall, or a sixth timeslot, raises large-lecture bins from five to six and the pigeonhole obstruction disappears.
2. **Split SEC01** — two groups of 115 fit in R-103 / R-104 and stop competing for R-101.
3. **Spread isolated lectures** — recolour degree-0 vertices across Friday instead of stacking them on Monday, so R-101 is not exhausted before backtracking starts.

Until one of those changes is made, publish the 15-class timetable and flag **SEC01** for manual intervention.
