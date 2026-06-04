# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.4.0] - 2026-06-04

### Added

- New button textures for clicked, hovered, and normal states
- Logo texture files for GUI
- Dark bricks texture

### Changed

- Extracted core game engine code into a dedicated `rollercoaster` dependency; pxWorlds now focuses purely on game content
- Refactored code base for improved readability: added comments, restructured conditionals, enhanced formatting across multiple classes
- Refactored shader code by removing unused variables and simplifying texture coordinate assignment

### Removed

- Unused texture files (checker.png, menustate.png, test.png)
- `PERFORMANCE_ANALYSIS.md`

### Fixed

- Removed `HiddenField` from Checkstyle configuration to resolve false-positive warnings

## [0.3.1] - 2026-05-29

### Added

- World select screen and new world creation screen
- World file migration system for forward-compatible `.pxwl` formats
- Dev mode detection and F4 keyboard shortcut to toggle debug UI

### Changed

- Refactored `Player` and `MovementSpeed` classes for improved clarity and maintainability
- Encapsulated default key retrieval logic inside `InputAction`
- Updated world loading and saving mechanisms for consistency with tile management
- Added `renderGui` method to `GameState` base class and all implementing states
- Removed unused `Transform` class; updated all references accordingly
- Translated all code comments and documentation to German

## [0.3.0] - 2026-05-29

### Added

- Fixed-timestep game loop (`GameLoop`) with spiral-of-death guard
- `WorldLoadingService` for game state management during world file loading
- `WorldIO` — GZIP binary serialization/deserialization of game worlds with version field and backward compatibility
- `WorldListener` interface for tile change notifications
- `DebugOverlay` and `DebugRenderer` for enhanced debug information display
- Entity interpolation via `prevPos` for smoother rendering between ticks
- Camera interpolation: render methods now accept an `alpha` parameter for sub-tick smoothing
- `PlayerSpawnState` for managed player spawn handling
- `WorldDebugRenderer` for tile-level debug visualization
- Pause screen with main menu navigation
- `TICK_RATE` constant for fixed-rate logic updates
- `TILE_SIZE` constant for consistent world scaling across renderers

### Changed

- Replaced old `Input` class with `InputHandler` and `InputProcessor` for improved event abstraction
- Refactored button rendering to use a tile-state enum instead of raw conditionals
- Refactored camera and world matrix handling to reduce per-frame object allocations
- Refactored configuration handling with improved dependency injection and simplified JSON operations

### Removed

- Obsolete `WorldManager` checksum validation and download functionality

## [0.2-SNAPSHOT] - 2026-04-16

### Added

- **CI** — GitHub Actions workflows for cross-platform artifact builds (Linux, macOS ARM64, Windows) and release creation
- **CI** — Gitea Actions `release-sync` workflow (mirrors Gitea releases to GitHub via API)
- **CI** — GitLab CI pipeline with build, lint, and GitHub sync stages
- `AutoCloseable` lifecycle management in `Shader` and `Model` (replacing deprecated `finalize()`)

### Changed

- Upgraded project to LWJGL 3.3.5, Java 21, and all modern dependencies
- Migrated build from Gradle to Maven (established in 2020; fully cleaned up here)
- Added UTF-8 encoding properties to Maven build

### Removed

- Obsolete `SharedLibraryLoader` class
- FOSSA configuration and obsolete Gradle workflow files
- Debug `System.out.println` statements from `Texture`

### Fixed

- Resolved all Checkstyle violations across the code base

## [0.1.0] - 2018-07-25

### Added

- **Platform / Window** — GLFW window lifecycle via LWJGL; configurable screen resolutions (`ScreenResolutions`) and window types (`ScreenTypes`)
- **Game state** — `GameStateManager` with push/pop/swap transitions; `GameState` base class covering update, render, and resize lifecycle
- **Input** — keyboard and mouse event handling with GLFW callbacks
- **Graphics** — `SpriteBatch`, `Camera`, `Shader`, `Texture`, `Model`; animated background on main menu
- **Tile world** — basic tile map rendering with `TileRenderer`
- **Player** — entity with movement (walking and running), diagonal movement support, directional animations
- **GUI** — main menu with animated background, buttons, and logo; `PauseGui`; debug information overlay
- **Config** — JSON-backed configuration with screen resolution and window settings
- **Physics** — `AABB` axis-aligned bounding box with collision response helpers

[Unreleased]: https://github.com/temmiland/pxWorlds/compare/0.4.0...HEAD
[0.4.0]: https://github.com/temmiland/pxWorlds/compare/0.3.1...0.4.0
[0.3.1]: https://github.com/temmiland/pxWorlds/compare/0.3.0...0.3.1
[0.3.0]: https://github.com/temmiland/pxWorlds/compare/0.2-SNAPSHOT...0.3.0
[0.2-SNAPSHOT]: https://github.com/temmiland/pxWorlds/compare/0.1.0...0.2-SNAPSHOT
[0.1.0]: https://github.com/temmiland/pxWorlds/releases/tag/0.1.0
