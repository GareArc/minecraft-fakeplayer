plugins {
    id("com.gradleup.shadow")
}

dependencies {
    implementation(project(":fakeplayer-api"))
    implementation(project(":fakeplayer-core"))
    implementation(project(":fakeplayer-v1_20_1"))
    implementation(project(":fakeplayer-v1_20_2"))
    implementation(project(":fakeplayer-v1_20_3"))
    implementation(project(":fakeplayer-v1_20_4"))
    implementation(project(":fakeplayer-v1_20_5"))
    implementation(project(":fakeplayer-v1_20_6"))
    implementation(project(":fakeplayer-v1_21"))
    implementation(project(":fakeplayer-v1_21_1"))
    implementation(project(":fakeplayer-v1_21_3"))
    implementation(project(":fakeplayer-v1_21_4"))
    implementation(project(":fakeplayer-v1_21_5"))
    implementation(project(":fakeplayer-v1_21_6"))
    implementation(project(":fakeplayer-v1_21_7"))
    implementation(project(":fakeplayer-v1_21_8"))
    implementation(project(":fakeplayer-v1_21_9"))
    implementation(project(":fakeplayer-v1_21_10"))
    implementation(project(":fakeplayer-v1_21_11"))
}

tasks.shadowJar {
    archiveBaseName.set("fakeplayer")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
    destinationDirectory.set(rootProject.layout.buildDirectory.dir("libs"))
}

tasks.assemble {
    dependsOn(tasks.shadowJar)
}
