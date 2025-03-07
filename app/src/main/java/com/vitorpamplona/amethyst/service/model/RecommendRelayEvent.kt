package com.vitorpamplona.amethyst.service.model

import androidx.compose.runtime.Immutable
import com.vitorpamplona.amethyst.model.HexKey
import com.vitorpamplona.amethyst.model.TimeUtils
import com.vitorpamplona.amethyst.model.toHexKey
import com.vitorpamplona.amethyst.service.CryptoUtils
import java.net.URI

@Immutable
class RecommendRelayEvent(
    id: HexKey,
    pubKey: HexKey,
    createdAt: Long,
    tags: List<List<String>>,
    content: String,
    sig: HexKey,
    val lenient: Boolean = false
) : Event(id, pubKey, createdAt, kind, tags, content, sig) {

    fun relay() = if (lenient) {
        URI.create(content.trim())
    } else {
        URI.create(content)
    }

    companion object {
        const val kind = 2

        fun create(relay: URI, privateKey: ByteArray, createdAt: Long = TimeUtils.now()): RecommendRelayEvent {
            val content = relay.toString()
            val pubKey = CryptoUtils.pubkeyCreate(privateKey).toHexKey()
            val tags = listOf<List<String>>()
            val id = generateId(pubKey, createdAt, kind, tags, content)
            val sig = CryptoUtils.sign(id, privateKey)
            return RecommendRelayEvent(id.toHexKey(), pubKey, createdAt, tags, content, sig.toHexKey())
        }
    }
}
