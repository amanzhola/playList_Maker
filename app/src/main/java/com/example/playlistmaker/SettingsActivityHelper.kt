package com.example.playlistmaker

import android.view.View
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class SettingsActivityHelper(private val activity: AppCompatActivity) {

    private lateinit var backgroundSwitch: Switch

    private lateinit var textSwitch: TextView
    private lateinit var textShare: TextView
    private lateinit var textSupport: TextView
    private lateinit var textAgreement: TextView
    private lateinit var maincolor: View
    private lateinit var topright: View

    private lateinit var panelHeaderMiddleTitle: TextView
    private lateinit var buttonSearch: ImageView

    fun setupViews() {
        activity.setContentView(R.layout.activity_settings)

        // Инициализация элементов
        backgroundSwitch = activity.findViewById(R.id.switch_control)
        textSwitch = activity.findViewById(R.id.switch_note)
        textShare = activity.findViewById(R.id.share_note)
        textSupport = activity.findViewById(R.id.support_note)
        textAgreement = activity.findViewById(R.id.agreement_note)
        maincolor = activity.findViewById(R.id.settings_light_mode)
        topright = activity.findViewById(R.id.Button_Search_Light_ModeRight)

        panelHeaderMiddleTitle = activity.findViewById(R.id.x_Panel_Header_Middle_1Title)
        buttonSearch = activity.findViewById(R.id.Button_Search_Light_ModeLeft)

        // Настройка переключателя
        backgroundSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setLightMode(
                    textSwitch,
                    textShare,
                    textSupport,
                    textAgreement,
                    panelHeaderMiddleTitle,
                    buttonSearch,
                    topright,
                    maincolor
                )
            } else {
                setDarkMode(
                    textSwitch,
                    textShare,
                    textSupport,
                    textAgreement,
                    panelHeaderMiddleTitle,
                    buttonSearch,
                    topright,
                    maincolor
                )
            }
        }
    }

    private fun setLightMode(vararg views: View) {
        (views[0] as TextView).setTextColor(ContextCompat.getColor(activity, R.color.black))
        (views[1] as TextView).setTextColor(ContextCompat.getColor(activity, R.color.black))
        (views[2] as TextView).setTextColor(ContextCompat.getColor(activity, R.color.black))
        (views[3] as TextView).setTextColor(ContextCompat.getColor(activity, R.color.black))

        (views[4] as TextView).apply {
            gravity = android.view.Gravity.CENTER
            setTextColor(ContextCompat.getColor(activity, R.color.black))
            setBackgroundColor(ContextCompat.getColor(activity, R.color.white))
        }

        (views[5] as ImageView).imageTintList = ContextCompat.getColorStateList(activity, R.color.black)

        activity.findViewById<View>(R.id.activity_settings).setBackgroundColor(ContextCompat.getColor(activity, R.color.white))
        activity.findViewById<View>(R.id.settings_light_mode).setBackgroundColor(ContextCompat.getColor(activity, R.color.white))
        activity.findViewById<View>(R.id.Button_Search_Light_ModeRight).setBackgroundColor(
            ContextCompat.getColor(activity, R.color.white))
        activity.findViewById<View>(R.id.headerbetweenspace).setBackgroundColor(ContextCompat.getColor(activity, R.color.white))
    }

    private fun setDarkMode(vararg views: View) {
        (views[0] as TextView).setTextColor(ContextCompat.getColor(activity, R.color.white))
        (views[1] as TextView).setTextColor(ContextCompat.getColor(activity, R.color.white))
        (views[2] as TextView).setTextColor(ContextCompat.getColor(activity, R.color.white))
        (views[3] as TextView).setTextColor(ContextCompat.getColor(activity, R.color.white))

        (views[4] as TextView).apply {
            gravity = android.view.Gravity.CENTER or android.view.Gravity.START
            setTextColor(ContextCompat.getColor(activity, R.color.white))
            setBackgroundColor(ContextCompat.getColor(activity, R.color.black))
        }

        (views[5] as ImageView).imageTintList = ContextCompat.getColorStateList(activity, R.color.backgroundtransparent)

        activity.findViewById<View>(R.id.activity_settings).setBackgroundColor(ContextCompat.getColor(activity, R.color.black))
        activity.findViewById<View>(R.id.settings_light_mode).setBackgroundColor(ContextCompat.getColor(activity, R.color.black))
        activity.findViewById<View>(R.id.Button_Search_Light_ModeRight).setBackgroundColor(
            ContextCompat.getColor(activity, R.color.black))
        activity.findViewById<View>(R.id.headerbetweenspace).setBackgroundColor(ContextCompat.getColor(activity, R.color.black))
    }

}