plugins {
    id("java-library")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    compileOnly("com.google.errorprone:error_prone_core:2.36.0")
    implementation(libs.annotation)
}
