# mcorouhlin

[![Maven Central](https://img.shields.io/maven-central/v/land.vani.mcorouhlin/mcorouhlin-api)](https://search.maven.org/search?q=g:land.vani.mcorouhlin)
[![detekt](https://github.com/vaniland-mc/mcorouhlin/actions/workflows/detekt.yml/badge.svg)](https://github.com/vaniland-mc/mcorouhlin/actions/workflows/detekt.yml)
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

### Config DSL

```kotlin
class Config(path: Path) : BukkitConfiguration<Config>(path) {
    var nullableInt: Int? by value("nullableInt")
    var defaultInt: Int by value<Int>("defaultInt").default(10)
    var strictInt: Int by value<Int>("strictInt").strict()
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
