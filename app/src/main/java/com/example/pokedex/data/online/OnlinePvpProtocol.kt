package com.example.pokedex.data.online

import android.util.Base64
import com.google.gson.Gson
import java.io.Serializable

private val gson = Gson()

data class RoomInvite(
    val hostAddress: String,
    val hostPort: Int,
    val roomId: String
) : Serializable

data class OnlinePacket(
    val type: String,
    val payload: String = ""
) : Serializable

object RoomInviteCodec {
    fun encode(invite: RoomInvite): String {
        val json = gson.toJson(invite)
        return Base64.encodeToString(json.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)
    }

    fun decode(code: String): RoomInvite {
        val bytes = Base64.decode(code, Base64.NO_WRAP or Base64.URL_SAFE)
        return gson.fromJson(String(bytes, Charsets.UTF_8), RoomInvite::class.java)
    }
}

object OnlinePacketCodec {
    private const val SEP = '|'

    fun encode(packet: OnlinePacket): String {
        val payload = if (packet.payload.isEmpty()) "" else encodePayload(packet.payload)
        return buildString {
            append(packet.type)
            append(SEP)
            append(payload)
        }
    }

    fun decode(line: String): OnlinePacket {
        val separatorIndex = line.indexOf(SEP)
        if (separatorIndex < 0) return OnlinePacket(type = line)
        val type = line.substring(0, separatorIndex)
        val payload = line.substring(separatorIndex + 1)
        return OnlinePacket(type = type, payload = decodePayload(payload))
    }

    private fun encodePayload(payload: String): String =
        Base64.encodeToString(payload.toByteArray(Charsets.UTF_8), Base64.NO_WRAP or Base64.URL_SAFE)

    private fun decodePayload(payload: String): String {
        if (payload.isEmpty()) return ""
        val bytes = Base64.decode(payload, Base64.NO_WRAP or Base64.URL_SAFE)
        return String(bytes, Charsets.UTF_8)
    }
}

