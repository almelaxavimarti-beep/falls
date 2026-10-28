// Configuración común a todos los módulos.
subprojects {
    group = "io.github.almelaxavimarti"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }

    // Compilar siempre para Java 25 y en UTF-8 (tildes y ñ en el código).
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(25)
        options.encoding = "UTF-8"
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
