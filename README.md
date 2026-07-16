# Madness Core

A core API for Minecraft Fabric that powers all LukaMadness mods.

Madness Core provides reusable systems that allow multiple mods to share the same infrastructure instead of reimplementing it in every project.

## Features

- Bloodline API
- Species API
- Ability framework
- Character customization
- Player offset system
- Attribute utilities
- Shared networking
- Animation utilities
- Common registries
- Compatibility hooks
- Developer API for creating addons

## Requirements

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Java 21

## Building

Clone the repository:

```bash
git clone https://github.com/LukaMadness/MadnessCore.git
cd MadnessCore
```

Build the project:

```bash
./gradlew build
```

Windows:

```bat
gradlew build
```

The compiled JAR can be found in:

```
build/libs/
```

Fabric projects are typically built using Gradle, and the generated mod JAR is placed in the `build/libs` directory. :contentReference[oaicite:1]{index=1}

## Development

Import the project as a Gradle project in IntelliJ IDEA or your preferred IDE.

Useful Gradle tasks:

```bash
./gradlew runClient
./gradlew runServer
./gradlew build
./gradlew clean
```

## Using Madness Core

Add Madness Core as a dependency in your project and implement its APIs to create custom:

- Bloodlines
- Species
- Abilities
- Attributes
- Character components
- Networking features

Documentation and examples will be added in future releases.

## Compatibility

Current target:

- Minecraft 1.21.1
- Fabric

Future loader support is not guaranteed.

## Roadmap

- Bloodline API
- Species API
- Ability System
- Character Offset API
- Networking API
- Animation API
- Documentation
- Examples
- Public API Reference

## Contributing

Issues and pull requests are welcome.

Please open an issue before submitting major changes.

## License

This project is licensed under the GNU General Public License v3.0 (GPL-3.0).

See the LICENSE file for more information.
