package com.example.smartotto  // Change to your actual package name

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import android.util.Log


class TemperatureTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    var temperature: String = "88.0"  // Default temperature
        set(value) {
            field = value
            text = field  // Update the text to display the temperature
        }

    var update_in_app: Boolean = false

    init {
        layoutParams = LinearLayout.LayoutParams(48, 48)
        setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
        textSize = 24f
        tag = "desired_temp_text"
        text = temperature  // Set the initial text
    }
}


class TemperatureControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

//    private val bosch = Bosch()
    private val tempLayouts: MutableMap<String, LinearLayout> = mutableMapOf()


    init {
        orientation = VERTICAL
        inflateLayouts()
    }

    fun get_temperature_layouts(): MutableMap<String, LinearLayout> {
        return tempLayouts
    }

    private fun inflateLayouts() {
        for (i in 1..10) {
            val layoutName = "room$i"  // or any naming convention you prefer
            val horizontalLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(5, 0, 0, 10)
            }

            val room_text = TextView(context).apply {
                text = layoutName
                layoutParams = LinearLayout.LayoutParams(180,48
                    //LinearLayout.LayoutParams.MATCH_PARENT,
                    //LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                textSize = 24f
                tag = "room_text"
            }

            // Create Image
            val thermoImage = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(36, 36).apply {
//                    weight = 1f // Distribute space evenly
                }
                setImageResource(R.drawable.baseline_thermostat_24) // Replace with your drawable
                contentDescription = context.getString(R.string.actual_temperature)
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                tag = "thermoImage"
            }

            val room_temperature = TextView(context).apply {
                text = "-0.0"
                layoutParams = LinearLayout.LayoutParams(48,48
                    //LinearLayout.LayoutParams.MATCH_PARENT,
                    //LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                textSize = 24f
                tag="room_temperature"
            }

            val desired_temp = TemperatureTextView(context).apply {
                text = "19.0"
                layoutParams = LinearLayout.LayoutParams(48,48
                    //LinearLayout.LayoutParams.MATCH_PARENT,
                    //LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                textSize = 24f
                tag="desired_temp_text"

            }

            // Create ImageButtons
            val buttonDecrease = ImageButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(60, 48).apply {
//                    weight = 1f // Distribute space evenly
                }
                setImageResource(R.drawable.baseline_arrow_downward_24) // Replace with your drawable
                contentDescription = context.getString(R.string.decrease_temperature)
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                setPadding(25, 0, 0, 0)
                tag="decrease_temp_button"

                setOnClickListener {
                    updateTemperature(desired_temp,-0.5)
                }
            }

            val buttonIncrease = ImageButton(context).apply {
                layoutParams = LinearLayout.LayoutParams(60, 48).apply {
                    //weight = 1f // Distribute space evenly
                    marginStart = 0 // Margin to the right
                }
                setImageResource(R.drawable.baseline_arrow_upward_24) // Replace with your drawable
                contentDescription = context.getString(R.string.increase_temperature)
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                setPadding(0, 0, 30, 0)
                tag="increase_temp_button"
                setOnClickListener {
                    updateTemperature(desired_temp,0.5)
                }
            }

            // Create Image
            val humidityImage = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(36, 36).apply {
//                    weight = 1f // Distribute space evenly
                }
                setImageResource(R.drawable.humidity_svgrepo_com) // Replace with your drawable
                contentDescription = context.getString(R.string.humidity)
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                tag = "humidityImage"
            }

            val actual_humidity = TextView(context).apply {
                text = "50"
                layoutParams = LinearLayout.LayoutParams(66,48
                    //LinearLayout.LayoutParams.MATCH_PARENT,
                    //LinearLayout.LayoutParams.WRAP_CONTENT,
                )
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                textSize = 24f
                tag="humidity_text"
            }

            // Add the button to the horizontal layout
            horizontalLayout.addView(room_text)
            horizontalLayout.addView(thermoImage)
            horizontalLayout.addView(room_temperature)
            horizontalLayout.addView(buttonDecrease)
            horizontalLayout.addView(desired_temp)
            horizontalLayout.addView(buttonIncrease)
            horizontalLayout.addView(humidityImage)
            horizontalLayout.addView(actual_humidity)


            // Add the created layout to the parent view
            addView(horizontalLayout)

            // Add the created layout to the map with a key
            tempLayouts[layoutName] = horizontalLayout
        }
    }

    private fun updateTemperature(roomTemperatureView: TemperatureTextView, change: Double) {
        try {
            val currentTemperature = roomTemperatureView.text.toString().toDouble()
            val newTemperature = currentTemperature + change
            roomTemperatureView.text = String.format(Locale.US, "%.1f", newTemperature)
            roomTemperatureView.update_in_app = true
            roomTemperatureView.temperature = roomTemperatureView.text.toString()

        } catch (e: Exception) {
            Log.e("TemperatureControlView", "Error updating temperature: ${e.message}", e)
        }
    }

}
