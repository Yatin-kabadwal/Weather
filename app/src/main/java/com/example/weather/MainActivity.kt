package com.example.weather

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weather.databinding.ActivityMainBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setContentView(binding.root)

        fetchWeatherData("Haldwani")
        setupSearch()
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    fetchWeatherData(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?) = true
        })
    }

    private fun fetchWeatherData(cityName: String) {
        binding.progressBar.visibility = View.VISIBLE

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(Apiinterface::class.java)

        retrofit.getWeatherData(
            city = cityName,
            appid = "8bb2a91a42814e64e4a6ded7572b830c",
            units = "metric"
        ).enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                binding.progressBar.visibility = View.GONE
                val data = response.body()
                if (response.isSuccessful && data != null) {
                    // Set data
                    binding.temp.text = "${data.main.temp} °C"
                    binding.weather.text = data.weather.firstOrNull()?.main ?: "N/A"
                    binding.maxTemp.text = "Max : ${data.main.temp_max} °C"
                    binding.minTemp.text = "Min : ${data.main.temp_min} °C"
                    binding.humidity.text = "${data.main.humidity} %"
                    binding.windspeed.text = "${data.wind.speed} m/s"
                    binding.sunrise.text = time(data.sys.sunrise.toLong())
                    binding.sunset.text = time(data.sys.sunset.toLong())
                    binding.sea.text = "${data.main.pressure} hPa"
                    binding.condition.text = data.weather.firstOrNull()?.main ?: "N/A"
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityName.text = cityName

                    changeImagesAccordingToWeatherCondition(data.weather.firstOrNull()?.main ?: "")

                    // Apply animations
                    applyViewAnimations()
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                Log.e("WeatherAPI", "Error: ${t.message}", t)
            }
        })
    }

    private fun changeImagesAccordingToWeatherCondition(condition: String) {
        when (condition) {
            "Clear", "Sunny", "Clear Sky" -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.cloud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Light Rain", "Drizzle", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun applyViewAnimations() {
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom)
        val fadeScale = AnimationUtils.loadAnimation(this, R.anim.fade_scale)

        binding.temp.startAnimation(fadeScale)
        binding.cityName.startAnimation(fadeIn)
        binding.lottieAnimationView.startAnimation(fadeScale)
        binding.weather.startAnimation(fadeIn)
        binding.maxTemp.startAnimation(fadeIn)
        binding.minTemp.startAnimation(fadeIn)
        binding.day.startAnimation(fadeIn)
        binding.date.startAnimation(fadeIn)
        binding.condition.startAnimation(fadeIn)
        binding.sunrise.startAnimation(fadeIn)
        binding.sunset.startAnimation(fadeIn)
        binding.humidity.startAnimation(fadeIn)
        binding.windspeed.startAnimation(fadeIn)
        binding.sea.startAnimation(fadeIn)
        binding.searchView.startAnimation(fadeIn)
        binding.frameLayout.startAnimation(slideIn)
    }

    private fun date(): String {
        return SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun time(timestamp: Long): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp * 1000))
    }

    private fun dayName(timestamp: Long): String {
        return SimpleDateFormat("EEEE", Locale.getDefault()).format(Date(timestamp))
    }
}
