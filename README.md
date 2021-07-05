# album app

This is a Java based entry point to setting up [album](https://album.solutions) via GUI. It's still highly experimental and might mess with your existing conda setup.

## How to use

### Build from source
1. Clone this repository
1. Import in IntelliJ, (I'm using JBRSDK11)
1.
    - (a) run the gradle task `jlink` or `jlinkzip` to build the executables, they will be built into `build/image` / `build/image.zip`
    - (b)  run the `mdc.ida.album.Main` class directly

### Download binaries
- [latest Windows build](https://drive.google.com/file/d/15uP3ZwDz-ro8z504e3GNUkf84fae_trF/view?usp=sharing)
- [latest Linux build](https://drive.google.com/file/d/1O63atjkb5KE7kAi9ZcFdnFwZ7XQuaCsD/view?usp=sharing)
- latest MacOS build <i>TODO</i>

### Run from binaries
The binaries should contain an executable `bin/album`. It will guide you through the album setup process (installing conda and the album environment).

## Compatible catalogs
- https://gitlab.com/ida-mdc/capture-knowledge/ ([catalog website](https://ida-mdc.gitlab.io/capture-knowledge/catalog))
