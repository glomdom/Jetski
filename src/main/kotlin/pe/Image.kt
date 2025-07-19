package io.github.glomdom.jetski.pe

class Image {
    var architecture: TargetArchitecture? = null
    var timestamp: UInt? = null
    var kind: ModuleKind? = null
    var characteristics: UShort? = null
    var linkerVersion: UShort? = null
    var subSystemMajor: UShort? = null
    var subSystemMinor: UShort? = null
    var win32Resources: DataDirectory? = null
    var debug: DataDirectory? = null
    var sections: MutableList<Section>? = null
}
