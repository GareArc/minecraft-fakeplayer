val detoolsVersion = "0.1.7-SNAPSHOT"

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    compileOnly(project(":fakeplayer-api"))

    implementation("com.github.tanyaofei.devtools:devtools-core:$detoolsVersion")
    implementation("com.github.tanyaofei.devtools:devtools-command:$detoolsVersion")
    implementation("com.github.tanyaofei.devtools:devtools-database:$detoolsVersion")

    compileOnly("com.mojang:authlib:4.0.43")
    compileOnly("dev.jorel:commandapi-paper-core:11.0.0")
    compileOnly("com.mojang:brigadier:1.1.8")
    compileOnly("io.netty:netty-transport:4.1.82.Final")
    compileOnly("commons-io:commons-io:2.15.1")

    compileOnly(files("${rootProject.projectDir}/lib/OpenInv.jar"))
    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks.processResources {
    inputs.property("revision", project.version)
    filesMatching("**/*.yml") {
        expand("revision" to project.version)
    }
}
