package io.github.glomdom.jetski.pe

enum class TargetArchitecture(val value: Int) {
    I386(0x014c),
    AMD64(0x8664),
    IA64(0x0200),
    ARM(0x01c0),
    ARMv7(0x01c4),
    ARM64(0xaa64);

    companion object {
        fun fromValue(value: UShort): TargetArchitecture = entries.find { it.value.toUShort() == value }!!
    }
}
