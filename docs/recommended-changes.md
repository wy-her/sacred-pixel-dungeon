# Recommended Changes for Forking Sacred Pixel Dungeon

This guide covers technical changes you may want to make when creating your own version based on Sacred Pixel Dungeon's source code.

## Application Name, Version, and Package Name

Variables in the root [build.gradle](/build.gradle):

- `appName` — User-visible name of your game. Change this to your game's name.
- `appPackageName` — Internal identifier. Format: `com.<dev>.<game>`. Must be unique.
- `appVersionCode` — Internal version number. Increment with each release. Keep at or above 891 to avoid triggering Sacred's version compatibility code.
- `appVersionName` — User-visible version string. Change freely.

## Font System

Sacred Pixel Dungeon uses two font rendering systems:

- **BitmapText** — Pixel font from `core/src/main/assets/fonts/pixel_font.png` (ASCII only)
- **RenderedText** — Canvas2D rendered font via `teavm/.../FreeTypeFontGenerator.java`
  - Default: Inter (Latin) + Noto Sans (CJK: KR, JP, SC, TC)
  - 7-font CSS font-family stack for multilingual support

To change the web font:
1. Place your `.woff2` files in `teavm/webapp/fonts/` and `core/src/main/assets/fonts/`
2. Update `@font-face` in `teavm/webapp/styles.css`
3. Update `CSS_FONT_FAMILY` in `teavm/.../FreeTypeFontGenerator.java`
4. Update font preload in `teavm/webapp/index.html`
5. Add font license file (OFL.txt) to the fonts folder

## Credits

Edit [AboutScene.java](/core/src/main/java/com/sacredpixel/sacredpixeldungeon/scenes/AboutScene.java) to update credits. Due to GPLv3, existing credits must not be removed.

## Update Notification

The game uses debug update/news services by default (no-op). To point to your own releases, modify the services modules in `services/`.

## Translations

The game supports 23 languages via `.properties` files in `core/src/main/assets/messages/`. To add/remove languages, edit the enum in [Languages.java](/core/src/main/java/com/sacredpixel/sacredpixeldungeon/messages/Languages.java).

## Tutorial System

Sacred Pixel Dungeon includes an interactive tutorial accessible from the title screen. If you're creating a fork:

- **Tutorial messages**: Located in `messages/windows/windows_*.properties` (keys starting with `wndtutorial.`)
- **Tutorial logic**: `tutorial/TutorialManager.java` and `tutorial/TutorialState.java`
- **Tutorial level**: `levels/TutorialLevel.java` — a 16x16 room with hidden door
- **Tutorial mobs**: `actors/mobs/TutorialRat.java` and `TutorialSnake.java` (HP=1)

To customize or disable the tutorial, modify `TutorialManager.java` or remove the tutorial button in `TitleScene.java`.

## Building for Distribution

### Web (HTML5)

```bash
./gradlew :teavm:buildRelease
```

Deploy `teavm/build/dist/cloudflare/webapp/` to any static hosting (Cloudflare Pages, Netlify, etc.).

### Mobile App (Capacitor)

The HTML5 build can be wrapped as a native app using [Capacitor](https://capacitorjs.com/):

1. Build the web app with `buildRelease`
2. Set up a Capacitor project pointing to `teavm/build/dist/cloudflare/webapp/`
3. Add Android/iOS platforms via Capacitor CLI
4. Build and sign the native app

## License

GPLv3 — modifications must remain open-source when distributed. See [LICENSE.txt](/LICENSE.txt).
