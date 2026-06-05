package com.maktab.app.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

/**
 * Moshi uchun universal Any? adapter.
 *
 * Moshi generated adapter (@JsonClass) bilan Any? maydonlari to'g'ri parse
 * qilinmasligi mumkin — JSON ichidagi son, string, object, array kabi turlar
 * null yoki noto'g'ri tip sifatida o'qiladi. Shu muammoni hal qiladi.
 */
class AnyJsonAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        if (type != Any::class.java) return null
        return AnyJsonAdapter()
    }
}

class AnyJsonAdapter : JsonAdapter<Any>() {

    override fun fromJson(reader: JsonReader): Any? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> reader.nextNull<Any>()
            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NUMBER -> {
                val d = reader.nextDouble()
                // Butun son bo'lsa Long, kasr bo'lsa Double sifatida qaytaradi
                if (d % 1.0 == 0.0 && d >= Long.MIN_VALUE.toDouble() && d <= Long.MAX_VALUE.toDouble()) {
                    d.toLong()
                } else {
                    d
                }
            }
            JsonReader.Token.STRING -> reader.nextString()
            JsonReader.Token.BEGIN_ARRAY -> {
                val list = mutableListOf<Any?>()
                reader.beginArray()
                while (reader.hasNext()) {
                    list.add(fromJson(reader))
                }
                reader.endArray()
                list
            }
            JsonReader.Token.BEGIN_OBJECT -> {
                val map = mutableMapOf<String, Any?>()
                reader.beginObject()
                while (reader.hasNext()) {
                    map[reader.nextName()] = fromJson(reader)
                }
                reader.endObject()
                map
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    override fun toJson(writer: JsonWriter, value: Any?) {
        when (value) {
            null -> writer.nullValue()
            is Boolean -> writer.value(value)
            is Long -> writer.value(value)
            is Int -> writer.value(value.toLong())
            is Double -> writer.value(value)
            is Float -> writer.value(value.toDouble())
            is Number -> writer.value(value.toDouble())
            is String -> writer.value(value)
            is List<*> -> {
                writer.beginArray()
                value.forEach { toJson(writer, it) }
                writer.endArray()
            }
            is Map<*, *> -> {
                writer.beginObject()
                value.forEach { (k, v) ->
                    writer.name(k.toString())
                    toJson(writer, v)
                }
                writer.endObject()
            }
            else -> writer.value(value.toString())
        }
    }
}
