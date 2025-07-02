package com.arjun.len_denkhata.data.utils

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
     * Returns intermediate result if the expression ends with an operator.
     */
    fun calculateExpressionLeftToRight(expression: String): Double {
        if (expression.isBlank()) return 0.0

        try {
            val sanitizedExpression = sanitizeExpression(expression)
            if (!isValidExpressionLeftToRight(sanitizedExpression)) return 0.0
            return evaluateExpressionLeftToRight(sanitizedExpression)
        } catch (e: Exception) {
            return 0.0
        }
    }

    /**
     * Maintains backward compatibility by calling the left-to-right calculator.
     */
    fun calculateExpression(expression: String): Double {
        return calculateExpressionLeftToRight(expression)
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
     * Percentages are calculated using the previous number (e.g., 100 + 3% = 100 + (100 * 0.03)).
     * Returns intermediate result if the expression ends with an operator.
     */
    private fun evaluateExpressionLeftToRight(expression: String): Double {
        if (expression.isEmpty()) return 0.0

        val numbers = mutableListOf<Double>()
        val operators = mutableListOf<Char>()
        var currentNumber = StringBuilder()
        val endsWithOperator = expression.last() in "+-*/".toCharArray()

        fun processCurrentNumber() {
            if (currentNumber.isNotEmpty()) {
                numbers.add(currentNumber.toString().toDoubleOrNull() ?: 0.0)
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
                        val percentageValue = currentNumber.toString().toDoubleOrNull() ?: 0.0
                        currentNumber.clear()
                        val previousNumber = numbers.lastOrNull() ?: 1.0
                        numbers.add(previousNumber * (percentageValue / 100.0))
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

        // If no numbers were processed, return 0.0
        if (numbers.isEmpty()) return 0.0

        // If the expression ends with an operator, return the result up to the last number
        if (endsWithOperator) {
            // Evaluate all complete operations
            var j = 0
            while (j < operators.size && j < numbers.size - 1) {
                if (operators[j] in "*/") {
                    val a = numbers[j]
                    val b = numbers[j + 1]
                    val op = operators[j]
                    val result = when (op) {
                        '*' -> a * b
                        '/' -> if (b != 0.0) a / b else Double.POSITIVE_INFINITY
                        else -> 0.0
                    }
                    numbers[j] = result
                    numbers.removeAt(j + 1)
                    operators.removeAt(j)
                } else {
                    j++
                }
            }

            // Evaluate addition and subtraction
            var result = numbers.firstOrNull() ?: 0.0
            for (k in 0 until minOf(operators.size, numbers.size - 1)) {
                val op = operators[k]
                val num = numbers[k + 1]
                result = when (op) {
                    '+' -> result + num
                    '-' -> result - num
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
                    '*' -> a * b
                    '/' -> if (b != 0.0) a / b else Double.POSITIVE_INFINITY
                    else -> 0.0
                }
                numbers[j] = result
                numbers.removeAt(j + 1)
                operators.removeAt(j)
            } else {
                j++
            }
        }

        // Evaluate addition and subtraction
        var result = numbers.firstOrNull() ?: 0.0
        for (k in 0 until operators.size) {
            val op = operators[k]
            val num = numbers[k + 1]
            result = when (op) {
                '+' -> result + num
                '-' -> result - num
                else -> result
            }
        }

        return result
    }

    /**
     * Formats the result, removing unnecessary decimal places for whole numbers.
     */
    fun formatResult(result: Double): String {
        return if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            "%.${getDecimalPlaces(result)}f".format(result)
        }
    }

    /**
     * Determines the number of decimal places for formatting.
     */
    private fun getDecimalPlaces(value: Double): Int {
        val str = value.toString()
        val decimalIndex = str.indexOf('.')
        return if (decimalIndex == -1) 0 else minOf(6, str.length - decimalIndex - 1)
    }
}