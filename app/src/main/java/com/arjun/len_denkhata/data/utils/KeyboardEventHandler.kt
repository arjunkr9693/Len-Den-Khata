package com.arjun.len_denkhata.data.utils

/**
 * Handles keyboard input for mathematical expressions, including digits, operators,
 * decimals, and percentages, with caching for performance.
 */
class KeyboardEventHandler {

    // Reusable StringBuilder to reduce memory allocations
    private val stringBuilder = StringBuilder(32)

    // Cache for last expression and result to avoid redundant calculations
    private var lastExpression = ""
    private var lastResult = ""

    /**
     * Handles digit input, preventing excessive length and leading zeros.
     */
    fun handleDigitInput(currentValue: String, digit: String): String {
        if (currentValue.length >= 20) return currentValue
        if (currentValue == "0" && digit != ".") return digit
        return currentValue + digit
    }

    /**
     * Handles operator input, replacing the last operator if another follows (except after %).
     */
    fun handleOperatorInput(currentValue: String, operator: String): String {
        if (currentValue.isEmpty()) return currentValue

        // Replace last operator if current character is also an operator (except after %)
        if (isLastCharOperator(currentValue) && !currentValue.endsWith("%")) {
            return currentValue.dropLast(1) + operator
        }

        // Prevent operator after decimal point without digits
        if (currentValue.endsWith(".")) return currentValue

        return currentValue + operator
    }

    /**
     * Handles decimal point input, ensuring only one decimal per number.
     */
    fun handleDecimalInput(currentValue: String): String {
        if (currentValue.isEmpty()) return "0."

        val lastOperatorIndex = findLastOperatorIndex(currentValue)
        val currentNumber = if (lastOperatorIndex == -1) {
            currentValue
        } else {
            currentValue.substring(lastOperatorIndex + 1)
        }

        return if (currentNumber.contains(".")) {
            currentValue
        } else {
            "$currentValue."
        }
    }

    /**
     * Handles percentage input, preventing invalid placements.
     */
    fun handlePercentageInput(currentValue: String): String {
        if (currentValue.isEmpty() || isLastCharOperator(currentValue) || currentValue.endsWith("%")) {
            return currentValue
        }
        return "$currentValue%"
    }

    /**
     * Handles backspace, removing the last character.
     */
    fun handleBackspace(currentValue: String): String {
        return if (currentValue.isNotEmpty()) {
            currentValue.dropLast(1)
        } else {
            currentValue
        }
    }

    /**
     * Calculates the expression with caching, showing intermediate results after operators.
     * Only recalculates when the expression changes significantly (e.g., new number added).
     */
    fun calculateWithCaching(expression: String, prefix: String = ""): String {
        // If the expression hasn't changed, return the cached result
        if (expression == lastExpression) {
            return lastResult
        }

        // Don't show calculation for empty input, single numbers, or incomplete decimals
        if (!shouldShowCalculation(expression)) {
            lastExpression = expression
            lastResult = ""
            return ""
        }

        // Calculate the result up to the current point
        val result = ExpressionCalculator.calculateExpressionLeftToRight(expression)
        val formattedResult = ExpressionCalculator.formatResult(result)

        // Update cache
        lastExpression = expression
        lastResult = if (prefix.isNotEmpty()) "$prefix$formattedResult" else formattedResult

        return lastResult
    }

    /**
     * Determines if a calculation should be shown.
     * Shows result after an operator or % if there's at least one number.
     */
    private fun shouldShowCalculation(expression: String): Boolean {
        if (expression.isEmpty()) return false

        val operators = setOf('+', '-', '×', '*', '÷', '/', '%')
        var hasNumber = false

        for (i in expression.indices) {
            val char = expression[i]
            when {
                char.isDigit() -> hasNumber = true
                char == '.' -> {} // Decimal is part of a number
                operators.contains(char) && hasNumber -> return true
            }
        }

        // Show calculation for single number followed by % (e.g., "3%")
        return expression.endsWith("%") && hasNumber
    }

    /**
     * Clears the calculation cache.
     */
    fun clearCache() {
        lastExpression = ""
        lastResult = ""
    }

    /**
     * Returns the final amount, using the calculated result if available.
     */
    fun getFinalAmount(currentValue: String, calculatedResult: String): Double {
        return if (calculatedResult.isNotEmpty() && shouldShowCalculation(currentValue)) {
            ExpressionCalculator.calculateExpressionLeftToRight(currentValue)
        } else {
            currentValue.toDoubleOrNull() ?: 0.0
        }
    }

    /**
     * Checks if the last character is an operator.
     */
    private fun isLastCharOperator(value: String): Boolean {
        if (value.isEmpty()) return false
        val lastChar = value.last()
        return lastChar in "+-×÷*/%".toCharArray()
    }

    /**
     * Finds the index of the last operator in the string.
     */
    private fun findLastOperatorIndex(value: String): Int {
        val operators = "+-×÷*/%".toCharArray()
        for (i in value.lastIndex downTo 0) {
            if (value[i] in operators) {
                return i
            }
        }
        return -1
    }
}