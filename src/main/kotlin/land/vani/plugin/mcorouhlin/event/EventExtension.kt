package land.vani.plugin.mcorouhlin.event

import land.vani.plugin.mcorouhlin.CoroutinePlugin

fun CoroutinePlugin.events(block: Events.() -> Unit) {
    Events(this).apply(block)
}
