package io.github.glomdom.jetski.pe

typealias RVA = UInt

data class DataDirectory(val virtualAddress: RVA, val size: UInt) {
    fun isZero(): Boolean = (virtualAddress.toInt() == 0 && size.toInt() == 0)
}