package com.arjun.len_denkhata.data.utils

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Handles mathematical expression evaluation with left-to-right processing,
 * percentage calculations based on the previous number, and operator precedence.
 */
object ExpressionCalculator {

    // Operator precedence mapping (only for *, / over +, -)
    private val operatorPrecedence = mapOf(
        '+' to 1,
        '-' to 1,
        '×' to 2,
        '*' to 2,
        '÷' to 2,
        '/' to 2
    )

    /**
     * Checks if the input string contains valid mathematical operators.
     * Ensures operators are not at the start (or end for non-percentage operators).
     */
    fun isExpression(input: String): Boolean {
        if (input.length < 2) return false

        val operators = setOf('+', '-', '×', '*', '÷', '/', '%')
        val sanitizedInput = input.replace(" ", "")
        var hasOperator = false
        var hasDigitBeforeOperator = false

        for (i in sanitizedInput.indices) {
            val char = sanitizedInput[i]
            if (operators.contains(char)) {
                if (i == 0 || (i == sanitizedInput.lastIndex && char != '%')) return false
                hasOperator = true
            } else if (char.isDigit() || char == '.') {
                hasDigitBeforeOperator = true
            }
        }

        return hasOperator && hasDigitBeforeOperator
    }

    /**
     * Calculates the mathematical expression left-to-right with operator precedence.
     * Handles percentages based on the previous number (e.g., 100 + 3% = 100 + (100 * 0.03)).
     * Returns result as BigDecimal for precision.
     */
    fun calculateExpressionLeftToRight(expression: String): BigDecimal {
        if (expression.isBlank()) return BigDecimal.ZERO

        try {
            val sanitizedExpression = sanitizeExpression(expression)
            if (!isValidExpressionLeftToRight(sanitizedExpression)) return BigDecimal.ZERO
            return evaluateExpressionLeftToRight(sanitizedExpression)
        } catch (e: Exception) {
            return BigDecimal.ZERO
        }
    }

    /**
     * Maintains backward compatibility by returning Double.
     */
    fun calculateExpression(expression: String): Double {
        return calculateExpressionLeftToRight(expression).toDouble()
    }

    /**
     * Sanitizes the expression by replacing special characters (× → *, ÷ → /) and removing spaces.
     */
    private fun sanitizeExpression(expression: String): String {
        return expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace(" ", "")
    }

    /**
     * Validates the expression for left-to-right evaluation.
     * Allows expressions ending with operators and percentages.
     */
    private fun isValidExpressionLeftToRight(expression: String): Boolean {
        if (expression.isEmpty()) return false

        var lastWasOperator = false
        var lastWasDecimal = false
        var decimalInCurrentNumber = false
        var lastWasPercentage = false

        for (i in expression.indices) {
            val char = expression[i]
            when {
                char.isDigit() -> {
                    lastWasOperator = false
                    lastWasDecimal = false
                    lastWasPercentage = false
                }
                char == '.' -> {
                    if (decimalInCurrentNumber || lastWasOperator || lastWasDecimal) return false
                    decimalInCurrentNumber = true
                    lastWasDecimal = true
                    lastWasOperator = false
                    lastWasPercentage = false
                }
                char == '%' -> {
                    if (i == 0 || lastWasOperator || lastWasDecimal) return false
                    lastWasPercentage = true
                    lastWasOperator = false
                    lastWasDecimal = false
                    decimalInCurrentNumber = false
                }
                char in "+-*/".toCharArray() -> {
                    if (lastWasOperator && !lastWasPercentage) return false
                    if (i == 0) return false
                    lastWasOperator = true
                    lastWasDecimal = false
                    lastWasPercentage = false
                    decimalInCurrentNumber = false
                }
                else -> return false
            }
        }
        return true
    }

