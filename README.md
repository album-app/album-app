# HIPS installer / launcher

This is a Java based entry point to setting up HIPS via GUI. It's still highly experimental and might mess with your existing conda setup.

## How to use

### Build from source
1. Clone this repository
1. Import in IntelliJ, (I'm using JBRSDK11)
1.
    - (a) run the gradle task `jlink` or `jlinkzip` to build the executables, they will be built into `build/image` / `build/image.zip`
    - (b)  run the `mdc.ida.hips.Main` class directly

### Download binaries
- [latest Windows build](https://drive.google.com/file/d/15uP3ZwDz-ro8z504e3GNUkf84fae_trF/view?usp=sharing)
- [latest Linux build](https://drive.google.com/file/d/1O63atjkb5KE7kAi9ZcFdnFwZ7XQuaCsD/view?usp=sharing)
- latest MacOS build <i>TODO</i>

### Run from binaries
The binaries should contain an executable `bin/HIPS`. It will guide you through the HIPS setup process (installing conda and the HIPS environment).

## Compatible catalogs
- https://gitlab.com/ida-mdc/capture-knowledge/ ([catalog website](https://ida-mdc.gitlab.io/capture-knowledge/catalog))
