package com.wdevelop.calculator

import android.util.Log
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var display: TextView
    private var currentExpression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        display = findViewById(R.id.textView)

        // Применение динамического размера шрифта для кнопок
        applyDynamicTextSize()

        // Эфекты нажатия кнопки
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        val buttons = listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
            R.id.buttonAdd, R.id.buttonSub, R.id.buttonMul, R.id.buttonDiv,
            R.id.buttonEqual, R.id.buttonClear, R.id.buttonDot, R.id.buttonPercent // R.id.buttonSign, TODO: Доработать функционал
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

    private fun applyDynamicTextSize() {
        val buttons = listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
            R.id.buttonAdd, R.id.buttonSub, R.id.buttonMul, R.id.buttonDiv,
            R.id.buttonEqual, R.id.buttonClear, R.id.buttonDot, R.id.buttonPercent // R.id.buttonSign, TODO: Доработать функционал
        )

        buttons.forEach { id ->
            val button = findViewById<Button>(id)
            button.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    button.viewTreeObserver.removeOnGlobalLayoutListener(this)

                    // Получаем размеры кнопки
                    val buttonWidth = button.width
                    val buttonHeight = button.height

                    Log.d("buttonWidth", "buttonWidth: $buttonWidth")
                    Log.d("buttonHeight", "buttonHeight: $buttonHeight")

                    // Рассчитываем размер шрифта, например, 20% от высоты кнопки
                    val textSize = (buttonWidth * 0.34).toFloat()
                    Log.d("textSize", "textSize: $textSize")

                    // Устанавливаем размер шрифта
                    button.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
                }
            })
        }
    }

    private fun onButtonClick(button: Button) {
        when (button.text) {
            "AC" -> clear()
            "=" -> calculate()
            "+", "-", "*", "/", "%" -> appendOperator(button.text.toString())
            "+/-" -> toggleSign()
            else -> appendNumber(button.text.toString())
        }
    }

    private fun clear() {
        currentExpression = ""
        display.text = "0"
    }

    private fun calculate() {
        Log.d("MainActivity", "Calculate button pressed")
        Log.d("MainActivity", "CurrentExpression: $currentExpression")

        try {
            val result = evaluateExpression(currentExpression)
            Log.d("MainActivity", "Result: $result")

            display.text = result
            currentExpression = result
        } catch (e: Exception) {
            Log.e("MainActivity", "Error calculating expression", e)
            display.text = "Error"
        }
    }

    private fun appendOperator(op: String) {
        if (currentExpression.isNotEmpty() && !isLastCharOperator()) {
            currentExpression += op
            display.text = currentExpression
        }
    }

    private fun appendNumber(number: String) {
        currentExpression += number
        display.text = currentExpression
    }

    private fun toggleSign() {
        // Implement sign toggle if needed
    }

    private fun isLastCharOperator(): Boolean {
        return currentExpression.lastOrNull()?.let { it == '+' || it == '-' || it == '*' || it == '/' || it == '%' } ?: false
    }

    private fun evaluateExpression(expression: String): String {
        val tokens = expression.split("(?<=[-+*/%])|(?=[-+*/%])".toRegex())
        var result = tokens[0].toDouble()

        var i = 1
        while (i < tokens.size) {
            val operator = tokens[i]
            val nextNumber = tokens[i + 1].toDouble()
            result = when (operator) {
                "+" -> result + nextNumber
                "-" -> result - nextNumber
                "*" -> result * nextNumber
                "/" -> result / nextNumber
                "%" -> result / 100 * nextNumber
                else -> result
            }
            i += 2
        }

        return result.removeTrailingZeroes()
    }

    /**
     * удаляет незначащие нули после десятичной точки
     */
    private fun Double.removeTrailingZeroes(): String {
        return if (this == this.toInt().toDouble()) this.toInt().toString() else this.toString()
    }
}