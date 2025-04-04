package com.wdevelop.calculator

import android.util.Log
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

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
            R.id.buttonAdd, R.id.buttonSub, R.id.buttonMul, R.id.buttonDiv, R.id.buttonBackspace,
            R.id.buttonEqual, R.id.buttonClear, R.id.buttonDot, R.id.buttonPercent
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
            R.id.buttonEqual, R.id.buttonClear, R.id.buttonDot, R.id.buttonPercent
        )

        buttons.forEach { id ->
            val button = findViewById<Button>(id)
            button.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
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
            "←" -> removeLastCharacter()
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
            display.text = getString(R.string.error_display_text)
        }
    }

    private fun removeLastCharacter() {
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.dropLast(1) // Удаляем последний символ
            display.text = if (currentExpression.isEmpty()) "0" else currentExpression
        }
    }

    private fun appendOperator(op: String) {
        if (currentExpression.isNotEmpty() && !isLastCharOperator()) {
            currentExpression += op
            display.text = currentExpression
        }
    }

    private fun appendNumber(number: String) {
        // Преобразуем запятую в точку
        val number = if (number == ",") "." else number

        // Если текущее выражение равно "0" и пользователь вводит еще один "0", то ничего не добавляем
        if (currentExpression == "0" && number == "0") {
            return
        }

        // Разделяем выражение на числа и операторы
        val parts = currentExpression.split("[-+*/%]".toRegex())
        val currentNumber = parts.lastOrNull() ?: ""

        // Проверка на наличие точки только в текущем числе
        if (number == "." && currentNumber.contains(".")) {
            // Если текущее число уже содержит точку, не добавляем её
            return
        }

        currentExpression += number
        display.text = currentExpression
    }

    private fun isLastCharOperator(): Boolean {
        return currentExpression.lastOrNull()
            ?.let { it == '+' || it == '-' || it == '*' || it == '/' || it == '%' } ?: false
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
     * Удаляет незначащие нули после десятичной точки
     */
    private fun Double.removeTrailingZeroes(): String {
        // Настраиваем формат для 10 знаков после запятой
        val decimalFormat = DecimalFormat("#.##########", DecimalFormatSymbols(Locale.US))
        decimalFormat.isDecimalSeparatorAlwaysShown = false
        return decimalFormat.format(this)
    }
}