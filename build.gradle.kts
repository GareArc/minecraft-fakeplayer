plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    id("com.gradleup.shadow") version "9.4.2" apply false
}

val revision: String by project

allprojects {
    group = "io.github.hello09x.fakeplayer"
    version = revision
}

subprojects {
    apply(plugin = "java")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    repositories {
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://oss.sonatype.org/content/groups/public/")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://jitpack.io")
    }

    dependencies {
        "compileOnly"("org.projectlombok:lombok:1.18.34")
        "annotationProcessor"("org.projectlombok:lombok:1.18.34")
    }
}
