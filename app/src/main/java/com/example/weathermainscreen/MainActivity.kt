package com.example.weathermainscreen

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.weathermainscreen.databinding.ActivityMainBinding
import org.json.JSONObject
import java.net.URL
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import java.util.logging.SimpleFormatter


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val location: String = "Moscow, RU"
    private lateinit var api: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get api from config.properties
        val properties = Properties()
        val inputStream = assets.open("config.properties")
        properties.load(inputStream)
        api = properties.getProperty("api_key")

        showCustomUI()
        weatherTask().execute()
    }
    private fun showCustomUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    inner class weatherTask() : AsyncTask<String, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
            binding.apply {
                loading.visibility = View.VISIBLE
                topSection.visibility = View.GONE
                bottomSection.visibility = View.GONE
            }
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$location&units=metric&appid=$api")
                    .readText(Charsets.UTF_8)
            } catch (e: Exception){
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val updatedAt: Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                val temperature = main.getDouble("temp").toInt().toString() + "°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")
                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val address = jsonObj.getString("name") + ", " + sys.getString("country")

                binding.location.text = address
                binding.updatedAt.text = updatedAtText
                binding.temperature.text = temperature
                binding.sunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                binding.sunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                binding.wind.text = windSpeed
                binding.pressure.text = pressure
                binding.humidity.text = humidity

                binding.loading.visibility = View.GONE
                binding.topSection.visibility = View.VISIBLE
                binding.bottomSection.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.loading.visibility = View.GONE
                binding.errorText.visibility = View.VISIBLE
            }
        }
    }
}