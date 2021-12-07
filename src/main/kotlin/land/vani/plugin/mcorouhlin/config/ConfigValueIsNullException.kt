package land.vani.plugin.mcorouhlin.config

class ConfigValueIsNullException(
    node: String,
) : Exception("Config value of '$node' is required but that is null")
