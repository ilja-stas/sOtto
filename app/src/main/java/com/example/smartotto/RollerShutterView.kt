package com.example.smartotto  // Change to your actual package name
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.chip.Chip
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.SwitchCompat
import android.widget.CompoundButton
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams

class RollerShutterView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private val layouts: MutableMap<String, LinearLayout> = mutableMapOf()

    init {
        orientation = VERTICAL
        inflateLayouts()
    }

    fun get_roller_layouts(): MutableMap<String, LinearLayout> {
        return layouts
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
            val rollerClosedImage = ImageView(context).apply {
                layoutParams = LinearLayout.LayoutParams(36, 36).apply {
//                    weight = 1f // Distribute space evenly
                }
                setImageResource(R.drawable.roller_shades_closed) // Replace with your drawable
                contentDescription = "asd"
                setBackgroundColor(ContextCompat.getColor(context, R.color.app_background))
                tag = "RollerImage"
            }




            val switch_left_1  = create_switch()
            val switch_right_2 = create_switch()

            // Add the button to the horizontal layout
            horizontalLayout.addView(room_text)
            horizontalLayout.addView(rollerClosedImage)
//            horizontalLayout.addView(seekBar)
//            horizontalLayout.addView(seekBar2)
            horizontalLayout.addView(switch_left_1)
            horizontalLayout.addView(switch_right_2)
//            horizontalLayout.addView(desired_temp)
//            horizontalLayout.addView(buttonIncrease)
//            horizontalLayout.addView(humidityImage)
//            horizontalLayout.addView(actual_humidity)


            // Add the created layout to the parent view
            addView(horizontalLayout)

            // Add the created layout to the map with a key
            layouts[layoutName] = horizontalLayout
        }
    }

    private fun create_switch() : SwitchCompat {
        // Create a Switch and configure its initial state and listener
        val switch = SwitchCompat(context).apply {
            text = ""// Initial text
            isChecked = false // Initial state of the switch (OFF)

//
        }
        return switch
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
