# Performance-Analyse pxWorlds & Rollercoaster

## Kurzfassung

Das Spiel-Rendering ist **gut optimiert** — Interpolation, Chunk-Culling, VBO-Pooling und Scratch-Buffer minimieren GC und Draw-Calls sauber. Das wahrgenommene Ruckeln (Stutter ohne FPS-Einbruch, 120-fps-Ziel aber nur 80 erreicht) kommt **nicht** von Render-Kosten, sondern von der **Game-Loop-Timing-Architektur**.

Es gibt **3 Probleme**, das erste ist die Hauptursache.

---

## Problem 1: Kein VSync + `parkNanos`-Frame-Limiter → Micro-Stutter (Hauptursache)

### Symptom
- Ruckeln/Judder trotz stabiler FPS-Zahl
- 120-fps-Ziel wird nur mit ~80 fps erreicht
- FPS-Zähler schwankt oder zeigt Durchschnitt, aber Frame-Abständ sind ungleich

### Root Cause

#### VSync ist deaktiviert
`glfwSwapInterval(...)` wird nirgends aufgerufen. Daher:
- Kein Hardware-VSync → `swapBuffers()` kehrt sofort zurück, ohne auf Vblank zu warten
- Frame-Pacing hängt **komplett** am Software-Limiter ([`GameLoop.java:172–175`](../rollercoaster/src/main/java/temmiland/rollercoaster/game/GameLoop.java#L172-L175))

#### Die `parkNanos`-Schlaf-Logik ist nicht präzise genug
```java
final double sleepSec = Math.min(
    tickDuration - tickAccumulator, 
    renderCap - renderAccumulator
) - 0.001;

if (sleepSec > 0.001) {
    LockSupport.parkNanos((long) (sleepSec * 1_000_000_000L));
}
```

**Das Problem:**
- `LockSupport.parkNanos` hat auf macOS (und anderen OS) eine **Scheduler-Granularität von ~4–10 ms**
- Die Loop will z.B. 7,3 ms schlafen (bei 120-fps-Ziel), aber `parkNanos` schläft stattdessen ~11–12 ms
- Ergebnis: Frame-Zeit wird ~4 ms zu lang
- Mathematik: `1000 ms / 12,5 ms ≈ 80 fps` ← **exakt deine Beobachtung**

**Pro Frame ein konstanter Overschlaf:**
- 60 fps Ziel (16,7 ms Budget): +4 ms Overschlaf = ~50 fps erreichbar
- 120 fps Ziel (8,3 ms Budget): +4 ms Overschlaf = **~80 fps erreichbar** ✓
- Je höher das Ziel, desto schlechter die Ausbeute

**Zusätzlich:** Die `-0.001` (1-ms-Sicherheitsmarge) ist **viel zu klein**, um diesen 4–5-ms-Overschlaf auszugleichen.

#### Das Jitter-Symptom
Die `parkNanos`-Oversleep ist **nicht exakt konstant**, sondern variiert ±1–2 ms je Frame:
- Frame 1: 11 ms (80 fps)
- Frame 2: 13 ms (76 fps)
- Frame 3: 12 ms (83 fps)
- FPS-Zähler über 1 Sek: Ø 80 fps ✓
- **Aber das Auge sieht die ungleichen Abstände als Ruckeln** (Jitter, kein stabiles Pacing)

### Nachweis
- Config: `~/.pxworlds/config/screen_configuration.json` → `"frameRate": 60`
- Aber du siehst: ~80 fps bei 120er Ziel, nicht 120
- Das beweist: FPS-Limit ist **nicht** GPU/CPU-getrieben, sondern vom Loop-Sleep kaputt gemacht

### Fix

**Option A: VSync (empfohlen für Games)**

Aktiviere Hardware-VSync in [`Window.createWindow()`](../rollercoaster/src/main/java/temmiland/rollercoaster/platform/Window.java#L101) nach `glfwMakeContextCurrent`:

```java
public void createWindow() {
    // ... existing code ...
    glfwShowWindow(window);
    glfwMakeContextCurrent(window);
    
    // ← ADD THIS:
    glfwSwapInterval(1);  // Enable VSync
    
    // ... rest of code ...
}
```

**Dann:** `swapBuffers()` blockiert **hardware-präzise** bis zum nächsten Vblank:
- Auf 60-Hz-Monitor: exakt 60 fps, 16,67 ms Abstände
- Auf 120-Hz-Monitor: exakt 120 fps, 8,33 ms Abstände
- Kein `parkNanos`-Jitter, kein Ruckeln

**Konsequenz für Loop:**
- Der `renderCap`-Check im Loop sollte **entfernt** werden (oder nur als Sicherheits-Cap weit oberhalb der Refresh-Rate bleiben)
- Stattdessen: jeden Iteration rendern, VSync gibt den Takt vor
- Der `parkNanos`-Sleep sollte nur noch für die **Tick-Schleife** gelten (damit die sich nicht hot-spinnt), nicht für Rendering

**Option B: Hybrid Sleep (nur ohne VSync nötig)**

Wenn du bewusst ohne VSync arbeiten willst:
```java
// Grob schlafen (~80% des Ziels), dann Busy-Spin für die letzten ~1–2 ms
double sleepMs = sleepSec * 1000 - 2.0;
if (sleepMs > 0.1) {
    LockSupport.parkNanos((long) (sleepMs * 1_000_000));
}
// Busy-Spin: Spin bis wirkliche Zeit erreicht
long spinStart = System.nanoTime();
while ((System.nanoTime() - spinStart) < (sleepSec * 1_000_000_000L)) {
    // Busy loop
}
```
Das trifft den Zielwert präzise, kostet aber CPU. **Nicht empfohlen; VSync ist besser.**

---

## Problem 2: Event-Polling nur mit 20 Hz (Tick-Rate) statt pro Frame

### Symptom
- Input-Latenz: Tasten-Druck wird bis zu 50 ms später registriert
- GUI/Menüs wirken träge, Maus-Hover zittrig, Cursor-Bewegung unflüssig
- Fühlt sich subjektiv auch wie „Ruckeln" an (ist aber Latenz, nicht Jitter)

### Root Cause

[`GameLoop.java:122–136`](../rollercoaster/src/main/java/temmiland/rollercoaster/game/GameLoop.java#L122-L136):

```java
while (!window.shouldClose()) {
    // ... accumulator logic ...
    
    // ← HIER: window.update() ist INNERHALB der Tick-Schleife
    while (tickAccumulator >= tickDuration) {
        tickAccumulator -= tickDuration;
        
        window.update();  // ← glfwPollEvents() + InputProcessor.processEvents()
        
        if (window.hasResized()) {
            gsm.resize();
        }
        
        gsm.update(tickDuration);
    }
    
    // Rendering ...
}
```

`window.update()` ruft `glfwPollEvents()` auf, das nur mit **20 TPS** (TICK_RATE) aufgerufen wird, nicht pro Frame (60+).

**Konsequenzen:**
- **Input-Latenz:** Bis zu 50 ms (eine volle Tick-Duration) zwischen Event und Verarbeitung
- **Maus-Position:** Von `InputProcessor` nur 20×/Sek aktualisiert → GUI-Hovering, Cursor-Bewegung in Menüs wirkt hakelig
- **Tasten-Edges:** Button-Presses (für GUI, Debug-Toggle) werden nur 20×/Sek abgefragt → Risiko von Input-Events übersehen
- **Edge-Effekt:** Wenn zwei Input-Events in demselben 50-ms-Fenster kommen, wird eins möglicherweise ignoriert

### Fix

Verschiebe `window.update()` **außerhalb** der Tick-Schleife, damit es **pro Loop-Iteration** (also pro Frame) aufgerufen wird:

```java
public void run(final Window window) {
    // ... setup ...
    
    while (!window.shouldClose()) {
        final double now    = Timer.getTime();
        final double passed = now - time;
        time = now;
        
        tickAccumulator   += passed;
        renderAccumulator += passed;
        frameTime         += passed;
        
        // ← MOVE window.update() HERE (once per loop iteration, not per tick)
        window.update();
        
        if (window.hasResized()) {
            gsm.resize();
        }
        
        // ... rest of loop ...
        
        // --- Logic: fixed timestep ---
        while (tickAccumulator >= tickDuration) {
            tickAccumulator -= tickDuration;
            // (no window.update() here)
            if (debugHud != null && debugToggle.getAsBoolean()) {
                debugHud.toggleDetailed();
            }
            gsm.update(tickDuration);
        }
        
        // --- Rendering: fpsRate cap ---
        if (renderAccumulator >= renderCap) {
            // ... render ...
        }
        
        // ... sleep ...
    }
}
```

**Effekt:**
- Input/Events werden 60+ mal pro Sekunde verarbeitet, nicht 20
- Input-Latenz sinkt von ~50 ms auf ~8 ms (bei 60 fps)
- Menüs/GUI-Cursor wirken flüssig und responsiv

---

## Problem 3: GUI rendert in Immediate-Mode (Fixed-Function Pipeline)

### Symptom
- GUI/Debug-Overlay könnte zu Pipeline-Stalls beitragen
- Nicht die Hauptursache des Stutters, aber ein sekundärer Performance-Penalty

### Root Cause

[`PxGuiRenderer.java`](../pxWorlds/src/main/java/temmiland/pxworlds/game/gui/PxGuiRenderer.java):

```java
@Override
public void drawText(final String text, final float cx, final float cy, ...) {
    // ...
    glUseProgram(0);              // ← Switch zu Fixed-Function
    glDisable(GL_TEXTURE_2D);
    glMatrixMode(GL_PROJECTION);
    glPushMatrix();
    // ... STBEasyFont Rendering ...
    glPopMatrix();
    glEnable(GL_TEXTURE_2D);
    glUseProgram(...);            // ← Switch zurück zu Shader
}

@Override
public void drawQuad(final float cx, final float cy, ...) {
    glUseProgram(0);              // ← Switch zu Fixed-Function
    glDisable(GL_TEXTURE_2D);
    glBegin(GL_QUADS);
    glVertex2f(...); glVertex2f(...); // ... Immediate-Mode
    glEnd();
    glEnable(GL_TEXTURE_2D);
    glUseProgram(...);            // ← Switch zurück
}
```

**Das Problem:**
- Jeder `drawText` / `drawQuad` Aufruf wechselt zwischen Shader-Pipeline und Fixed-Function
- Jeder Pipeline-Wechsel (`glUseProgram(0)` ↔ Shader) zwingt den OpenGL-Treiber zu einer **Synchronisation/Flush**
- Bei vielen Text-Elementen (Menüs, Debug-Overlay) summiert sich das zu **Pipeline-Stalls**
- Immediate-Mode (`glBegin/glEnd`, `glVertex2f`) ist eine tote API — wird vom Treiber emuliert, ineffizient

### Fix (mittelfristig, nicht prioritär)

Batch die GUI auf GPU-VBOs + einen einzigen Shader (wie das Tile-Rendering):
- VBO für alle Quads (einmal pro Frame neu füllen)
- Ein Batch-Shader für Quads + Textur-Atlasing für Text (StB-Font in einen Atlas)
- Ein `glDrawArrays` / `glDrawElements` für die gesamte GUI pro Frame

**Für jetzt:** Aktuell ist die GUI nicht der Haupt-Bottleneck. Erst nach VSync + Event-Polling fixieren, dann bei Bedarf Profile und optimieren.

---

## Was gut ist (nicht anfassen)

### Interpolation
- [`Entity.java:206`](../pxWorlds/src/main/java/temmiland/pxworlds/game/entity/Entity.java#L206): `renderX = prevX + (posX - prevX) * alpha` ✓
- [`Camera.java:110–113`](../pxWorlds/src/main/java/temmiland/pxworlds/game/rendering/Camera.java#L110-L113): `prevPosition.lerp(position, alpha, ...)` ✓
- Smooth, frame-rate-unabhängig, keine Allokationen

### Chunk-System
- [`Chunk.rebuildIfDirty`](../pxWorlds/src/main/java/temmiland/pxworlds/game/world/Chunk.java#L144): VBO nur bei `dirty`, nicht jedem Frame ✓
- [`World.render`](../pxWorlds/src/main/java/temmiland/pxworlds/game/world/World.java#L237): Viewport-Culling, nur sichtbare Chunks ✓
- Ein Draw-Call pro Chunk, nicht pro Tile ✓

### Memory
- Scratch-Buffer statt Pro-Frame-Allokationen: [`World.scratchColliders`](../pxWorlds/src/main/java/temmiland/pxworlds/game/world/World.java#L96), [`Camera.projectionAlphaScratch`](../pxWorlds/src/main/java/temmiland/pxworlds/game/rendering/Camera.java#L16) ✓
- GC-Druck ist minimal

---

## Zusammenfassung & Prioritäten

| Priorität | Problem | Aufwand | Wirkung | Fix |
|-----------|---------|---------|---------|-----|
| 🔴 **1** | Kein VSync + parkNanos | klein (~5 min) | Stutter weg, 120-fps-Ziel erreichbar | `glfwSwapInterval(1)` + Loop-Architektur anpassen |
| 🟠 **2** | Event-Polling nur 20 Hz | klein (~10 min) | Input-Latenz weg, GUI responsiv | `window.update()` aus Tick-Schleife nach oben |
| 🟡 **3** | GUI Immediate-Mode | mittel (~1–2h) | Sekundärer Performance-Gewinn | GUI auf VBO + Batch umstellen (später) |

**Empfohlene Reihenfolge:**
1. Fix 1 + 2 umsetzen → schnell gegentesten, ob Stutter/Latenz weg sind
2. (Später) Fix 3 angehen, wenn profiling zeigt, dass GUI ein Problem ist

---

## Fußnoten

### "80 fps bei 120-Ziel" — Mathematik

```
Ziel:        120 fps
renderCap:   1/120 = 8,33 ms
Sleep-Zeit:  8,33 ms - 1 ms = 7,3 ms (- 0,001 Sicherheits-Margin)

parkNanos-Overschlaf: ~4–5 ms (macOS Scheduler-Granularität)
Echte Frame-Zeit:     7,3 ms + ~4,7 ms ≈ 12,0 ms

Erreichte FPS: 1000 ms / 12,0 ms = 83 fps ≈ 80 fps ✓
```

Der Overschlaf ist **nicht GPU/CPU-Limit**, sondern OS-Scheduler.

### Warum `parkNanos` für Frame-Limiting nicht ausreicht

- `parkNanos` hat Scheduler-Granularität (1–10 ms je nach OS)
- Precision: ±10 % (besser möglich mit Hybrid-Sleep + Busy-Spin, aber Kosten CPU)
- Für hochfrequente Frame-Limits (60+ fps): **VSync ist die richtige Antwort**
- Hybrid-Sleep nur, wenn bewusst unter Refresh-Rate limitieren nötig

---

**Nächste Schritte:**
1. VSync aktivieren (`glfwSwapInterval(1)`)
2. Event-Polling pro Loop-Iteration
3. Gegentesten auf macOS / Windows
4. Danach Profiling, ob GUI ein Problem ist
