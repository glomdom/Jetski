package io.github.glomdom.jetski.pe

import java.io.File
import java.io.RandomAccessFile

class ImageReader(file: File) {
    val image = Image()

    private val stream = RandomAccessFile(file, "r")
    private lateinit var cli: DataDirectory

    companion object {
        val MZ_SIGNATURE = 0x5A4D.toUShort()
        val PE_SIGNATURE = 0x00004550U
    }

    init {
        require(file.length() >= 128) { "PE file is less than 128 bytes" }
        require(stream.readUShort() == MZ_SIGNATURE) { "PE file does not start with 0x4D5A (MZ) signature" }

        stream.skipBytes(58)

        val peOffset = stream.readUInt()
        stream.seek(peOffset.toLong())

        require(stream.readUInt() == PE_SIGNATURE) { "PE header missing PE signature" }

        image.architecture = readArchitecture()

        val sections = stream.readUShort()
        image.timestamp = stream.readUInt()
        stream.skipBytes(10)

        val characteristics = stream.readUShort()
        val (subsystem, dllCharacteristics) = readOptionalHeaders()
        readSections(sections)

        image.kind = getModuleKind(characteristics, subsystem)
        image.characteristics = characteristics

        stream.close()
    }

    private fun readArchitecture(): TargetArchitecture {
        return TargetArchitecture.fromValue(stream.readUShort())
    }

    private fun getModuleKind(characteristics: UShort, subsystem: UShort): ModuleKind {
        if (characteristics has 0x2000) {
            return ModuleKind.Dll
        }

        if (subsystem == 0x02.toUShort() || subsystem == 0x09.toUShort()) {
            return ModuleKind.Windows
        }

        return ModuleKind.Console
    }

    private fun readOptionalHeaders(): Pair<UShort, UShort> {
        val pe64 = stream.readUShort() == 0x020b.toUShort()

        image.linkerVersion = stream.readUShort()
        stream.skipBytes(44)
        image.subSystemMajor = stream.readUShort()
        image.subSystemMinor = stream.readUShort()
        stream.skipBytes(16)

        val subsystem = stream.readUShort()
        val dllCharacteristics = stream.readUShort()

        stream.skipBytes(if (pe64) 56 else 40)
        image.win32Resources = readDataDirectory()
        stream.skipBytes(24)
        image.debug = readDataDirectory()
        stream.skipBytes(56)

        cli = readDataDirectory()
        require(!cli.isZero()) { "CLI data directory address and size must not be 0" }

        stream.skipBytes(8) // reserved

        return subsystem to dllCharacteristics
    }

    private fun RandomAccessFile.readZeroTerminatedString(length: Int): String {
        val bytes = ByteArray(length)
        this.readFully(bytes)

        val nullIndex = bytes.indexOf(0)
        val validBytes = if (nullIndex >= 0) bytes.sliceArray(0 until nullIndex) else bytes

        return validBytes.toString(Charsets.UTF_8)
    }

    private fun readSections(count: UShort) {
        val sections = mutableListOf<Section>()

        repeat(count.toInt()) { index ->
            val name = stream.readZeroTerminatedString(8)

            val virtualSize = stream.readUInt()
            val virtualAddress = stream.readUInt()
            val sizeOfRawData = stream.readUInt()
            val pointerToRawData = stream.readUInt()

            stream.skipBytes(16)
            sections.add(Section(name, virtualAddress, virtualSize, sizeOfRawData, pointerToRawData))
        }

        image.sections = sections
    }

    private fun readDataDirectory(): DataDirectory {
        return DataDirectory(stream.readUInt(), stream.readUInt())
    }

    private fun RandomAccessFile.readUShort(): UShort {
        val b1 = this.readUnsignedByte()
        val b2 = this.readUnsignedByte()

        return ((b2 shl 8) or b1).toUShort()
    }

    private fun RandomAccessFile.readUInt(): UInt {
        val low = this.readUShort().toUInt()
        val high = this.readUShort().toUInt()

        return (high shl 16) or low
    }

    private infix fun UShort.has(flag: Int): Boolean = (this.toInt() and flag) != 0
}