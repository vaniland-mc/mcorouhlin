package land.vani.plugin.mcrouhlin.util

private val randomStrings = ('a'..'z') + ('A'..'Z')

fun randomString(length: Int = 16): String = List(length) { randomStrings.random() }.joinToString("")
