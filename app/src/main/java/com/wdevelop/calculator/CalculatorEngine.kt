package com.wdevelop.calculator

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.Stack

class CalculatorEngine {

    sealed class Result {
        data class Success(val value: String) : Result()
        data class Error(val message: String) : Result()
    }

    fun evaluate(expression: String): Result {
        if (expression.isEmpty()) return Result.Success("0")

        return try {
            val processedExpression = prepareExpression(expression)
            val tokens = tokenize(processedExpression)
            val rpn = shuntingYard(tokens)
            val result = evaluateRPN(rpn)
            Result.Success(formatResult(result))
        } catch (e: ArithmeticException) {
            Result.Error(e.message ?: "Error")
        } catch (e: Exception) {
            Result.Error("Invalid Expression")
        }
    }

    private fun prepareExpression(expression: String): String {
        val expr = expression.replace(" ", "")
        
        // Handle implicit multiplication: digit( -> digit*(, )( -> )*(, )digit -> )*digit
        val result = StringBuilder()
        for (i in expr.indices) {
            val current = expr[i]
            result.append(current)
            if (i < expr.length - 1) {
                val next = expr[i + 1]
                if ((current.isDigit() || current == ')') && next == '(') {
                    result.append('*')
                } else if (current == ')' && next.isDigit()) {
                    result.append('*')
                }
            }
        }
        
        // Handle unary minus at start or after (
        // Convert -X to (0-X) or just mark it? Let's use a simpler trick: replace "-" with "u" if unary
        val finalExpr = StringBuilder()
        var prev: Char? = null
        for (char in result.toString()) {
            if (char == '-' && (prev == null || prev == '(' || isOperator(prev.toString()))) {
                finalExpr.append('u') // 'u' for unary minus
            } else {
                finalExpr.append(char)
            }
            prev = char
        }
        
        return finalExpr.toString()
    }

    private fun tokenize(expression: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            when {
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                        sb.append(expression[i])
                        i++
                    }
                    tokens.add(sb.toString())
                    continue
                }
                c == '(' || c == ')' || isOperator(c.toString()) || c == 'u' -> {
                    tokens.add(c.toString())
                }
            }
            i++
        }
        return tokens
    }

    private fun shuntingYard(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = Stack<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null -> output.add(token)
                token == "(" -> operators.push(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        output.add(operators.pop())
                    }
                    if (operators.isNotEmpty()) operators.pop() // Pop "("
                }
                isOperator(token) || token == "u" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(" &&
                        precedence(operators.peek()) >= precedence(token)) {
                        output.add(operators.pop())
                    }
                    operators.push(token)
                }
            }
        }
        while (operators.isNotEmpty()) {
            output.add(operators.pop())
        }
        return output
    }

    private fun evaluateRPN(rpn: List<String>): Double {
        val stack = Stack<Double>()
        for (token in rpn) {
            val value = token.toDoubleOrNull()
            if (value != null) {
                stack.push(value)
            } else if (token == "u") {
                if (stack.isEmpty()) throw Exception("Invalid")
                stack.push(-stack.pop())
            } else {
                if (stack.size < 2) throw Exception("Invalid")
                val b = stack.pop()
                val a = stack.pop()
                stack.push(applyOperator(a, b, token))
            }
        }
        if (stack.size != 1) throw Exception("Invalid")
        return stack.pop()
    }

    private fun isOperator(s: String) = s == "+" || s == "-" || s == "*" || s == "/" || s == "%"

    private fun precedence(op: String): Int = when (op) {
        "+", "-" -> 1
        "*", "/", "%" -> 2
        "u" -> 3 // Unary minus has higher precedence
        else -> 0
    }

    private fun applyOperator(a: Double, b: Double, op: String): Double = when (op) {
        "+" -> a + b
        "-" -> a - b
        "*" -> a * b
        "/" -> {
            if (b == 0.0) throw ArithmeticException("Divide by zero")
            a / b
        }
        "%" -> a / 100 * b // Existing percentage logic
        else -> 0.0
    }

    private fun formatResult(value: Double): String {
        val decimalFormat = DecimalFormat("#.##########", DecimalFormatSymbols(Locale.US))
        decimalFormat.isDecimalSeparatorAlwaysShown = false
        return decimalFormat.format(value)
    }

    fun isValidAppend(currentExpression: String, newChar: String): Boolean {
        if (newChar == ".") {
            // Rule 2.1: Allow point only after a digit
            if (currentExpression.isEmpty() || !currentExpression.last().isDigit()) {
                return false
            }
            // Rule 2.2: Only one point per fractional number
            val lastNumber = currentExpression.split("[-+*/%()]".toRegex()).last()
            return !lastNumber.contains(".")
        }

        if (newChar == "(") {
            // Rule: Parenthesis can follow an operator or another open parenthesis
            // or it can be at the start. Implicit multiplication is handled in prepareExpression,
            // but for UI logic, we might want to restrict it or allow it.
            // Current engine handles implicit multiplication (e.g. 2( -> 2*( ), so we allow it after digits too.
            return true
        }

        if (newChar == ")") {
            val openCount = currentExpression.count { it == '(' }
            val closeCount = currentExpression.count { it == ')' }
            return openCount > closeCount && currentExpression.isNotEmpty() && (currentExpression.last().isDigit() || currentExpression.last() == ')')
        }

        if (isOperator(newChar)) {
            if (currentExpression.isEmpty()) return newChar == "-"
            val lastChar = currentExpression.last()
            if (isOperator(lastChar.toString())) return false
            if (lastChar == '(') return newChar == "-"
        }

        return true
    }
}
