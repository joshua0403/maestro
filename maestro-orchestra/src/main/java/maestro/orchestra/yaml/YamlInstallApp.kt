package maestro.orchestra.yaml

import com.fasterxml.jackson.annotation.JsonCreator

data class YamlInstallApp(
    val appId: String,
    val appPath: String
)