plugins {
    id("java")
    id("maven-publish")
}

group = "me.vrganj"
version = "1.0-SNAPSHOT"

repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    mavenCentral()
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.18.2-R0.1-SNAPSHOT")
    compileOnly("org.nanohttpd:nanohttpd:2.3.1")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

// for testing purposes
publishing {
    publications {
        create<MavenPublication>("aoe") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            url = uri("http://localhost:7000")
            isAllowInsecureProtocol = true
        }
    }
}


