plugins {
    //平台支持必选
    id("org.jetbrains.intellij.platform") version "2.0.0"
    //java 支持
    id("java")
    //kotlin 支持
    id("org.jetbrains.kotlin.jvm") version "1.9.24"
}

//依赖配置
dependencies {
    //运行环境配置
    intellijPlatform {
        intellijIdeaCommunity("2024.2")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        plugin("Dart:242.20629")
        instrumentationTools()
    }

    //依赖配置
    implementation("com.github.javafaker:javafaker:1.0.2")
    implementation("io.github.serpro69:kotlin-faker:2.0.0-rc.4")
    implementation("io.github.serpro69:kotlin-faker-lorem:2.0.0-rc.4")
}


//插件配置
intellijPlatform {
    projectName = project.name
    autoReload = true

    pluginConfiguration {
        ideaVersion {
            sinceBuild = "111"
            untilBuild = "999.*"
        }
    }

}

//仓库,一般情况下不用配置
repositories {
    mavenCentral()

    intellijPlatform {
        defaultRepositories()
    }
}

kotlin {
    jvmToolchain(17)
}
