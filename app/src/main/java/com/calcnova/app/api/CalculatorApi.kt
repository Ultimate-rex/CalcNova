package com.calcnova.app.api

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.*

/**
 * CalculatorApi simulates a REST backend entirely on-device.
 * Every public function here is one "endpoint": it accepts a request
 * data class and returns a JSON string (either a *Response or an ApiError).
 *
 * No database, no ORM, no SQL anywhere in this file or in this app.
 * Persistent state (history/settings) is plain JSON on disk.
 */
object CalculatorApi {

    private val json = Json { prettyPrint = false; ignoreUnknownKeys = true }

    // ---------------- POST /calculate (basic + scientific, unified) ----------------
    // Supports: + - * / % ^ ! ( ) decimals, functions sin/cos/tan/asin/acos/atan/
    // sinh/cosh/tanh/log/ln/sqrt/cbrt/abs, constants pi/e. Angle mode affects trig.
    fun advancedCalculate(request: AdvancedCalcRequest): String {
        return try {
            val mode = try {
                AngleMode.valueOf(request.angleMode.uppercase())
            } catch (e: Exception) {
                AngleMode.DEG
            }
            val value = ExpressionEvaluator.evaluate(request.expression, mode)
            val response = BasicCalcResponse(
                expression = request.expression,
                result = formatNumber(value)
            )
            json.encodeToString(response)
        } catch (e: Exception) {
            errorJson("CALC_ERROR", e.message ?: "Invalid expression")
        }
    }

    // ---------------- POST /basic/calculate (kept for compatibility) ----------------
    fun basicCalculate(request: BasicCalcRequest): String {
        return advancedCalculate(AdvancedCalcRequest(request.expression, "DEG"))
    }

    // ---------------- POST /developer/convert ----------------
    fun baseConvert(request: BaseConvertRequest): String {
        return try {
            val decimalValue = parseFromBase(request.value, request.fromBase)
            val response = BaseConvertResponse(
                decimal = decimalValue.toString(),
                binary = decimalValue.toString(2),
                octal = decimalValue.toString(8),
                hex = decimalValue.toString(16).uppercase()
            )
            json.encodeToString(response)
        } catch (e: Exception) {
            errorJson("BASE_CONVERT_ERROR", e.message ?: "Invalid value for base ${request.fromBase}")
        }
    }

    // ---------------- POST /developer/bitwise ----------------
    fun bitwise(request: BitwiseRequest): String {
        return try {
            val a = parseFromBase(request.a, request.fromBase)
            val b = request.b?.let { parseFromBase(it, request.fromBase) }

            val result: Long = when (request.operation.uppercase()) {
                "AND" -> a and (b ?: throw IllegalArgumentException("AND needs two operands"))
                "OR" -> a or (b ?: throw IllegalArgumentException("OR needs two operands"))
                "XOR" -> a xor (b ?: throw IllegalArgumentException("XOR needs two operands"))
                "NOT" -> a.inv()
                "NAND" -> (a and (b ?: throw IllegalArgumentException("NAND needs two operands"))).inv()
                "NOR" -> (a or (b ?: throw IllegalArgumentException("NOR needs two operands"))).inv()
                "XNOR" -> (a xor (b ?: throw IllegalArgumentException("XNOR needs two operands"))).inv()
                "SHL" -> a shl (b ?: 1L).toInt()
                "SHR" -> a shr (b ?: 1L).toInt()
                else -> throw IllegalArgumentException("Unknown operation: ${request.operation}")
            }

            val response = BitwiseResponse(
                operation = request.operation,
                decimalResult = result.toString(),
                binaryResult = java.lang.Long.toBinaryString(result),
                hexResult = java.lang.Long.toHexString(result).uppercase()
            )
            json.encodeToString(response)
        } catch (e: Exception) {
            errorJson("BITWISE_ERROR", e.message ?: "Invalid bitwise operation")
        }
    }

    // ---------------- helpers ----------------

    private fun errorJson(code: String, message: String): String {
        return json.encodeToString(ApiError(errorCode = code, message = message))
    }

    private fun parseFromBase(value: String, base: String): Long {
        val radix = when (base.uppercase()) {
            "BIN" -> 2
            "OCT" -> 8
            "DEC" -> 10
            "HEX" -> 16
            else -> throw IllegalArgumentException("Unknown base: $base")
        }
        return java.lang.Long.parseLong(value.trim(), radix)
    }

    private fun formatNumber(value: Double): String {
        if (value.isNaN()) throw ArithmeticException("Not a number")
        if (value.isInfinite()) throw ArithmeticException("Result too large")
        return if (value == value.roundToLong().toDouble() && abs(value) < 1e15) {
            value.roundToLong().toString()
        } else {
            // trim trailing zeros for readability
            val rounded = (round(value * 1e10) / 1e10)
            rounded.toString().trimEnd('0').trimEnd('.')
        }
    }
}

/**
 * Advanced recursive-descent parser/evaluator.
 * Grammar:
 *   expression := term (('+'|'-') term)*
 *   term       := unary (('*'|'/'|'%') unary)*
 *   unary      := ('-'|'+')? power
 *   power      := postfix ('^' unary)?      (right-associative)
 *   postfix    := primary ('!')*
 *   primary    := number | constant | identifier '(' expression ')' | '(' expression ')'
 *
 * Supports functions: sin cos tan asin acos atan sinh cosh tanh log ln sqrt cbrt abs
 * Supports constants: pi (or the pi symbol), e
 */
