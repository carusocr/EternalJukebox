package org.abimon.eternalJukebox.data.storage

import io.vertx.ext.web.RoutingContext
import org.abimon.eternalJukebox.EternalJukebox
import org.abimon.eternalJukebox.objects.EnumStorageType
import org.abimon.visi.io.DataSource
import org.abimon.visi.io.FileDataSource
import org.abimon.visi.io.writeTo
import java.io.File
import java.io.FileOutputStream

object LocalStorage : IStorage {
    val storageLocations: Map<EnumStorageType, File> = EnumStorageType.values().map { type -> type to File(EternalJukebox.config.storageOptions["${type.name}_FOLDER"] as? String ?: type.name.toLowerCase()) }.toMap()

    override fun shouldStore(type: EnumStorageType): Boolean = true

    override fun store(name: String, type: EnumStorageType, data: DataSource): Boolean {
        FileOutputStream(File(storageLocations[type]!!, name)).use { fos -> data.use { inputStream -> inputStream.writeTo(fos) } }
        return true
    }

    override fun provide(name: String, type: EnumStorageType): DataSource? {
        val file = File(storageLocations[type]!!, name)
        if(file.exists())
            return FileDataSource(file)
        return null
    }

    override fun provide(name: String, type: EnumStorageType, context: RoutingContext): Boolean {
        val file = File(storageLocations[type]!!, name)
        if(file.exists()) {
            context.response().sendFile(file.absolutePath)
            return true
        }

        return false
    }

    override fun isStored(name: String, type: EnumStorageType): Boolean = File(storageLocations[type]!!, name).exists()

    init {
        storageLocations.values.forEach { if(!it.exists()) it.mkdirs() }
    }
}