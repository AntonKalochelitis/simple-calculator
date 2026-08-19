package com.wdevelop.calculator

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var display: TextView
    private var currentExpression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Включаем edge-to-edge с обратной совместимостью.
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        val root = findViewById<LinearLayout>(R.id.rootLayout)

        // Базовый отступ интерфейса — 16dp.
        val basePadding = (16 * resources.displayMetrics.density).toInt()

        // Учитываем системные панели Android:
        // status bar, navigation bar и т.д.
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(
                basePadding + insets.left,
                basePadding + insets.top,
                basePadding + insets.right,
                basePadding + insets.bottom
            )

            windowInsets
        }

        display = findViewById(R.id.textView)

        // Применение динамического размера шрифта для кнопок.
        applyDynamicTextSize()

        // Эффекты нажатия кнопки.
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        val buttons = listOf(
            R.id.button0,
            R.id.button1,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6,
            R.id.button7,
            R.id.button8,
            R.id.button9,
            R.id.buttonAdd,
            R.id.buttonSub,
            R.id.buttonMul,
            R.id.buttonDiv,
            R.id.buttonBackspace,
            R.id.buttonEqual,
            R.id.buttonClear,
            R.id.buttonDot,
            R.id.buttonPercent
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
            R.id.button0,
            R.id.button1,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6,
            R.id.button7,
            R.id.button8,
            R.id.button9,
            R.id.buttonAdd,
            R.id.buttonSub,
            R.id.buttonMul,
            R.id.buttonDiv,
            R.id.buttonEqual,
            R.id.buttonClear,
            R.id.buttonDot,
            R.id.buttonPercent
        )

        buttons.forEach { id ->
            val button = findViewById<Button>(id)

            button.viewTreeObserver.addOnGlobalLayoutListener(
                object : ViewTreeObserver.OnGlobalLayoutListener {

                    override fun onGlobalLayout() {
                        button.viewTreeObserver.removeOnGlobalLayoutListener(this)

                        val buttonWidth = button.width
                        val buttonHeight = button.height

                        Log.d(
                            "Calculator",
                            "buttonWidth: $buttonWidth, buttonHeight: $buttonHeight"
                        )

                        val textSize = (buttonWidth * 0.34).toFloat()

                        Log.d(
                            "Calculator",
                            "textSize: $textSize"
                        )

                        button.setTextSize(
                            TypedValue.COMPLEX_UNIT_PX,
                            textSize
                        )
                    }
                }
            )
        }
    }

    private fun onButtonClick(button: Button) {
        when (button.text) {
            "AC" -> clear()
            "=" -> calculate()
            "+", "-", "*", "/", "%" ->
                appendOperator(button.text.toString())

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
            Log.e(
                "MainActivity",
                "Error calculating expression",
                e
            )

            display.text = getString(R.string.error_display_text)
        }
    }

    private fun removeLastCharacter() {
        if (currentExpression.isNotEmpty()) {
            currentExpression = currentExpression.dropLast(1)

            display.text =
                if (currentExpression.isEmpty()) {
                    "0"
                } else {
                    currentExpression
                }
        }
    }

    private fun appendOperator(op: String) {
        if (currentExpression.isNotEmpty() && !isLastCharOperator()) {
            currentExpression += op
            display.text = currentExpression
        }
    }

    private fun appendNumber(number: String) {
        val normalizedNumber =
            if (number == ",") "." else number

        if (currentExpression == "0" && normalizedNumber == "0") {
            return
        }

        val parts = currentExpression.split("[-+*/%]".toRegex())
        val currentNumber = parts.lastOrNull() ?: ""

        if (
            normalizedNumber == "." &&
            currentNumber.contains(".")
        ) {
            return
        }

        currentExpression += normalizedNumber
        display.text = currentExpression
    }

    private fun isLastCharOperator(): Boolean {
        return currentExpression.lastOrNull()
            ?.let {
                it == '+' ||
                        it == '-' ||
                        it == '*' ||
                        it == '/' ||
                        it == '%'
            } ?: false
    }

    private fun evaluateExpression(expression: String): String {
        val tokens = expression.split(
            "(?<=[-+*/%])|(?=[-+*/%])".toRegex()
        )

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

    private fun Double.removeTrailingZeroes(): String {
        val decimalFormat = DecimalFormat(
            "#.##########",
            DecimalFormatSymbols(Locale.US)
        )

        decimalFormat.isDecimalSeparatorAlwaysShown = false

        return decimalFormat.format(this)
    }
}