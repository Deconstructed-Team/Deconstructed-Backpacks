# Deconstructed Backpacks

Backpacks of six tiers with upgrades: worn, carried or placed as a block. For Minecraft 1.12.2.

Needs [Deconstructed Core](https://github.com/Deconstructed-Team/Deconstructed-Core) of the same version or newer.

## Origin and license

This is a port to Minecraft 1.12.2 of [Sophisticated Backpacks](https://github.com/P3pp3rF1y/SophisticatedBackpacks) by P3pp3rF1y, made from
[`cf55d7d6`](https://github.com/P3pp3rF1y/SophisticatedBackpacks/tree/cf55d7d6bc4dbdd646a41b12f4fd0cbf921b47d2), the last commit of that repository published under the
GNU General Public License v3, on 2025-02-10. Its later commits are under All Rights Reserved; nothing in this
port comes from them.

The GPL v3 under which that commit was published lets anyone use, change and share the code, and the rights it
gives can't be taken back for what was released under it: a later change of license applies only to what comes
after. So this port:

- is under the same GNU General Public License v3 (see [LICENSE](LICENSE)), as a work based on a GPL one must be;
- keeps P3pp3rF1y's copyright of the original code, and states that the code is changed: it is a port to an
  older game version, with much rewritten for it;
- is published with its full source, as the GPL asks;
- doesn't use the original's name for the mod, and isn't made, endorsed or supported by P3pp3rF1y. Don't report
  problems with it to the original mod.

The textures are by Ridanisaurus, made for the original mod and contributed to its repository while it was under
the GPL v3, so they are under it as well.

## Building

```bash
./gradlew build
```

The jar to install is `build/libs/deconstructedbackpacks-<version>.jar` (not `-dev` or `-sources`).

Deconstructed Core is taken from its GitHub release, of the version in `gradle.properties`. To build
against a local copy instead, give it a version that isn't released, run `./gradlew publishToMavenLocal`
in it, and pass that version here, as `./gradlew build -Pcore_version=1.0.1`.

Another mod can be built against this one, from its GitHub releases:

```groovy
repositories {
    ivy {
        url = 'https://github.com/Deconstructed-Team/'
        patternLayout {
            artifact '[organisation]/releases/download/v[revision]/[module]-[revision](-[classifier]).[ext]'
        }
        metadataSources { artifact() }
        content { includeGroup 'Deconstructed-Backpacks' }
    }
}
dependencies {
    implementation rfg.deobf('Deconstructed-Backpacks:deconstructedbackpacks:1.0.0')
}
```
