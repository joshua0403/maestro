package maestro.orchestra.yaml

import com.fasterxml.jackson.annotation.JsonCreator

data class YamlUpdateState(
    val appId: String,
    val containerPath: String
)