package com.wdevelop.calculator

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorEngineTest {

    private val engine = CalculatorEngine()

    private fun eval(expr: String): String {
        val result = engine.evaluate(expr)
        return if (result is CalculatorEngine.Result.Success) result.value else (result as CalculatorEngine.Result.Error).message
    }

    @Test
    fun testBasicArithmetic() {
        assertEquals("5", eval("2+3"))
        assertEquals("1", eval("3-2"))
        assertEquals("6", eval("2*3"))
        assertEquals("2", eval("6/3"))
    }

    @Test
    fun testPrecedence() {
        assertEquals("14", eval("2+3*4"))
        assertEquals("16", eval("10+2*3"))
    }

    @Test
    fun testParentheses() {
        assertEquals("20", eval("(2+3)*4"))
        assertEquals("14", eval("2*(3+4)"))
        assertEquals("2", eval("10/(2+3)"))
    }

    @Test
    fun testNestedParentheses() {
        assertEquals("46", eval("2*(3+(4*5))"))
        assertEquals("20", eval("((2+3)*4)"))
    }

    @Test
    fun testImplicitMultiplication() {
        assertEquals("6", eval("2(3)"))
        assertEquals("14", eval("2(3+4)"))
        assertEquals("6", eval("(2)(3)"))
    }

    @Test
    fun testUnaryMinus() {
        assertEquals("-3", eval("-5+2"))
        assertEquals("-10", eval("5*(-2)"))
        assertEquals("5", eval("-(-5)"))
    }

    @Test
    fun testDivideByZero() {
        assertEquals("Divide by zero", eval("1/0"))
    }

    @Test
    fun testDecimals() {
        assertEquals("5", eval("2.5+2.5"))
        assertEquals("12", eval("(2.5+3.5)*2"))
    }

    @Test
    fun testIsValidAppend() {
        // Decimal point rules
        assertEquals(false, engine.isValidAppend("", ".")) // Not after empty
        assertEquals(false, engine.isValidAppend("2+", ".")) // Not after operator
        assertEquals(true, engine.isValidAppend("2", ".")) // Valid after digit
        assertEquals(false, engine.isValidAppend("2.5", ".")) // Already has dot in number
        assertEquals(true, engine.isValidAppend("2.5+", "3"))
        assertEquals(true, engine.isValidAppend("2.5+3", ".")) // New number, can have dot
        
        // Parentheses rules
        assertEquals(true, engine.isValidAppend("(", "("))
        assertEquals(true, engine.isValidAppend("2", "(")) // Implicit multiplication
        assertEquals(false, engine.isValidAppend("", ")")) // No open paren
        assertEquals(false, engine.isValidAppend("(", ")")) // Empty paren
        assertEquals(true, engine.isValidAppend("(2", ")")) // Valid close
    }
}
