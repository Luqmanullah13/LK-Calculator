package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

sealed class EvalResult {
    data class Success(val formatted: String, val rawValue: Double) : EvalResult()
    data class Error(val message: String) : EvalResult()
}

object MathEngine {

    fun evaluate(
        expression: String,
        isDegreeMode: Boolean = true,
        decimalPlaces: Int = 2
    ): EvalResult {
        val cleaned = expression.trim()
        if (cleaned.isEmpty()) return EvalResult.Error("Empty")

        return try {
            val tokens = tokenize(cleaned)
            if (tokens.isEmpty()) return EvalResult.Error("Empty")
            val parser = Parser(tokens, isDegreeMode)
            val value = parser.parseExpression()
            if (parser.hasMore()) {
                return EvalResult.Error("Syntax Error")
            }
            if (value.isNaN() || value.isInfinite()) {
                return EvalResult.Error("Math Error")
            }
            val formatted = formatNumber(value, decimalPlaces)
            EvalResult.Success(formatted = formatted, rawValue = value)
        } catch (e: MathEvalException) {
            EvalResult.Error(e.message ?: "Error")
        } catch (_: Exception) {
            EvalResult.Error("Syntax Error")
        }
    }

    /**
     * Evaluates an in-progress expression for live preview.
     * Automatically balances unclosed parentheses when possible and returns null if incomplete.
     */
    fun evaluatePreview(
        expression: String,
        isDegreeMode: Boolean = true,
        decimalPlaces: Int = 2
    ): String? {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) return null
        val lastChar = trimmed.last()
        if (lastChar in listOf('+', '-', '×', '*', '÷', '/', '^', '(')) return null

        val openCount = trimmed.count { it == '(' }
        val closeCount = trimmed.count { it == ')' }
        val balanced = if (openCount > closeCount) {
            trimmed + ")".repeat(openCount - closeCount)
        } else {
            trimmed
        }

