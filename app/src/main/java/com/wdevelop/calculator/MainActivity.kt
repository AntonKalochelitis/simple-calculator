package com.wdevelop.calculator

import android.util.Log
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var display: TextView
    private var currentNumber = ""
    private var operator = ""
    private var operand1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.textView)

        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        val buttons = listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
            R.id.buttonAdd, R.id.buttonSub, R.id.buttonMul, R.id.buttonDiv,
            R.id.buttonEqual, R.id.buttonClear, R.id.buttonDot, R.id.buttonSign, R.id.buttonPercent
        )

        buttons.forEach { id ->
            val button = findViewById<Button>(id)
            button.setOnClickListener {
                button.startAnimation(fadeOut)
                it.postDelayed({
                    onButtonClick(button)
                    button.startAnimation(fadeIn)
                }, fadeOut.duration)
            }
        }
    }

    private fun onButtonClick(button: Button) {
        when (button.text) {
            "AC" -> clear()
            "=" -> calculate()
            "+", "-", "*", "/", "%" -> setOperator(button.text.toString())
            "+/-" -> toggleSign()
            else -> appendNumber(button.text.toString())
        }
    }

    private fun clear() {
        currentNumber = ""
        operator = ""
        operand1 = null
        display.text = "0"
    }

    private fun calculate() {
        Log.d("MainActivity", "Calculate button pressed")
        Log.d("MainActivity", "CurrentNumber: $currentNumber")
        Log.d("MainActivity", "Operator: $operator")
        Log.d("MainActivity", "Operand1: $operand1")

        if (operator.isNotEmpty() && operand1 != null) {
            val op1 = operand1?.let {
                if (it.endsWith("%")) {
                    it.removeSuffix("%").toDoubleOrNull()?.div(100) ?: 0.0
                } else {
                    it.toDoubleOrNull() ?: 0.0
                }
            } ?: 0.0

            val op2 = currentNumber.let {
                if (it.endsWith("%")) {
                    it.removeSuffix("%").toDoubleOrNull()?.div(100) ?: 0.0
                } else {
                    it.toDoubleOrNull() ?: 0.0
                }
            }

            val result = when (operator) {
                "+" -> (op1 + op2).removeTrailingZeroes()
                "-" -> (op1 - op2).removeTrailingZeroes()
                "*" -> (op1 * op2).removeTrailingZeroes()
                "/" -> if (op2 != 0.0) (op1 / op2).removeTrailingZeroes() else "Error"
                "%" -> (op1 * op2 / 100).removeTrailingZeroes()
                else -> "0"
            }

            Log.d("MainActivity", "Result: $result")

            display.text = result
            currentNumber = result
            operator = ""
            operand1 = null
        }
    }

    private fun setOperator(op: String) {
        if (currentNumber.isNotEmpty()) {
            operand1 = currentNumber
            operator = op
            currentNumber = ""
            display.text = "$operand1$operator"
        }
    }

    private fun appendNumber(number: String) {
        currentNumber += number
        display.text = currentNumber
    }

    private fun toggleSign() {
        if (currentNumber.isNotEmpty()) {
            currentNumber = if (currentNumber.startsWith("-")) {
                currentNumber.substring(1)
            } else {
                "-$currentNumber"
            }
            display.text = currentNumber
        }
    }

    private fun Double.removeTrailingZeroes(): String {
        return if (this == this.toInt().toDouble()) this.toInt().toString() else this.toString()
    }
}