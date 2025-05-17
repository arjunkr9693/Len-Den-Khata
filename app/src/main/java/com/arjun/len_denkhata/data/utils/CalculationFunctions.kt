package com.arjun.len_denkhata.data.utils

fun isExpression(input: String): Boolean {
    return input.contains("+") || input.contains("-") ||
            input.contains("*") || input.contains("/") ||
            input.contains("%")
}

fun calculateExpression(expression: String): Double {
    return try {
        val parts = expression.split("(?<=[+\\-*/%])|(?=[+\\-*/%])".toRegex())
        if (parts.size < 3) return expression.toDoubleOrNull() ?: 0.0

        var result = parts[0].toDoubleOrNull() ?: 0.0
        var i = 1
        while (i < parts.size) {
            val operator = parts[i]
            val operand = parts.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0

            when (operator) {
                "+" -> result += operand
                "-" -> result -= operand
                "*" -> result *= operand
                "/" -> if (operand != 0.0) result /= operand else return 0.0
                "%" -> result %= operand
            }
            i += 2
        }
        result
    } catch (e: Exception) {
        0.0
    }
}