package com.wdevelop.calculator

import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wdevelop.calculator.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private val engine = CalculatorEngine()
    private var currentExpression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupButtons()
        applyDynamicTextSize()

        if (savedInstanceState != null) {
            currentExpression = savedInstanceState.getString("expression", "")
            updateDisplay()
        }
    }

    private fun applyDynamicTextSize() {
        val buttons = listOf(
            binding.button0, binding.button1, binding.button2, binding.button3,
            binding.button4, binding.button5, binding.button6, binding.button7,
            binding.button8, binding.button9, binding.buttonAdd, binding.buttonSub,
            binding.buttonMul, binding.buttonDiv, binding.buttonEqual,
            binding.buttonClear, binding.buttonDot, binding.buttonPercent
        )

        buttons.forEach { button ->
            button.viewTreeObserver.addOnGlobalLayoutListener(
                object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        button.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        val buttonWidth = button.width
                        val textSize = (buttonWidth * 0.34).toFloat()
                        button.setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSize)
                    }
                }
            )
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("expression", currentExpression)
    }

    private fun setupInsets() {
        val basePadding = (16 * resources.displayMetrics.density).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.rootLayout) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(
                basePadding + insets.left,
                basePadding + insets.top,
                basePadding + insets.right,
                basePadding + insets.bottom
            )
            windowInsets
        }
    }

    private fun setupButtons() {
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)

        val buttons = listOf(
            binding.button0, binding.button1, binding.button2, binding.button3,
            binding.button4, binding.button5, binding.button6, binding.button7,
            binding.button8, binding.button9, binding.buttonAdd, binding.buttonSub,
            binding.buttonMul, binding.buttonDiv, binding.buttonBackspace,
            binding.buttonEqual, binding.buttonClear, binding.buttonDot,
            binding.buttonPercent
        )

        buttons.forEach { button ->
            button.setOnClickListener {
                it.startAnimation(fadeOut)
                it.postDelayed({
                    handleButtonClick(button.text.toString())
                    it.startAnimation(fadeIn)
                }, fadeOut.duration)
            }
        }
    }

    private fun handleButtonClick(value: String) {
        when (value) {
            "AC" -> {
                currentExpression = ""
                updateDisplay()
            }
            "=" -> {
                val result = engine.evaluate(currentExpression)
                when (result) {
                    is CalculatorEngine.Result.Success -> {
                        currentExpression = result.value
                        updateDisplay()
                    }
                    is CalculatorEngine.Result.Error -> {
                        binding.textView.text = result.message
                        currentExpression = ""
                    }
                }
            }
            "←" -> {
                if (currentExpression.isNotEmpty()) {
                    currentExpression = currentExpression.dropLast(1)
                    updateDisplay()
                }
            }
            else -> {
                if (engine.isValidAppend(currentExpression, value)) {
                    currentExpression += value
                    updateDisplay()
                }
            }
        }
    }

    private fun updateDisplay() {
        binding.textView.text = if (currentExpression.isEmpty()) "0" else currentExpression
    }
}
