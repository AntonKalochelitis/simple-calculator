package com.wdevelop.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Stack

class CalculatorEngine {

    sealed class Result {
        data class Success(val value: String) : Result()
        data class Error(val message: String) : Result()
    }

    fun evaluate(expression: String, isLive: Boolean = false): Result {
        if (expression.isEmpty()) return Result.Success("0")

        return try {
            var expr = expression
            if (isLive) {
                // Remove trailing operators for live preview
                while (expr.isNotEmpty() && isOperator(expr.last().toString())) {
                    expr = expr.dropLast(1)
                }
                if (expr.isEmpty()) return Result.Success("")
                
                // Balance parentheses for live preview
                val openCount = expr.count { it == '(' }
                val closeCount = expr.count { it == ')' }
                if (openCount > closeCount) {
                    expr += ")".repeat(openCount - closeCount)
                }
            }

            val processedExpression = prepareExpression(expr)
            val tokens = tokenize(processedExpression)
            val rpn = shuntingYard(tokens)
            val result = evaluateRPN(rpn)
            Result.Success(formatResult(result))
        } catch (e: ArithmeticException) {
            Result.Error(e.message ?: "Error")
        } catch (e: Exception) {
            if (isLive) Result.Success("") else Result.Error("Invalid Expression")
        }
    }

    private fun prepareExpression(expression: String): String {
        val expr = expression.replace(" ", "")
        
        val result = StringBuilder()
        for (i in expr.indices) {
            val current = expr[i]
            result.append(current)
            if (i < expr.length - 1) {
                val next = expr[i + 1]
                // Implicit multiplication: 2( -> 2*(, )( -> )*(, )2 -> )*2
                if ((current.isDigit() || current == ')') && next == '(') {
                    result.append('*')
                } else if (current == ')' && (next.isDigit() || next == '.')) {
                    result.append('*')
                }
            }
        }
        
        val finalExpr = StringBuilder()
        var prev: Char? = null
        for (char in result.toString()) {
            if (char == '-' && (prev == null || prev == '(' || isOperator(prev.toString()))) {
                finalExpr.append('u')
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
                token.first().isDigit() || (token.length > 1 && token[1].isDigit()) -> output.add(token)
                token == "(" -> operators.push(token)
                token == ")" -> {
                    while (operators.isNotEmpty() && operators.peek() != "(") {
                        output.add(operators.pop())
                    }
                    if (operators.isNotEmpty()) operators.pop()
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

    private fun evaluateRPN(rpn: List<String>): BigDecimal {
        val stack = Stack<BigDecimal>()
        val mc = MathContext(16, RoundingMode.HALF_UP)
        
        for (token in rpn) {
            val value = token.toBigDecimalOrNull()
            if (value != null) {
                stack.push(value)
            } else if (token == "u") {
                if (stack.isEmpty()) throw Exception("Invalid")
                stack.push(stack.pop().negate())
            } else {
                if (stack.size < 2) throw Exception("Invalid")
                val b = stack.pop()
                val a = stack.pop()
                stack.push(applyOperator(a, b, token, mc))
            }
        }
        if (stack.size != 1) throw Exception("Invalid")
        return stack.pop()
    }

    private fun isOperator(s: String) = s == "+" || s == "-" || s == "*" || s == "/" || s == "%"

    private fun precedence(op: String): Int = when (op) {
        "+", "-" -> 1
        "*", "/", "%" -> 2
        "u" -> 3
        else -> 0
    }

    private fun applyOperator(a: BigDecimal, b: BigDecimal, op: String, mc: MathContext): BigDecimal = when (op) {
        "+" -> a.add(b, mc)
        "-" -> a.subtract(b, mc)
        "*" -> a.multiply(b, mc)
        "/" -> {
            if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Divide by zero")
            a.divide(b, mc)
        }
        "%" -> a.multiply(b, mc).divide(BigDecimal("100"), mc)
        else -> BigDecimal.ZERO
    }

    private fun formatResult(value: BigDecimal): String {
        val stripped = value.stripTrailingZeros()
        return stripped.toPlainString()
    }

    fun isValidAppend(currentExpression: String, newChar: String): Boolean {
        if (newChar == ".") {
            if (currentExpression.isEmpty() || !currentExpression.last().isDigit()) {
                return false
            }
            val lastNumber = currentExpression.split("[-+*/%()]".toRegex()).last()
            return !lastNumber.contains(".")
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
