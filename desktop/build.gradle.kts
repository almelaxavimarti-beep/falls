// desktop: la aplicación de escritorio. Usa core por dentro.
// JavaFX se añadirá en el Ciclo 4.
plugins {
    application
}

dependencies {
    implementation(project(":core"))
}

application {
    mainClass.set("io.github.almelaxavimarti.falls.desktop.Main")
}
