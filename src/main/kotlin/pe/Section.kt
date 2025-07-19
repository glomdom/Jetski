package io.github.glomdom.jetski.pe

data class Section(
    val name: String,
    val virtualAddress: RVA,
    val virtualSize: UInt,
    val sizeOfRawData: UInt,
    val pointerToRawData: UInt
)
