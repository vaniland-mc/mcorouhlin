# mcorouhlin

[![Maven Central](https://img.shields.io/maven-central/v/land.vani.mcorouhlin/mcorouhlin-api)](https://search.maven.org/search?q=g:land.vani.mcorouhlin)
[![Build](https://github.com/vaniland-mc/mcorouhlin/actions/workflows/build.yml/badge.svg)](https://github.com/vaniland-mc/mcorouhlin/actions/workflows/build.yml)
[![codecov](https://codecov.io/gh/vaniland-mc/mcorouhlin/branch/main/graph/badge.svg?token=Qh9dZllma8)](https://codecov.io/gh/vaniland-mc/mcorouhlin)

A Kotlin utility library for minecraft plugins.

## Features

### Event DSL

```kotlin
plugin.events {
    cancelIf<BlockBreakEvent> { event ->
        event.player.isOp
    }
}
```

### Permissions

```kotlin
enum class Permissions(
    override val node: String,
    override val description: String?,
    override val default: PermissionDefault?,
    override val children: Map<Permission, Boolean>,
) : Permission {
    TEST("mcorouhlin.test", "test permission", PermissionDefault.OP, mapOf()),
    ;
}
```

### Inventory DSL

```kotlin
val inventory = plugin.inventory(Component.text("some inventory")) {
    default(itemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE))

    slot(1, itemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE)) {
        // on clicked
    }
}
```

### License

This project is licensed under GPL-3.0.
See also [LICENCE](LICENSE).

This project is including the following OpenSource project codes:

- [Shynixn/MCCoroutine](https://github.com/Shynixn/MCCoroutine) (MIT License)
- [nicolaic/brigadier-dsl](https://github.com/nicolaic/brigadier-dsl) (Apache-2.0 License)