        return when (val result = evaluate(balanced, isDegreeMode, decimalPlaces)) {
            is EvalResult.Success -> result.formatted
            is EvalResult.Error -> null
        }
    }

    fun formatNumber(value: Double, decimalPlaces: Int): String {
        if (value.isNaN() || value.isInfinite()) return "Error"
        val cleanZero = if (abs(value) < 1e-12) 0.0 else value
        val absVal = abs(cleanZero)

        if (absVal >= 1e12 || (absVal > 0.0 && absVal < 1e-7)) {
            val sci = String.format(java.util.Locale.US, "%.${decimalPlaces.coerceAtLeast(2)}E", cleanZero)
            return sci.replace("E+", "E")
        }

        // First eliminate IEEE-754 rounding noise at 10 decimal digits
        val denoised = BigDecimal.valueOf(cleanZero)
            .setScale(10, RoundingMode.HALF_UP)

        val rounded = denoised
            .setScale(decimalPlaces.coerceIn(0, 8), RoundingMode.HALF_UP)
            .stripTrailingZeros()

        val plain = rounded.toPlainString()
        return if (plain == "-0") "0" else plain
    }

    fun countUnclosedParens(expression: String): Int {
        var balance = 0
        for (ch in expression) {
            if (ch == '(') balance++
            else if (ch == ')' && balance > 0) balance--
        }
        return balance
    }

    private class MathEvalException(message: String) : RuntimeException(message)

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val symbol: Char) : Token()
        data class Function(val name: String) : Token()
        data object LeftParen : Token()
        data object RightParen : Token()
        data object Percent : Token()
        data object Factorial : Token()
    }

    private fun tokenize(input: String): List<Token> {
        val rawTokens = mutableListOf<Token>()
        var i = 0
        val len = input.length

        while (i < len) {
            val ch = input[i]
            when {
                ch.isWhitespace() -> i++
                ch.isDigit() || ch == '.' -> {
                    val start = i
                    var hasDot = ch == '.'
                    i++
                    while (i < len) {
                        val c = input[i]
                        if (c.isDigit()) {
                            i++
                        } else if (c == '.' && !hasDot) {
                            hasDot = true
                            i++
                        } else if (c == 'E') {
                            // Scientific notation exponent (uppercase E)
                            i++
                            if (i < len && (input[i] == '+' || input[i] == '-')) {
                                i++
                            }
                            if (i >= len || !input[i].isDigit()) {
                                throw MathEvalException("Invalid Exponent")
                            }
                            while (i < len && input[i].isDigit()) {
                                i++
                            }
                            break
                        } else {
                            break
                        }
                    }
                    val numStr = input.substring(start, i)
                    val num = numStr.toDoubleOrNull() ?: throw MathEvalException("Invalid Number")
                    rawTokens.add(Token.Number(num))
                }
                ch == 'π' -> {
                    rawTokens.add(Token.Number(PI))
                    i++
                }
                ch == 'e' -> {
                    rawTokens.add(Token.Number(kotlin.math.E))
                    i++
                }
                ch == '√' -> {
                    rawTokens.add(Token.Function("sqrt"))
                    i++
                }
                ch.isLetter() -> {
                    val start = i
                    while (i < len && input[i].isLetter()) i++
                    val word = input.substring(start, i).lowercase()
                    when (word) {
                        "sin", "cos", "tan", "log", "ln", "sqrt" -> {
                            rawTokens.add(Token.Function(word))
                        }
                        "pi" -> rawTokens.add(Token.Number(PI))
                        else -> throw MathEvalException("Unknown: $word")
                    }
                }
                ch == '(' -> {
                    rawTokens.add(Token.LeftParen)
                    i++
                }
                ch == ')' -> {
                    rawTokens.add(Token.RightParen)
                    i++
                }
                ch == '%' -> {
                    rawTokens.add(Token.Percent)
                    i++
                }
                ch == '!' -> {
                    rawTokens.add(Token.Factorial)
                    i++
                }
                ch in listOf('+', '-', '×', '*', '÷', '/', '^') -> {
                    val normalized = when (ch) {
                        '*' -> '×'
                        '/' -> '÷'
                        else -> ch
                    }
                    rawTokens.add(Token.Operator(normalized))
                    i++
                }
                else -> throw MathEvalException("Invalid Symbol")
            }
        }

        // Insert implicit multiplication tokens where needed
        val result = mutableListOf<Token>()
        for (idx in rawTokens.indices) {
            val current = rawTokens[idx]
            if (idx > 0) {
                val prev = rawTokens[idx - 1]
                val prevEndsValue = prev is Token.Number ||
                    prev is Token.RightParen ||
                    prev is Token.Percent ||
                    prev is Token.Factorial
                val currStartsValue = current is Token.Number ||
                    current is Token.LeftParen ||
                    current is Token.Function
                if (prevEndsValue && currStartsValue) {
                    result.add(Token.Operator('×'))
                }
            }
            result.add(current)
        }
        return result
    }

    private class Parser(
        private val tokens: List<Token>,
        private val isDegreeMode: Boolean
    ) {
        private var pos = 0

        fun hasMore(): Boolean = pos < tokens.size

        private fun peek(): Token? = if (pos < tokens.size) tokens[pos] else null
        private fun consume(): Token = tokens[pos++]

        fun parseExpression(): Double {
            var left = parseTerm()
            while (true) {
                val op = peek()
                if (op is Token.Operator && (op.symbol == '+' || op.symbol == '-')) {
                    consume()
                    val rightTermStart = pos
                    val right = parseTerm()
                    // Smart calculator percentage for A + B% or A - B%
                    val isSimplePercent = isDirectPercentTerm(rightTermStart, pos)
                    left = if (op.symbol == '+') {
                        if (isSimplePercent) left + (left * right) else left + right
                    } else {
                        if (isSimplePercent) left - (left * right) else left - right
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun isDirectPercentTerm(startIdx: Int, endIdx: Int): Boolean {
            if (endIdx - startIdx < 2) return false
            return tokens[endIdx - 1] is Token.Percent &&
                tokens.subList(startIdx, endIdx - 1).none { it is Token.Operator && (it.symbol == '×' || it.symbol == '÷') }
        }

        private fun parseTerm(): Double {
            var left = parsePower()
            while (true) {
                val op = peek()
                if (op is Token.Operator && (op.symbol == '×' || op.symbol == '÷')) {
                    consume()
                    val right = parsePower()
                    left = if (op.symbol == '×') {
                        left * right
                    } else {
                        if (abs(right) < 1e-15) throw MathEvalException("Divide by 0")
                        left / right
                    }
                } else {
                    break
                }
            }
            return left
        }

        private fun parsePower(): Double {
            val base = parseUnary()
            val op = peek()
            return if (op is Token.Operator && op.symbol == '^') {
                consume()
                val exponent = parsePower() // Right-associative
                val res = base.pow(exponent)
                if (res.isNaN() || res.isInfinite()) throw MathEvalException("Domain Error")
                res
            } else {
                base
            }
        }

        private fun parseUnary(): Double {
            val op = peek()
            if (op is Token.Operator && (op.symbol == '+' || op.symbol == '-')) {
                consume()
                val value = parseUnary()
                return if (op.symbol == '-') -value else value
            }
            return parsePostfix()
        }

        private fun parsePostfix(): Double {
            var value = parsePrimary()
            while (true) {
                when (peek()) {
                    is Token.Percent -> {
                        consume()
                        value /= 100.0
                    }
                    is Token.Factorial -> {
                        consume()
                        value = factorial(value)
                    }
                    else -> break
                }
            }
            return value
        }

        private fun parsePrimary(): Double {
            val token = peek() ?: throw MathEvalException("Incomplete")
            return when (token) {
                is Token.Number -> {
                    consume()
                    token.value
                }
                is Token.LeftParen -> {
                    consume()
                    val value = parseExpression()
                    if (peek() !is Token.RightParen) throw MathEvalException("Missing )")
                    consume()
                    value
                }
                is Token.Function -> {
                    consume()
                    val arg = if (peek() is Token.LeftParen) {
                        consume()
                        val inner = parseExpression()
                        if (peek() !is Token.RightParen) throw MathEvalException("Missing )")
                        consume()
                        inner
                    } else {
                        parseUnary()
                    }
                    applyFunction(token.name, arg)
                }
                else -> throw MathEvalException("Syntax Error")
            }
        }

        private fun applyFunction(name: String, arg: Double): Double {
            val angleRad = if (isDegreeMode) Math.toRadians(arg) else arg
            return when (name) {
                "sin" -> sin(angleRad)
                "cos" -> cos(angleRad)
                "tan" -> {
                    val c = cos(angleRad)
                    if (abs(c) < 1e-12) throw MathEvalException("Undefined")
                    tan(angleRad)
                }
                "sqrt" -> {
                    if (arg < 0.0) throw MathEvalException("Invalid √")
                    sqrt(arg)
                }
                "log" -> {
                    if (arg <= 0.0) throw MathEvalException("Domain Error")
                    log10(arg)
                }
                "ln" -> {
                    if (arg <= 0.0) throw MathEvalException("Domain Error")
                    ln(arg)
                }
                else -> throw MathEvalException("Unknown Fn")
            }
        }

        private fun factorial(n: Double): Double {
            if (n < 0.0 || abs(n - floor(n)) > 1e-9) {
                throw MathEvalException("Invalid !")
            }
            val intVal = n.toInt()
            if (intVal > 170) throw MathEvalException("Overflow")
            var acc = 1.0
            for (i in 2..intVal) {
                acc *= i.toDouble()
            }
            return acc
        }
    }
}
