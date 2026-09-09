package com.calcnova.app.api

import kotlinx.serialization.Serializable

/**
 * All calculator "endpoints" in this app speak JSON in and JSON out,
 * exactly like a real REST API would - but they execute locally on-device.
 * There is no database anywhere in this layer. Calculation history is
 * persisted as a plain JSON file (see core/JsonHistoryStore.kt), not a DB.
 */

@Serializable
data class ApiError(
    val success: Boolean = false,
    val errorCode: String,
    val message: String
)

// ---------- Basic / Advanced Calculator (unified expression engine) ----------

@Serializable
data class BasicCalcRequest(
    val expression: String
)

@Serializable
data class AdvancedCalcRequest(
    val expression: String,
    val angleMode: String = "DEG"
)

@Serializable
data class BasicCalcResponse(
    val success: Boolean = true,
    val expression: String,
    val result: String
)

// ---------- Angle mode (used by the advanced expression engine) ----------

enum class AngleMode { DEG, RAD, GRAD }

// ---------- Developer / Programmer Calculator ----------

@Serializable
data class BaseConvertRequest(
    val value: String,
    val fromBase: String   // BIN, OCT, DEC, HEX
)

@Serializable
data class BaseConvertResponse(
    val success: Boolean = true,
    val decimal: String,
    val binary: String,
    val octal: String,
    val hex: String
)

@Serializable
data class BitwiseRequest(
    val a: String,
    val b: String? = null,     // null for NOT
    val fromBase: String = "DEC",
    val operation: String      // AND, OR, XOR, NOT, NAND, NOR, XNOR, SHL, SHR
)

@Serializable
data class BitwiseResponse(
    val success: Boolean = true,
    val operation: String,
    val decimalResult: String,
    val binaryResult: String,
    val hexResult: String
)

// ---------- History (JSON-file backed, not DB) ----------

@Serializable
data class HistoryEntry(
    val calculatorType: String,   // BASIC, SCIENTIFIC, PROGRAMMER
    val expression: String,
    val result: String,
    val timestampMillis: Long
)

// ---------- Settings (JSON-file backed, not DB) ----------

@Serializable
data class SettingsData(
    val themeMode: String = "SYSTEM",      // SYSTEM, LIGHT, DARK
    val autoCalculate: Boolean = true,      // live result while typing, no "=" needed
    val hapticFeedback: Boolean = true,
    val defaultAngleMode: String = "DEG"    // DEG, RAD, GRAD
)