    /**
     * Evaluates the expression left-to-right, respecting * and / precedence.
     * Uses BigDecimal for precise arithmetic with large numbers.
     * Percentages are calculated using the previous number (e.g., 100 + 3% = 100 + (100 * 0.03)).
     * Returns intermediate result if the expression ends with an operator.
     */
    private fun evaluateExpressionLeftToRight(expression: String): BigDecimal {
        if (expression.isEmpty()) return BigDecimal.ZERO

        val numbers = mutableListOf<BigDecimal>()
        val operators = mutableListOf<Char>()
        var currentNumber = StringBuilder()
        val endsWithOperator = expression.last() in "+-*/".toCharArray()

        fun processCurrentNumber() {
            if (currentNumber.isNotEmpty()) {
                try {
                    numbers.add(BigDecimal(currentNumber.toString()))
                } catch (e: NumberFormatException) {
                    numbers.add(BigDecimal.ZERO)
                }
                currentNumber.clear()
            }
        }

        var i = 0
        while (i < expression.length) {
            val char = expression[i]
            when {
                char.isDigit() || char == '.' -> {
                    currentNumber.append(char)
                }
                char == '%' -> {
                    if (currentNumber.isNotEmpty()) {
                        val percentageValue = try {
                            BigDecimal(currentNumber.toString())
                        } catch (e: NumberFormatException) {
                            BigDecimal.ZERO
                        }
                        currentNumber.clear()
                        val previousNumber = numbers.lastOrNull() ?: BigDecimal.ONE
                        numbers.add(previousNumber.multiply(percentageValue).divide(BigDecimal("100"), 10, RoundingMode.HALF_UP))
                    }
                }
                char in "+-*/".toCharArray() -> {
                    processCurrentNumber()
                    operators.add(char)
                }
            }
            i++
        }

        // Process the last number only if the expression doesn't end with an operator
        if (!endsWithOperator) {
            processCurrentNumber()
        }

        // If no numbers were processed, return 0
        if (numbers.isEmpty()) return BigDecimal.ZERO

        // If the expression ends with an operator, evaluate up to the last number
        if (endsWithOperator) {
            // Evaluate multiplication and division first
            var j = 0
            while (j < operators.size && j < numbers.size - 1) {
                if (operators[j] in "*/") {
                    val a = numbers[j]
                    val b = numbers[j + 1]
                    val op = operators[j]
                    val result = when (op) {
                        '*' -> a.multiply(b)
                        '/' -> if (b != BigDecimal.ZERO) a.divide(b, 10, RoundingMode.HALF_UP) else BigDecimal.ZERO
                        else -> BigDecimal.ZERO
                    }
                    numbers[j] = result
                    numbers.removeAt(j + 1)
                    operators.removeAt(j)
                } else {
                    j++
                }
            }

            // Evaluate addition and subtraction
            var result = numbers.firstOrNull() ?: BigDecimal.ZERO
            for (k in 0 until minOf(operators.size, numbers.size - 1)) {
                val op = operators[k]
                val num = numbers[k + 1]
                result = when (op) {
                    '+' -> result.add(num)
                    '-' -> result.subtract(num)
                    else -> result
                }
            }
            return result
        }

        // Full evaluation for complete expressions
        // Evaluate multiplication and division first
        var j = 0
        while (j < operators.size) {
            if (operators[j] in "*/") {
                val a = numbers[j]
                val b = numbers[j + 1]
                val op = operators[j]
                val result = when (op) {
                    '*' -> a.multiply(b)
                    '/' -> if (b != BigDecimal.ZERO) a.divide(b, 10, RoundingMode.HALF_UP) else BigDecimal.ZERO
                    else -> BigDecimal.ZERO
                }
                numbers[j] = result
                numbers.removeAt(j + 1)
                operators.removeAt(j)
            } else {
                j++
            }
        }

        // Evaluate addition and subtraction
        var result = numbers.firstOrNull() ?: BigDecimal.ZERO
        for (k in 0 until operators.size) {
            val op = operators[k]
            val num = numbers[k + 1]
            result = when (op) {
                '+' -> result.add(num)
                '-' -> result.subtract(num)
                else -> result
            }
        }

        return result
    }

    /**
     * Formats the result, removing unnecessary decimal places for whole numbers.
     */
    fun formatResult(result: BigDecimal): String {
        // Strip trailing zeros and decimal point if the number is a whole number
        val stripped = result.stripTrailingZeros()
        return if (stripped.scale() <= 0) {
            stripped.toPlainString()
        } else {
            stripped.setScale(minOf(6, stripped.scale()), RoundingMode.HALF_UP).toPlainString()
        }
    }

    /**
     * Legacy formatResult for Double, maintained for compatibility.
     */
    fun formatResult(result: Double): String {
        return if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            "%.${getDecimalPlaces(result)}f".format(result)
        }
    }

    /**
     * Determines the number of decimal places for formatting (for Double compatibility).
     */
    private fun getDecimalPlaces(value: Double): Int {
        val str = value.toString()
        val decimalIndex = str.indexOf('.')
        return if (decimalIndex == -1) 0 else minOf(6, str.length - decimalIndex - 1)
    }
}