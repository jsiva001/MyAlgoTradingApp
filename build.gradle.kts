// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.hilt.android) apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.5"
}

// Detekt Configuration
dependencies {
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.5")
}

detekt {
    toolVersion = "1.23.5"
    config = files("detekt.yml")
    buildUponDefaultConfig = true
    
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(false)
        sarif.required.set(true)
    }
}