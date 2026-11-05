StunSlam v1.1.0 - Fabric 1.21.11 (client-side), with Mod Menu + Cloth Config settings screen

BUILD
1. Install JDK 21.
2. In this folder, generate the Gradle wrapper (or copy gradlew/gradle/ from a
   Fabric template made at https://fabricmc.net/develop for 1.21.11):
       gradle wrapper
3. Build:
       ./gradlew build        (Windows: gradlew.bat build)
4. Jar: build/libs/stunslam-1.0.0.jar (not the -sources jar)

INSTALL (mods folder)
- Fabric Loader 0.16.14+, Fabric API (1.21.11), Cloth Config (1.21.11), Mod Menu (optional, for the settings button)

NOTES
- Config file: config/stunslam.json. Toggle key default: ] (rebind in Controls).
- If the fabric-loom version in build.gradle doesn't resolve, use the one from the Fabric template generator.
- If a Yarn method name fails to compile, check IDE autocomplete; names can shift between builds.
- Use only on servers you own/administer for anti-cheat testing.

USAGE (v1.1.0)
- Hotbar must contain an axe and a mace.
- Default: right-click while a player is in reach (crosshair target, or nearest within the view-angle limit) runs axe hit -> swap -> mace hit.
- Trigger / target mode / fall + cooldown requirements are in the Mod Menu settings.
- Delete config/stunslam.json after updating from v1.0.0 to pick up the new defaults.
