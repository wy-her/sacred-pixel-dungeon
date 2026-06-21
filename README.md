# Sacred Pixel Dungeon

Sacred Pixel Dungeon is an HTML5 cross-platform roguelike dungeon crawler and a fan-made fork of [Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/) by [Evan Debenham (00-Evan)](https://github.com/00-Evan).

This project serves as a **gateway to Pixel Dungeon games** — helping new players discover this amazing roguelike genre through accessible browser-based gameplay.

## Play Now

**https://sacredpixel.net** — Free, no ads, no installation required.

### Browser Compatibility

- Chrome 80+ (recommended)
- Firefox 78+
- Safari 14+
- Edge 80+
- Mobile Chrome / Safari supported

---

## Why This Project Exists

We created Sacred Pixel Dungeon to **spread the love of Pixel Dungeon** to a wider audience:

- **Zero-friction access** — Play instantly in any browser, no app installation needed
- **Full keyboard accessibility** — Play entirely without a mouse for accessibility needs
- **Beginner-friendly tutorial** — Interactive 1-minute tutorial teaches core mechanics step by step
- **UI/UX improvements** — Enhanced interface and quality-of-life features
- **Balance adjustments** — Tweaked gameplay for web platform characteristics

Our goal is to introduce more players to the Pixel Dungeon family of games. We encourage players who enjoy Sacred Pixel Dungeon to also try [Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/) and other community forks!

---

## Distribution & Monetization

Sacred Pixel Dungeon is committed to keeping the game **free and accessible** while supporting ongoing development.

### Free Distribution (No Ads)

| Platform | URL | Ads |
|----------|-----|-----|
| **Official Website** | [sacredpixel.net](https://sacredpixel.net) | None |
| **itch.io** | [sacredpixel.itch.io](https://sacredpixel.itch.io) | None |
| **ONE Store** (Korea) | [ONE Store](https://m.onestore.co.kr/v2/ko-kr/app/0001006199) | None |
| **GitHub Pages** | (if deployed) | None |
| **Self-hosted** | Build from source | None |

The game is **completely free** with **no advertisements** on our official website and all self-hosted distributions.

### Apps in Toss (Korea Only)

The [Apps in Toss](https://toss.im/) version includes **banner ads** and **interstitial ads** to support development costs.

**Why ads on this platform?**
- HTML5 porting requires ongoing maintenance
- Ad revenue covers maintenance costs and development labor
- **Remaining revenue is reinvested in marketing** to introduce more players to Pixel Dungeon games
- Users who prefer ad-free experience can play at [sacredpixel.net](https://sacredpixel.net)

### Respect for the Original

We deeply respect Shattered Pixel Dungeon and its creator Evan Debenham:

- This project exists primarily to bring the game to **web browsers** — a platform the original does not target
- We aim to complement, not compete with, the original game
- All code changes are open source under GPLv3, as required by the original license

---

## Building

Requires **Java 21+** and Gradle (wrapper included).

### Production Build

```bash
./gradlew :teavm:buildRelease
```

Output: `teavm/build/dist/cloudflare/webapp/` — serve this directory with any static HTTP server.

### Debug Build + Local Testing

```bash
./gradlew :teavm:buildDebug
cd teavm/build/dist/webapp
python -m http.server 8080
```

Then open `http://localhost:8080` in your browser.

### Capacitor Android APK

```bash
# 1. Build web assets
./gradlew :teavm:buildRelease

# 2. Copy to Capacitor and build APK
cp -r teavm/build/dist/cloudflare/webapp/* capacitor-app/android/app/src/main/assets/public/
cd capacitor-app/android && ./gradlew assembleRelease
```

Output: `capacitor-app/android/app/build/outputs/apk/release/app-release.apk`

### Appsintoss (Toss Mini App)

Requires: `pip install fonttools brotli` (for font subsetting)

```bash
# 1. Build with font subsetting
./gradlew :teavm:buildAppsintoss

# 2. Create .ait bundle
cd appsintoss-app && npm run copy-game && npm run build
```

Output: `appsintoss-app/sacred-pixel-dungeon.ait`

---

## Project Structure

| Module | Purpose |
|--------|---------|
| `SPD-classes` | Watabou framework — LibGDX wrapper (scenes, sprites, audio, UI, particles) |
| `core` | All game logic (~1180 Java files) — actors, items, levels, scenes, UI |
| `teavm` | HTML5/WebGL build via TeaVM (replaces GWT) |
| `services` | Update checking and news feed services |

---

## Technical Highlights

- **TeaVM 0.13.1** — Java bytecode → JavaScript (5.2MB output, ~1min compile)
- **Canvas2D font rendering** — Inter + Noto Sans (CJK support) via CSS font-family stack
- **localStorage saves** — Game state persisted in browser storage
- **Single-threaded actor system** — Adapted for HTML5 with `waitingForCallback` mechanism
- **On-demand music loading** — Initial download ~26MB (music streamed on demand)
- **Browser zoom prevention** — Ctrl+scroll, pinch, double-tap zoom blocked
- **Auto-save** — Saves on tab switch, page hide, and every 10 seconds

---

## Credits

Sacred Pixel Dungeon is based on:
- **[Shattered Pixel Dungeon](https://shatteredpixel.com/shatteredpd/)** by [Evan Debenham (00-Evan)](https://github.com/00-Evan/shattered-pixel-dungeon)
- **[Pixel Dungeon](http://pixeldungeon.watabou.ru/)** by [Oleg Dolya (Watabou)](https://github.com/watabou/pixel-dungeon)

### Resources
- **Font:** [Inter](https://rsms.me/inter/) by Rasmus Andersson, [Noto Sans](https://fonts.google.com/noto/specimen/Noto+Sans) by Google
- **License:** GPLv3 — see [LICENSE.txt](LICENSE.txt)

---

## License

Sacred Pixel Dungeon is free software licensed under the **GNU General Public License v3.0** (GPLv3).

You are free to:
- Use, study, and modify the source code
- Distribute copies of the original or modified versions
- Use the game for any purpose

Under the condition that:
- Any distributed copies or modifications must also be licensed under GPLv3
- The source code must be made available

### License Files

| File | Description |
|------|-------------|
| [LICENSE.txt](LICENSE.txt) | GPLv3 full text |
| [NOTICE.txt](NOTICE.txt) | Copyright notices and attributions |
| [THIRD_PARTY_LICENSES.md](THIRD_PARTY_LICENSES.md) | Third-party library licenses |

### Source Code

The complete source code is available at:
- **GitHub:** https://github.com/wy-her/sacred-pixel-dungeon

This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

---

## GitHub Description

> **Sacred Pixel Dungeon** — A fan-made HTML5 port of Shattered Pixel Dungeon.
>
> Play free with no ads at **[sacredpixel.net](https://sacredpixel.net)**

### Key Changes from Original

- **Platform**: Runs in any modern web browser — no app installation needed
- **Tutorial**: Interactive 1-minute tutorial for new players
- **Accessibility**: Full keyboard navigation support (arrow keys, WAXD+QEZC, Tab) — playable without a mouse
- **Cross-device sync**: Export/import save data via URL for seamless device switching
- **UI/UX improvements**: Enhanced interface, improved window behavior, better input handling
- **Balance tweaks**: Reworked Berserker talents, Imp quest, Blacksmith quest, Chaotic Censer, and Rat King summoning altar for web play
