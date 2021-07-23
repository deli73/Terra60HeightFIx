import com.dfsek.terra.configureCompilation
import com.dfsek.terra.configureDependencies

configureCompilation()
configureDependencies()

group = "com.dfsek.terra.common"

dependencies {
    "shadedApi"("com.dfsek:Paralithic:0.4.0")

    "shadedApi"("com.dfsek.tectonic:common:2.1.2")
    "shadedApi"("com.dfsek.tectonic:yaml:2.1.2")

    "shadedApi"("net.jafama:jafama:2.3.2")
}

