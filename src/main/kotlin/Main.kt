package io.github.glomdom.jetski

import io.github.glomdom.jetski.pe.ImageReader
import java.io.File

fun main() {
    val file = File("IntegrationTests.dll")
    val reader = ImageReader(file)

    val output = """
        architecture = ${reader.image.architecture}
        timestamp = ${reader.image.timestamp}
        kind = ${reader.image.kind}
        characteristics = ${reader.image.characteristics}
        linkerVersion = ${reader.image.linkerVersion}
        subSystemMajor = ${reader.image.subSystemMajor}
        subSystemMinor = ${reader.image.subSystemMinor}
        win32Resources = ${reader.image.win32Resources}
        debug = ${reader.image.debug}
        sections = ${reader.image.sections}
    """.trimIndent()

    println(output)
}