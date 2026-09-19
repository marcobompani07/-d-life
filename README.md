# 🌱 d-life — Multi-threaded 2D Ecosystem Simulation

A real-time, multi-threaded life-simulation written in **Java + JavaFX**.  
Autonomous creatures roam a 2D world, hunt food, fight each other, reproduce, mutate, and die — all driven by a decoupled brain/body architecture and a thread-safe spatial grid.

Made by **Marco Bompani** and **Edward K Aiddo**

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Tech Stack & Build](#2-tech-stack--build)
3. [Architecture at a Glance](#3-architecture-at-a-glance)
4. [World & Coordinate System](#4-world--coordinate-system)
5. [Spatial Grid — ThreadSafeBackgroundGrid](#5-spatial-grid--threadsafebackgroundgrid)
6. [Thread-Safe Collections](#6-thread-safe-collections)
7. [Worker Thread Architecture](#7-worker-thread-architecture)
8. [How the Simulation Tick Works](#8-how-the-simulation-tick-works)
9. [CreatureBrain — The Decision Engine](#9-creaturebrain--the-decision-engine)
10. [Creature — State & Execution](#10-creature--state--execution)
11. [Food System](#11-food-system)
12. [Combat](#12-combat)
13. [Hunger & Starvation](#13-hunger--starvation)
14. [Reproduction & Mutation](#14-reproduction--mutation)
15. [Rendering Pipeline (JavaFX Thread)](#15-rendering-pipeline-javafx-thread)
16. [Save & Load System](#16-save--load-system)

---

## 1. Project Overview

d-life simulates an evolving population of creatures inside a bounded 2D world.  
Each creature is an autonomous agent that perceives its surroundings, decides what to do next, and executes that action — all while sharing state safely with other threads.

Key simulation dynamics:

| Mechanic     | Effect                                                  |
| ------------ | ------------------------------------------------------- |
| Movement     | Consumes hunger every step                              |
| Eating food  | Reduces hunger, restores HP                             |
| Combat       | Costs hunger, deals damage                              |
| Starvation   | Death at `hunger >= 100`; performance degrades above 60 |
| Reproduction | Spawns mutated offspring; costly to the parent          |
| Mutation     | Stats drift each generation (HP, attack, speed, color)  |

---

## 2. Tech Stack & Build

| Item          | Detail                   |
| ------------- | ------------------------ |
| Language      | Java 11                  |
| UI            | JavaFX 26.0.1            |
| Serialization | Jackson Databind 2.19    |
| Build         | Maven (`sample/pom.xml`) |
| Entry point   | `org.openjfx.App`        |

**Run:**

```bash
cd sample
mvn javafx:run
```

---

## 3. Architecture at a Glance

```
┌────────────────────────────────────────────────────────┐
│                    JavaFX UI Thread                    │
│  App (stage, controls)  ←── Platform.runLater() ───┐  │
│  MapGraphicsHandler (canvas draw)                   │  │
│  CreatureInfoDisplayThread → UpdateCreatureDisplay  │  │
└────────────────────────────────────────────────────┼──┘
                                                     │
              MovmentHandlingThread ─────────────────┘
              (snapshot → Platform.runLater)
                    │
        ┌───────────┴──────────────────┐
        │  ThreadSafeCreaturesArray    │
        │  ThreadSafeFoodArray         │  ← shared simulation state
        │  ThreadSafeBackgroundGrid    │
        └──────────┬───────────────────┘
                   │ request / release
        ┌──────────┴───────────────────────────────────────┐
        │  CreatureWorker × N  (one per CPU core − 2)      │
        │    └─ CreatureActionHandler                      │
        │         └─ creature.update()                     │
        │              ├─ findClosestFood()                │
        │              ├─ findClosestCreature()            │
        │              ├─ CreatureBrain.think()  → action  │
        │              └─ execute action (move/eat/fight…) │
        └──────────────────────────────────────────────────┘

        FoodGeneratorThread  (runs once per second, refills food)
```

---

## 4. World & Coordinate System

All positions are expressed in **world units** (doubles):

```java
// App.java
static final double WORLD_MULTIPLIYER = 10;
static final double WORLD_WIDTH  = 1000 * WORLD_MULTIPLIYER;  // 10 000 units wide
static       double WORLD_HEIGHT = <derived from screen height>;
static final int    UNIT_DIVISION = 1000;   // px-to-unit scaling base
```

Creatures and food are rectangular/point entities within `[0, WORLD_WIDTH) × [0, WORLD_HEIGHT)`.  
Movement is clamped so that no entity can leave the world boundary.

The **spatial grid** divides this world into 10-unit cells:

```
grid dimensions = (WORLD_WIDTH+1)/10  ×  (WORLD_HEIGHT+1)/10
```

The on-screen **canvas** occupies 75 % of the primary monitor's width/height.  
A `standardUnit` scalar maps world units to canvas pixels.  
Zoom (10 %–500 %) and drag-pan are supported in the UI.

---

## 5. Spatial Grid — ThreadSafeBackgroundGrid

`BackgroundGridElement` is the atom of the grid. Each cell stores two IDs:

```java
private int foodId;      // -1 = empty
private int creatureId;  // -1 = empty
```

`ThreadSafeBackgroundGrid` wraps the raw `BackgroundGridElement[][]` and exposes synchronized operations:

| Method                                     | Description                                                                         |
| ------------------------------------------ | ----------------------------------------------------------------------------------- |
| `addCreature(x,y,w,h,id)`                  | Validates that all covered cells are free, then stamps the ID                       |
| `moveCreature(oldX,oldY,newX,newY,w,h,id)` | Atomically checks target cells, clears old, stamps new — returns `false` if blocked |
| `removeCreature(x,y,w,h,id)`               | Clears cells that still carry this creature's ID                                    |
| `getCreature(x,y)`                         | Returns the creature ID at a grid cell (synchronized read)                          |
| `isFree(x,y,id)`                           | True if the cell is empty or already belongs to this creature                       |

**Why this matters:** `moveCreature` is the gatekeeper of all creature movement. If the target cells are occupied, the move is rejected and `Creature.update()` reacts by reversing direction. This prevents overlapping entities without any per-creature locking.

A creature's physical footprint spans multiple grid cells because its `width` and `height` are both 10 units (one full cell each dimension, but the stamping loop covers `ceil((pos+size)/10) − 1` cells to handle fractional overlap).

---

## 6. Thread-Safe Collections

### ThreadSafeCreaturesArray

```
Creature[] creatures        // indexed by creature ID
boolean[]  requestedCreatures  // mutex flags
```

Two access modes are available:

| Method                                 | Semantics                                                                                                                                     |
| -------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `request(i)`                           | **Blocking exclusive lock** — waits with `wait()` if already held. Used by `CreatureActionHandler` before calling `update()`.                 |
| `getCreature(i)`                       | **Non-blocking read** — returns the reference directly (synchronized, but no ownership flag). Used by nearby creatures scanning for a target. |
| `release(i)`                           | Clears the flag and calls `notifyAll()`.                                                                                                      |
| `addCreature(c)` / `removeCreature(i)` | Synchronized writes that also call `notifyAll()`.                                                                                             |
| `getAvailableId()`                     | Scans for the first `null` slot — used during reproduction.                                                                                   |

> **Design note:** Two creatures scanning each other simultaneously would deadlock if both used `request()`. Using the lightweight `getCreature()` for observation avoids this.

### ThreadSafeFoodArray

Same request/release pattern as creatures, but for `Food[]`:

| Method                | Semantics                                            |
| --------------------- | ---------------------------------------------------- |
| `request(i)`          | Blocking exclusive lock                              |
| `requestAdd(i, food)` | Internally calls `request` → writes → `release`      |
| `requestRemove(i)`    | Internally calls `request` → sets `null` → `release` |
| `release(i)`          | Releases the lock                                    |

---

## 7. Worker Thread Architecture

```
WORKER_COUNT = Runtime.getRuntime().availableProcessors() − 2
MAX_CREATURES = 10 000
```

Each worker is a `CreatureWorker extends Thread` that loops indefinitely:

```
while (running):
    actionHandler.updateCreature()
```

`CreatureActionHandler` holds a **round-robin index**:

```java
// simplified
public void updateCreature() throws InterruptedException {
    Creature creature = creatureArray.request(creatureIndex);
    if (creature != null) creature.update();
    creatureArray.release(creatureIndex);
    creatureIndex = updateCounter.getNext();   // advances the shared counter
}
```

`ThreadSafeCreatureUpdateCounter` distributes indices among workers:

- Each call to `getNext()` returns the next index (0 … MAX−1) atomically.
- When the counter wraps back to 0 it enforces a **minimum tick interval of 10 ms** with `Thread.sleep()`, so the simulation never spins faster than 100 ticks/second regardless of CPU speed.

This means all `N` worker threads share one counter and organically divide the creature array among themselves without any explicit work-stealing queue.

---

## 8. How the Simulation Tick Works

One call to `Creature.update()` is one "frame" for that creature. Here is the full sequence:

```
1.  Guard: hunger >= 100 OR hp <= 0  →  die and return
2.  Compute weightedSpeed & weightedAttack
        if hunger >= 60:
            hungerEffect = (hunger − 60) / 40
            weightedSpeed  = speed  × (1 − hungerEffect)
            weightedAttack = attack × (1 − hungerEffect)
3.  findClosestFood()   — scans grid cells within view radius
4.  findClosestCreature() — scans grid cells within view radius
5.  CreatureBrain.think(food, creature, stats, self) → action int
6.  switch(action):
        EAT            → eatFood()
        MOVE_TO_FOOD   → move toward food, hunger++, try eatFood()
        MOVE_TO_CREATURE → move toward brain's persistent target, hunger++
        ATTACK         → takeDamage() on target (cooldown-gated), hunger++
        FLEE           → move away from target, perpendicular bounce on wall
        RANDOM_MOVEMENT → wander (1 % chance to pick new angle), bounce on walls
        REPRODUCE      → hunger += 50, hp /= 2, call reproduct()
7.  Death check after hunger mutation
```

**Hunger cost per move:**

```java
this.setHunger(this.getHunger() + this.getSpeed() / 100);
```

**Attack hunger cost:**

```java
this.setHunger(this.getHunger() + this.getBaseAttack() * 2);
```

---

## 9. CreatureBrain — The Decision Engine

`CreatureBrain` is a stateful decision object (one instance per creature). It owns the **persistent creature target** and implements a **priority chain** evaluated on every `think()` call.

### Action Constants

| Constant           | Value | Meaning                                 |
| ------------------ | ----- | --------------------------------------- |
| `RANDOM_MOVEMENT`  | 0     | Wander with occasional direction change |
| `MOVE_TO_FOOD`     | 1     | Walk toward the nearest detected food   |
| `MOVE_TO_CREATURE` | 2     | Pursue the current target creature      |
| `ATTACK`           | 3     | Strike the target creature (in range)   |
| `EAT`              | 4     | Consume adjacent food                   |
| `FLEE`             | 5     | Run away from the current target        |
| `REPRODUCE`        | 6     | Spawn a child and pay the energy cost   |

### Decision Priority Chain (top = highest priority)

```
think(closestFoodData, closestCreatureData, x, y, view, attackRange, hunger, hp, maxHp, self)
│
├─ 1. Target hygiene
│      if target.hp <= 0           → clear target
│      if target distance > view×10 → clear target
│      if target == null && closestCreature alive → adopt closestCreature as target
│
├─ 2. EAT
│      if closestFood != null && foodDistance <= FOOD_WIDTH/2  →  return EAT
│
├─ 3. Emergency survival
│      isLowHp      = hp <= maxHp × 0.60
│      isTooHungry  = hunger >= 75.0
│      if (isLowHp || isTooHungry):
│          if food visible     →  return MOVE_TO_FOOD
│          elif target alive   →  return FLEE
│
├─ 4. Hunger maintenance
│      if food visible && (hunger >= 40 || no target)  →  return MOVE_TO_FOOD
│
├─ 5. Reproduction check
│      cooldownReady      = (now − lastReproductionTime) >= 10 000 ms
│      wellFedAndHealthy  = hunger < 30 && hp >= maxHp × 0.80
│      if cooldownReady && wellFedAndHealthy && random <= reproductionRate:
│          if no target OR target in range (distance <= attackRange×10)  →  return REPRODUCE
│          else                                                           →  return MOVE_TO_CREATURE
│
├─ 6. Combat / pursuit
│      if target alive:
│          if distance <= attackRange×10   →  return ATTACK
│          else                           →  return MOVE_TO_CREATURE
│
└─ 7. Default  →  return RANDOM_MOVEMENT
```

### Persistent Target

- The brain holds a `Creature creatureTarget` field.
- It is set to the nearest visible creature when currently `null`.
- It is cleared when the target's HP drops to zero or the target wanders beyond `view × 10` world units.
- This means a creature does **not** switch targets every frame; it commits to one opponent until that opponent dies or escapes.

The `Creature.update()` side also clears `brain.setCreatureTarget(null)` after an ATTACK kills the target or after REPRODUCE, preventing stale references.

---

## 10. Creature — State & Execution

### Fields (per instance)

| Field                   | Type     | Default            | Description                                        |
| ----------------------- | -------- | ------------------ | -------------------------------------------------- |
| `id`                    | `int`    | assigned           | Unique ID = index in creatures array               |
| `x`, `y`                | `double` | random             | Position (world units)                             |
| `width`, `height`       | `double` | 10                 | Bounding box (world units)                         |
| `color`                 | `Color`  | random             | Rendered fill color                                |
| `speed`                 | `double` | random [0.2, 1.2]  | Base movement per tick                             |
| `hunger`                | `double` | 0                  | 0 (full) → 100 (dead)                              |
| `hp`                    | `double` | random [100, 200]  | Current hit points                                 |
| `maxHp`                 | `double` | same as initial hp | Maximum hit points                                 |
| `baseAttack`            | `double` | random [1, 11]     | Damage per hit                                     |
| `view`                  | `int`    | 5                  | Vision radius in grid cells (50 world units)       |
| `attackRange`           | `int`    | 1                  | Melee threshold multiplier (×10 world units)       |
| `hpRegenPercentage`     | `double` | 0.15               | Fraction of maxHp restored when eating             |
| `reproductionRate`      | `double` | 0.1                | Probability of reproducing when conditions are met |
| `lastReproductionTime`  | `long`   | init time          | ms timestamp for cooldown                          |
| `REPRODUCTION_COOLDOWN` | `long`   | 10 000 ms          | Minimum time between reproductions (static)        |
| `ATTACK_COOLDOWN`       | `long`   | 500 ms             | Minimum time between attacks (static)              |

All getters/setters are `synchronized` because multiple worker threads may read a creature's state while another thread holds the exclusive update lock.

### Key Methods

#### `findClosestFood()`

```
gridX = x/10 , gridY = y/10
scan cells [gridX−view … gridX+view] × [gridY−view … gridY+view]
for each cell with food:
    food = foodArray.request(foodId)   ← blocking lock
    distance = from food point to closest edge of creature rectangle
    track minimum
    foodArray.release(foodId)
return {closestFood, closestDistance}
```

Distance is measured from the **food point** to the **nearest point on the creature's rectangle** (not from the creature's top-left corner), giving accurate proximity even for large bounding boxes.

#### `findClosestCreature()`

```
scan cells [startX−view … endX+view] × [startY−view … endY+view]
for each cell with a creature ID != self:
    creature = creatureArray.getCreature(id)   ← non-blocking read
    distanceX = axis-aligned gap between rectangles on X
    distanceY = axis-aligned gap between rectangles on Y
    distance  = sqrt(distanceX² + distanceY²)
    if distance <= view × 10: track minimum
return {closestCreature, closestDistance}
```

Rectangle-to-rectangle gap distance means two creatures touching are at distance 0, matching intuitive physical proximity.

#### `eatFood(food, foodId, newX, newY)`

```
closestX = clamp(food.x, newX, newX+width)    ← nearest point on creature rect
closestY = clamp(food.y, newY, newY+height)
distanceToFood = euclidean(food − closest point)

if distanceToFood <= FOOD_WIDTH/2:
    hunger  -= 20
    hunger   = max(0, hunger)
    hp       = min(maxHp, hp + maxHp × hpRegenPercentage)   // +15% maxHp
    backgroundGrid[foodGridX][foodGridY].setFood(-1)
    foodArray.requestRemove(foodId)
```

#### `move(newX, newY)`

```
moved = threadSafeBackgroundGrid.moveCreature(oldX, oldY, newX, newY, w, h, id)
if moved:
    this.x = newX
    this.y = newY
return moved
```

The creature's position is **only updated if the grid accepts the move**. On failure the caller reverses direction.

#### `takeDamage(damage)` ← `synchronized`

```
hp -= damage
if hp <= 0:
    grid.removeCreature(...)
    creatureArray.removeCreature(id)
    App.removeCreature(id)
    return true
return false
```

#### `reproduct()` — called on REPRODUCE action

```
newId = creatureArray.getAvailableId()   // scan for null slot
spawnX, spawnY = parent position + random offset [15, 40] units
clamp to world bounds
childHp     = mutate(maxHp,    0.15 × MutationRate, [50, 500])
childAttack = mutate(baseAtk,  0.15 × MutationRate, [1, 25])
childSpeed  = mutate(speed,    0.10 × MutationRate, [0.2, 3])
childColor  = mutateColor(color, 0.02 × MutationRate)
if grid.addCreature(spawnX, spawnY, 10, 10, newId):
    creatureArray.addCreature(child)
    App.addCreature()
```

---

## 11. Food System

### Food Entity

```java
class Food {
    int id, categoryId;
    int x, y;               // world units (integers)
    static final int FOOD_WIDTH  = 10;
    static final int FOOD_HEIGHT = 10;
}
```

### FoodGeneratorThread

- Runs as a dedicated thread.
- **Every second** (padded with `Thread.sleep`), it generates up to `FOODSPAWNCOUNT = 100` new food items.
- For each empty slot in `ThreadSafeFoodArray`, it picks a random free grid cell, places a `Food` object, and stamps the cell's `foodId`.
- Food persists until a creature eats it.

### Eating sequence (Creature side)

1. `findClosestFood()` locates nearest food within `view` cells.
2. Brain returns `EAT` (if touching) or `MOVE_TO_FOOD` (if approaching).
3. `eatFood()` is called:
    - On `MOVE_TO_FOOD`: called with `newX, newY` (position after the step).
    - On `EAT`: called with current position.
4. If within `FOOD_WIDTH/2` (5 units): hunger drops 20, HP restored 15 % of maxHp, food removed from grid and array.

---

## 12. Combat

1. Brain returns `ATTACK` when `creatureTarget` is alive and `distance <= attackRange × 10`.
2. `Creature.update()` checks the `ATTACK_COOLDOWN = 500 ms` since last attack.
3. If cooldown elapsed:
    - `target.takeDamage(weightedAttack)` — synchronized, may kill the target instantly.
    - Attacker pays `hunger += baseAttack × 2`.
    - If target is now dead, `brain.setCreatureTarget(null)`.
4. After attack, if the attacker's own hunger reaches 100, it also dies.

**Attack reach formula:**

```
attackRange × 10   (world units)
```

For the default `attackRange = 1` this means melee contact (10 units ≈ one creature width).

---

## 13. Hunger & Starvation

Hunger is a `double` in `[0, 100]`.

| Threshold | Effect                                                          |
| --------- | --------------------------------------------------------------- |
| 0         | Fully satiated                                                  |
| ≥ 40      | Brain considers seeking food even when a target exists          |
| ≥ 60      | Performance penalty begins: `effect = (hunger − 60) / 40`       |
| ≥ 75      | Emergency: brain prioritizes food over combat, flees if no food |
| = 100     | Death — removed from grid and creature array                    |

**Performance under starvation:**

```
weightedSpeed  = speed  × (1 − (hunger − 60) / 40)
weightedAttack = attack × (1 − (hunger − 60) / 40)
```

At hunger = 100 both would be zero, but the death check at the start of `update()` prevents the creature from ever reaching that point alive.

---

## 14. Reproduction & Mutation

### Trigger Conditions (all must be true)

| Condition        | Value                                                    |
| ---------------- | -------------------------------------------------------- |
| Cooldown elapsed | `(now − lastReproductionTime) >= 10 000 ms`              |
| Low hunger       | `hunger < 30.0`                                          |
| High HP          | `hp >= maxHp × 0.80`                                     |
| Random roll      | `Math.random() <= reproductionRate` (default 0.1 = 10 %) |

If a target creature is present, the creature moves toward it first; it reproduces when within `attackRange × 10` units (simulating pair bonding). If no target is present, reproduction happens asexually on the spot.

### Parent Energy Cost

```java
this.setHunger(this.getHunger() + 50);   // significant hunger spike
this.setHp(this.getHp() / 2);           // HP halved
```

### Offspring Mutation

```java
// mutate(base, factor, min, max)
change = (random[−1,1] × factor) + 1.0
mutatedValue = clamp(base × change, min, max)
```

| Trait           | Mutation Factor                   | Range      |
| --------------- | --------------------------------- | ---------- |
| `maxHp`         | `0.15 × MutationRate`             | [50, 500]  |
| `baseAttack`    | `0.15 × MutationRate`             | [1, 25]    |
| `speed`         | `0.10 × MutationRate`             | [0.2, 3]   |
| `color (R/G/B)` | `0.02 × MutationRate` per channel | [0.0, 1.0] |

`App.MutationRate` (default `1.0`) is a global scalar that amplifies or reduces all mutation simultaneously.

The spawn point is placed at a random distance between 15 and 40 world units from the parent, clamped to world boundaries.

---

## 15. Rendering Pipeline (JavaFX Thread)

JavaFX prohibits UI updates from non-application threads. The simulation bridges this with a **rate-limited snapshot + `Platform.runLater`** approach:

```
MovmentHandlingThread (background thread, ~100 Hz target):
    if ThreadSafeUpdateMapQueueCounter.getCounter() < 1:
        counter.increaseCounter()
        snapshot all creatures → Creature[]   (request/release each)
        snapshot all food     → Food[]
        Platform.runLater(updateRunnableGenerator.generate(snapshot))

On JavaFX thread (UpdateGuiRunnableGenerator runnable):
    mapGraphicsHandler.update(creatures, foodArray)
    counter.decreaseCounter()
```

`ThreadSafeUpdateMapQueueCounter` ensures at most **one pending render call** is queued at a time. If the JavaFX thread hasn't finished the previous frame yet, `MovmentHandlingThread` skips the new frame and tries again after ~10 ms.

`MapGraphicsHandler.update()` on the JavaFX thread:

1. Clears the canvas.
2. Tiles a terrain background image.
3. Draws each creature as a filled rectangle in its color (scaled by `standardUnit × ZOOM`, offset by camera pan).
4. Draws each food item as a green filled oval.
5. If a creature is focused (`focusedCreatureId >= 0`), the camera auto-tracks it.

### Creature Info Panel (CreatureInfoDisplayThread)

A second background thread polls the selected creature every 100 ms and posts a `Platform.runLater` to update the right sidebar with: ID, color swatch, speed, hunger, HP/maxHP, base attack.

---

## 16. Save & Load System

Save is triggered by the **Save** button:

1. All simulation threads are stopped and joined.
2. Creature state is serialized to `CreatureSaveData[]` (Jackson-annotated POJO). Color is stored as separate `red`, `green`, `blue` doubles since `javafx.scene.paint.Color` is not directly serializable.
3. Food array, background grid, and creature count are also stored.
4. JSON is written to `src/main/resources/saves/<name>.json`.
5. All threads are restarted.

Load is symmetric: Jackson deserializes the JSON, reconstructs `Creature[]` from `CreatureSaveData[]`, and restores `lastReproductionTime` via a stored offset (`lastReproductionTimeOffset = now − lastReproductionTime` at save time).