object ExpressionEvaluator {
    fun evaluate(expression: String, angleMode: AngleMode = AngleMode.DEG): Double {
        val cleaned = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .replace("π", "pi")
            .replace(" ", "")
        if (cleaned.isBlank()) throw IllegalArgumentException("Empty expression")
        val parser = Parser(cleaned, angleMode)
        val result = parser.parseExpression()
        if (!parser.isAtEnd()) throw IllegalArgumentException("Invalid expression")
        return result
    }

    private class Parser(private val text: String, private val angleMode: AngleMode) {
        private var pos = 0

        fun isAtEnd() = pos >= text.length

        fun parseExpression(): Double {
            var value = parseTerm()
            while (!isAtEnd() && (peek() == '+' || peek() == '-')) {
                val op = next()
                val rhs = parseTerm()
                value = if (op == '+') value + rhs else value - rhs
            }
            return value
        }

        private fun parseTerm(): Double {
            var value = parseUnary()
            while (!isAtEnd() && (peek() == '*' || peek() == '/' || peek() == '%')) {
                val op = next()
                val rhs = parseUnary()
                value = when (op) {
                    '*' -> value * rhs
                    '/' -> {
                        if (rhs == 0.0) throw ArithmeticException("Division by zero")
                        value / rhs
                    }
                    else -> value.mod(rhs)
                }
            }
            return value
        }

        private fun parseUnary(): Double {
            if (!isAtEnd() && peek() == '-') { next(); return -parseUnary() }
            if (!isAtEnd() && peek() == '+') { next(); return parseUnary() }
            return parsePower()
        }

        private fun parsePower(): Double {
            val base = parsePostfix()
            if (!isAtEnd() && peek() == '^') {
                next()
                val exponent = parseUnary()
                return base.pow(exponent)
            }
            return base
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (!isAtEnd() && peek() == '!') {
                next()
                value = factorial(value)
            }
            return value
        }

        private fun parsePrimary(): Double {
            if (!isAtEnd() && peek() == '(') {
                next()
                val value = parseExpression()
                expect(')')
                return value
            }
            if (!isAtEnd() && (peek().isDigit() || peek() == '.')) {
                return parseNumber()
            }
            if (!isAtEnd() && peek().isLetter()) {
                return parseIdentifierExpression()
            }
            throw IllegalArgumentException("Unexpected character at position $pos")
        }

        private fun parseIdentifierExpression(): Double {
            val start = pos
            while (!isAtEnd() && peek().isLetter()) next()
            val name = text.substring(start, pos)

            if (!isAtEnd() && peek() == '(') {
                next()
                val arg = parseExpression()
                expect(')')
                return applyFunction(name, arg)
            }

            return when (name) {
                "pi" -> PI
                "e" -> E
                else -> throw IllegalArgumentException("Unknown identifier: $name")
            }
        }

        private fun applyFunction(name: String, arg: Double): Double {
            fun toInternalAngle(deg: Double): Double = when (angleMode) {
                AngleMode.DEG -> Math.toRadians(deg)
                AngleMode.GRAD -> deg * (PI / 200.0)
                AngleMode.RAD -> deg
            }
            fun fromInternalAngle(rad: Double): Double = when (angleMode) {
                AngleMode.DEG -> Math.toDegrees(rad)
                AngleMode.GRAD -> rad * (200.0 / PI)
                AngleMode.RAD -> rad
            }
            return when (name) {
                "sin" -> sin(toInternalAngle(arg))
                "cos" -> cos(toInternalAngle(arg))
                "tan" -> tan(toInternalAngle(arg))
                "asin" -> fromInternalAngle(asin(arg))
                "acos" -> fromInternalAngle(acos(arg))
                "atan" -> fromInternalAngle(atan(arg))
                "sinh" -> sinh(arg)
                "cosh" -> cosh(arg)
                "tanh" -> tanh(arg)
                "log" -> log10(arg)
                "ln" -> ln(arg)
                "sqrt" -> sqrt(arg)
                "cbrt" -> cbrt(arg)
                "abs" -> abs(arg)
                else -> throw IllegalArgumentException("Unknown function: $name")
            }
        }

        private fun parseNumber(): Double {
            val start = pos
            while (!isAtEnd() && (peek().isDigit() || peek() == '.')) next()
            if (start == pos) throw IllegalArgumentException("Expected number at position $pos")
            return text.substring(start, pos).toDouble()
        }

        private fun factorial(value: Double): Double {
            if (value < 0 || value != floor(value)) throw IllegalArgumentException("Factorial needs a non-negative whole number")
            var result = 1.0
            var n = value
            while (n > 1) { result *= n; n -= 1 }
            return result
        }

        private fun expect(c: Char) {
            if (isAtEnd() || peek() != c) throw IllegalArgumentException("Expected '$c' at position $pos")
            next()
        }

        private fun peek(): Char = text[pos]
        private fun next(): Char = text[pos++]
    }
}
