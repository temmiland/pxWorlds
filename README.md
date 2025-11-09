# 🦚 pxWorlds

[![Stars](https://img.shields.io/github/stars/temmiland/pxWorlds?style=social)](https://github.com/temmiland/pxWorlds/stargazers)
[![License](https://img.shields.io/github/license/temmiland/pxWorlds)](./LICENSE)

> **pxWorlds** is a prototype game built with **LWJGL**, inspired by classic tutorials and open-world creativity.
> 🎓 Inspired by: [3D Game Development Series on YouTube](https://www.youtube.com/watch?v=VH9KAhjXVFM&list=PLILiqflMilIxta2xKk2EftiRHD4nQGW0u)

## 🚀 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/temmiland/pxWorlds.git
cd pxWorlds/
```

### 2. Build the project

Make sure you have Git and Java 21+ installed.

```bash
./mvnw clean install
```

### 3. Run the project

```bash
./mvnw exec:exec
```

### 4. Running the JAR (Standalone Executable)

After building, you can run the game using the generated JAR files. Download the appropriate JAR for your platform from the [Releases](https://github.com/temmiland/pxWorlds/releases) page.

- **Linux**: `java -jar pxWorlds-linux.jar`
- **macOS (Intel)**: `java -jar pxWorlds-macos-amd64.jar`
- **macOS (Apple Silicon)**: `java -jar pxWorlds-macos-arm64.jar`
- **Windows**: `java -jar pxWorlds-windows.jar`

Make sure you have Java 21+ installed on your system.

This project is licensed under the MIT License.
Please see [`LICENSE`](https://github.com/temmiland/pxWorlds/blob/master/LICENSE) for more info.
