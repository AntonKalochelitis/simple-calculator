package com.wdevelop.calculator

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.animation.AnimationUtils
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.wdevelop.calculator.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private val engine = CalculatorEngine()
    private var currentExpression = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        if (android.os.Build.VERSION.SDK_INT < 35) {
            enableEdgeToEdge()
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInsets()
        setupButtons()

        if (savedInstanceState != null) {
            currentExpression = savedInstanceState.getString("expression", "")
            updateDisplay()
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

        val buttons = mapOf(
            binding.button0 to "0", binding.button1 to "1", binding.button2 to "2",
            binding.button3 to "3", binding.button4 to "4", binding.button5 to "5",
            binding.button6 to "6", binding.button7 to "7", binding.button8 to "8",
            binding.button9 to "9", binding.buttonAdd to "+", binding.buttonSub to "-",
            binding.buttonMul to "*", binding.buttonDiv to "/", 
            binding.buttonBackspace to "BACK",
            binding.buttonEqual to "=", binding.buttonClear to "AC", binding.buttonDot to ".",
            binding.buttonPercent to "%", binding.buttonOpenBracket to "(", binding.buttonCloseBracket to ")"
        )

        buttons.forEach { (button, value) ->
            button.setOnClickListener {
                it.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                it.startAnimation(fadeOut)
                it.postDelayed({
                    handleButtonClick(value)
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
                        updateDisplay(isFinal = true)
                    }
                    is CalculatorEngine.Result.Error -> {
                        binding.textView.text = result.message
                        binding.textViewPreview.text = ""
                        currentExpression = ""
                    }
                }
            }
            "BACK" -> {
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

    private fun updateDisplay(isFinal: Boolean = false) {
        binding.textView.text = if (currentExpression.isEmpty()) "0" else currentExpression
        
        // Auto scroll to the end
        binding.textView.post {
            val parent = binding.textView.parent as? android.widget.HorizontalScrollView
            parent?.fullScroll(android.view.View.FOCUS_RIGHT)
        }

        if (!isFinal && currentExpression.isNotEmpty()) {
            val previewResult = engine.evaluate(currentExpression, isLive = true)
            if (previewResult is CalculatorEngine.Result.Success) {
                binding.textViewPreview.text = previewResult.value
            } else {
                binding.textViewPreview.text = ""
            }
        } else {
            binding.textViewPreview.text = ""
        }
    }
}
