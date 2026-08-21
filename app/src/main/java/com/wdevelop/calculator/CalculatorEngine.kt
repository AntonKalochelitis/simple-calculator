package com.wdevelop.calculator

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CalculatorEngine {

    sealed class Result {
        data class Success(val value: String) : Result()
        data class Error(val message: String) : Result()
    }

    fun evaluate(expression: String): Result {
        if (expression.isEmpty()) return Result.Success("0")

        return try {
            val tokens = expression.split("(?<=[-+*/%])|(?=[-+*/%])".toRegex())
            
            if (tokens.isEmpty()) return Result.Success("0")

            var result = tokens[0].toDoubleOrNull() ?: return Result.Error("Invalid Input")
            
            var i = 1
            while (i < tokens.size) {
                val operator = tokens[i]
                if (i + 1 >= tokens.size) break // Trailing operator
                
                val nextNumber = tokens[i + 1].toDoubleOrNull() ?: return Result.Error("Invalid Input")

                result = when (operator) {
                    "+" -> result + nextNumber
                    "-" -> result - nextNumber
                    "*" -> result * nextNumber
                    "/" -> {
                        if (nextNumber == 0.0) return Result.Error("Divide by zero")
                        result / nextNumber
                    }
                    "%" -> result / 100 * nextNumber
                    else -> result
                }
                i += 2
            }

            Result.Success(formatResult(result))
        } catch (e: Exception) {
            Result.Error("Error")
        }
    }

    private fun formatResult(value: Double): String {
        val decimalFormat = DecimalFormat(
            "#.##########",
            DecimalFormatSymbols(Locale.US)
        )
        decimalFormat.isDecimalSeparatorAlwaysShown = false
        return decimalFormat.format(value)
    }

    fun isValidAppend(currentExpression: String, newChar: String): Boolean {
        val operators = setOf("+", "-", "*", "/", "%")
        
        if (newChar in operators) {
            if (currentExpression.isEmpty()) return false
            val lastChar = currentExpression.last().toString()
            if (lastChar in operators) return false
            return true
        }

        if (newChar == ".") {
            val parts = currentExpression.split("[-+*/%]".toRegex())
            val currentNumber = parts.lastOrNull() ?: ""
            return !currentNumber.contains(".")
        }

        return true
    }
}
