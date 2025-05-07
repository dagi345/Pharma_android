package com.example.pharma_connect_androids.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object LocalDateSerializer : KSerializer<LocalDate> {
    // Formatter for serializing back to simple date (can keep as YYYY-MM-DD)
    private val outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    // Formatter for deserializing the format received from backend (YYYY-MM-DDTHH:mm:ss.SSSZ)
    private val inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        // Serialize as YYYY-MM-DD
        encoder.encodeString(value.format(outputFormatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val dateString = decoder.decodeString()
        return try {
            // Parse the full date-time-zone string
            val offsetDateTime = OffsetDateTime.parse(dateString, inputFormatter)
            // Extract only the LocalDate part
            offsetDateTime.toLocalDate()
        } catch (e: DateTimeParseException) {
            // Fallback or handle potential parsing errors if format varies unexpectedly
             try {
                 // Attempt to parse as simple date if complex parsing fails
                 LocalDate.parse(dateString, outputFormatter)
             } catch (e2: DateTimeParseException) {
                 throw IllegalArgumentException("Invalid date format for LocalDate: '$dateString' - Tried ISO_OFFSET_DATE_TIME and ISO_LOCAL_DATE", e2)
             }
        }
    }
} 