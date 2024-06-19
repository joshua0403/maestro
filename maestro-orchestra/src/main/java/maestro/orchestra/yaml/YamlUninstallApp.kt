package maestro.orchestra.yaml

import com.fasterxml.jackson.annotation.JsonCreator
import java.lang.UnsupportedOperationException

data class YamlUninstallApp(
    val appId: String
) {
    companion object {

        @JvmStatic
        @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
        fun parse(appId: String): YamlUninstallApp {
            return YamlUninstallApp(appId)
        }
    }
}