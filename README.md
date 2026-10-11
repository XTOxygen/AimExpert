# AimExpert

Minecraft **1.8.9** Forge mod (client side) — custom crosshairs, dynamic behaviour, a rounded ClickGUI with a
Gaussian blurred background, shareable crosshair templates and Chinese/English support.

* Mod version: **1.0.0**
* Author: **EndWithMe**
* Mod id: `aimexpert`
* Environment: client (works on vanilla servers, the sharing relay additionally needs the mod on the server)
* Default GUI key: **M** (changeable in `Options -> Controls`)

---

## 1. ClickGUI

Press **M** in game.

* Black / gray / white grayscale theme, rounded corners, semi transparent panel and cards.
* **Gaussian blurred** background (see section 5) with a dim overlay on top.
* Cards slide **down from the top** (staggered, top to bottom) when the GUI opens and slide **up** when it closes.
* Mouse wheel scrolls the card list, the scroll bar on the right shows the position.
* ESC or the **x** button (or a click outside the panel) closes the GUI.
* Language can be switched at any time (General card, `EN / 中文`), every label updates immediately.

Settings cards:

| Card | Contents |
| --- | --- |
| General | language, background blur, blur strength, animations, key hint, version info |
| Crosshair | custom crosshair on/off, hide crosshair, size, opacity, trigger range, other player handling, per type enable + RGB color (normal / friendly / hostile / FOV / attack) |
| Drawing board | shape selector, pixel grid, grid size, mirror drawing, clear, invert, copy shape, paste shape, template name, save as template |
| Dynamic | dynamic crosshair, react to FOV movement, react to attacking, spread amount, response speed, switch animation, duration, direction (small→large / large→small) |
| Template | template name, template list, load, save, delete, copy JSON, paste JSON, reset to Local |
| Share | recipient, share, share to all, method hints, relay status, pending offers, accept, decline |

All changes are written to `config/AimExpert.cfg` when the GUI is closed.

## 1.1 Drawing board

The **Drawing board** card is a pixel editor for the crosshair shapes, so patterns do not have to be written by hand:

* Pick which shape to edit with the shape selector (`Nrm / Fr / Hos / FOV / Atk`).
* **Left click or drag paints a dot, right click or drag erases it** in a `Grid size` x `Grid size` grid (5 to 21).
* `Mirror drawing` mirrors every painted dot horizontally and vertically, which is what most crosshair
  shapes need.
* `Clear`, `Invert`, `Copy shape` and `Paste shape` (paste into another shape type) are one click away.
* Everything is applied to the active template **immediately**, so the crosshair in game and the previews in
  the Crosshair card change while drawing.
* `Save as template` writes the result to `config/aimexpert/templates`. An empty name or the built in name
  `Local` are turned into a fresh unique name automatically, so saving a drawing never fails.
* Saved templates can be shared with the two methods described in section 4.

## 2. Crosshairs

The vanilla crosshair is removed (the `CROSSHAIRS` overlay event is cancelled) and replaced by a pixel dot
renderer that draws the pattern of the active template.

* Five shapes are built in: **normal** (cross with a center gap), **friendly mob** (dotted ring),
  **hostile mob** (heavy X), **FOV movement** (corner brackets) and **attacking** (radial burst).
* Every shape has its own **color**, and the shared **size** and **opacity** apply to all of them.
* `Hide crosshair` removes both the vanilla and the custom crosshair, `Custom crosshair` off brings the vanilla one back.
* **Trigger range** (default 8 blocks) is an own aim ray cast, so a mob is recognised as target while merely
  aiming at it. (Vanilla 1.8.9 stops entity picking after roughly three blocks, which would keep the mob
  crosshairs from ever appearing at a normal aiming distance.)
* **Other players** can be treated as `Hostile`, `Friendly` or `Ignored`.
* **Dynamic spread** eases smoothly towards its target scale while moving (`FOV movement`) or attacking.
* **Shape switching** is animated over `Animation duration`, either `small to large` or `large to small`,
  with a cubic ease out and a short alpha fade.

Selection priority, highest first:

```
attacking  >  hostile / friendly target  >  FOV movement  >  normal
```

If a type is disabled the next one in that order is used, so a disabled type never leaves the screen empty.

## 3. Templates

