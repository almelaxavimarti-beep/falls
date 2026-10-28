// core: el "cerebro" de Falls. Java puro, sin JavaFX ni javax.sound.midi (decisión 003).
plugins {
    `java-library`
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