* Built in template: **Local**.
* Own templates live in `config/aimexpert/templates/<name>_<hash>.json`, template names accept Chinese characters.
* A template carries the five patterns plus size and opacity. Colors stay local to every user, so loading
  somebody else's template never overrides your colors.

Template JSON example:

```json
{
  "name": "Local",
  "formatVersion": 1,
  "size": 100,
  "opacity": 100,
  "patterns": {
    "NORMAL": ["...#...", "...#...", "...#...", "###.###", "...#...", "...#...", "...#..."],
    "FRIENDLY": ["..#.#..", ".#...#.", "#.....#", ".......", "#.....#", ".#...#.", "..#.#.."],
    "HOSTILE": ["#.....#", "##...##", ".##.##.", "...#...", ".##.##.", "##...##", "#.....#"],
    "FOV": ["##...##", "#.....#", "#.....#", "...#...", "#.....#", "#.....#", "##...##"],
    "ATTACK": ["...#...", ".#.#.#.", "..#.#..", "#.....#", "..#.#..", ".#.#.#.", "...#..."]
  }
}
```

* `#` is a dot, everything else is empty. Patterns are padded to a square and limited to 21x21 so an
  imported file can never stall the renderer.
* The exporting side prefers the native LWJGL clipboard, the AWT clipboard of the vanilla helper is only the
  fallback, because AWT can return a stale value when another program owns the clipboard.

## 4. Sharing (two methods)

**Method 1 — in game**

```
/shareaim <template> [player]
```

* Without a player name the template is sent to every AimExpert user on the server.
* The sender sees `You have shared the template <name> with <player>` (in the configured language).
* The receiver gets a clickable chat line:
  `[Player] has shared a crosshair template with you. Accept?  [Accept] [Decline]`
  Clicking `Accept` loads the template immediately.
* `/aimexpert accept <id>` / `/aimexpert decline <id>` do the same from the command line, `/aimexpert open`
  opens the GUI, `/aimexpert copy [template]` and `/aimexpert paste` handle the JSON.

The relay is done with Forge network packets: the client sends the template to the server, the server
forwards an offer to the target players and reports back the result. The client announces itself with a
handshake, so if the server does not run AimExpert the GUI shows `Server relay: not available` and the chat
tells you to use method 2 instead.

**Method 2 — copy and paste**

`Copy JSON` (or `/aimexpert copy`) puts the template JSON on the clipboard, `Paste JSON`
(or `/aimexpert paste`) imports whatever JSON is on the clipboard as a new template. This works without any
server support and is the way to exchange templates over chat, a forum or a pastebin.

## 5. Background blur and Optifine

The blur is built from vanilla framebuffers only (`net.minecraft.client.shader.Framebuffer`) with a
down/up sample chain using linear filtering, i.e. it is a pure fixed function Gaussian approximation and
needs no shader pack, no Optifine and no OpenGL 3 feature.

* The world image is captured in `RenderWorldLastEvent`, while the world framebuffer still holds the frame,
  and is blurred later when the GUI is drawn, so a GUI that clears the framebuffer cannot break it.
* Optifine is **optional**: the shader state is detected reflectively (`Config.isShaders()`, no compile time
  dependency, no import). When shaders are active the vertical texture orientation is flipped accordingly.
* Every blur step runs inside a save/restore of the GL state (framebuffer, viewport, matrices, texture,
  depth, blend, alpha, cull) and an exception in the blur path disables the blur for the session and falls
  back to a plain dark overlay instead of crashing.

## 6. Building

```
gradlew build --offline
```

The jar is written to `build/libs/AimExpert-1.0.0.jar`. The project targets Forge 1.8.9-11.15.1.2318 with
MCP `stable_20`, Java 8 and UTF-8 source encoding (the Chinese strings require it).

## 7. Project layout

```
com/endwithme/aimexpert/
  AimExpert.java            mod entry point, config and network setup
  Reference.java            mod id, name, version, author, channel
  config/AimSettings.java   all persisted settings
  i18n/Lang.java            built in English and Chinese strings
  crosshair/                types, patterns, templates, template files, runtime state and aim ray cast
  render/                   rounded rect helpers, crosshair renderer, blur renderer, Optifine helper
  gui/                      ClickGUI, cards, widgets
  net/                      handshake and template packets
  share/                    client share manager, server relay, pending offers
  command/                  /shareaim and /aimexpert client commands
```
