package casa.derapps.tola

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.BuildConfig
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale


class MainFragment : Fragment(), OnClickListener {
    lateinit var recyclerArray: ArrayList<SportsData>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentView = inflater.inflate(R.layout.main_fragment, container, false)

        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                val url = remoteConfig.getString("url")
                Log.d("url1", url)
                recyclerArray = ArrayList()
                val urlBundle = Bundle()
                urlBundle.putString("URL", url)
                val deviceMan = Build.MANUFACTURER
                Log.d("url2", url)

                if (url.isNotEmpty() && !deviceMan.equals("Google") && !checkIsEmu()) {
                    findNavController().navigate(
                        R.id.action_mainFragment_to_fragmentWebView,
                        urlBundle
                    )
                } else {
                    // val dataArray = loadSportsNews()&& !deviceMan.equals("Google") && !checkIsEmu()
                    //val data = addData(dataArray)
                    fragmentView.isVisible = true
                    val data = addDataTest(fragmentView)
                    initRecycler(fragmentView, data)
                }
            }
        }

        return fragmentView
    }


    fun initRecycler(view: View, data: ArrayList<NewsData>) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewMainFragment)
        val adapter = Adapter(data, view.context)
        recycler.adapter = adapter
        adapter.setOnClickListener(this)
        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    fun addData(data: ArrayList<SportsData>): ArrayList<Source> {
        val source = ArrayList<Source>()
        Log.d("DATA", recyclerArray.toString())
        for (i in data) {
            for (j in i.sources)
                source.add(Source(j.name, j.description, j.url))
        }
        Log.d("data", source.toString())
        return source
    }

    fun loadAPI(): ServiceProvider {

        val url = "https://newsapi.org/v2/top-headlines/"
        val api = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceProvider::class.java)

        return api
    }

    fun loadSportsNews(): ArrayList<SportsData> {
        val dataSports = ArrayList<SportsData>()
        //val textSport = view?.findViewById<TextView>(R.id.newsMainFragment)
        CoroutineScope(Dispatchers.Main).launch {
            val response = loadAPI().getSports().awaitResponse()
            if (response.isSuccessful) {
                val sportResponse = response.body()
                if (sportResponse != null) {
                    dataSports.add(sportResponse)
                    recyclerArray.add(sportResponse)
                    //textSport?.text = sportResponse.sources.toString()
                    Log.d("Sport", recyclerArray.toString())
                }
            } else {
                Log.e("Error", response.errorBody().toString())
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error" + response.message(),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        return dataSports
    }

    fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    private fun checkIsEmu(): Boolean {
        if (BuildConfig.DEBUG) return false // when developer use this build on emulator

        val phoneModel = Build.MODEL
        val buildProduct = Build.PRODUCT
        val buildHardware = Build.HARDWARE
        val brand = Build.BRAND;


        var result = (Build.FINGERPRINT.startsWith("generic")
                || phoneModel.contains("google_sdk")
                || phoneModel.lowercase(Locale.getDefault()).contains("droid4x")
                || phoneModel.contains("Emulator")
                || phoneModel.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || buildHardware == "goldfish"
                || Build.BRAND.contains("google")
                || buildHardware == "vbox86"
                || buildProduct == "sdk"
                || buildProduct == "google_sdk"
                || buildProduct == "sdk_x86"
                || buildProduct == "vbox86p"
                || Build.BOARD.lowercase(Locale.getDefault()).contains("nox")
                || Build.BOOTLOADER.lowercase(Locale.getDefault()).contains("nox")
                || buildHardware.lowercase(Locale.getDefault()).contains("nox")
                || buildProduct.lowercase(Locale.getDefault()).contains("nox"))

        if (result) return true
        result =
            result or (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
        if (result) return true
        result = result or ("google_sdk" == buildProduct)
        return result
    }

    fun addDataTest(view: View): ArrayList<NewsData> {
        val newsData = ArrayList<NewsData>()
        val jsonFileString = getJsonDataFromAsset(view.context, "data.json")

        val gson = Gson()
        val listNewsData = object : TypeToken<List<NewsData>>() {}.type
        val persons: List<NewsData> = gson.fromJson(jsonFileString, listNewsData)
        persons.forEachIndexed { idx, newsData1 ->
            newsData.add(NewsData(newsData1.name, newsData1.description))
        }

        return newsData
    }

    override fun onClick(p0: View) {
        val descriptionNews = p0.findViewById<TextView>(R.id.description_recycler)?.text
        val bundleText = Bundle()
        val image = p0.findViewById<ImageView>(R.id.imageRecycler)
        bundleText.putString("Desc", descriptionNews.toString())
        val drawable1 = image.drawable
        val bitmapDrawable = drawable1 as BitmapDrawable
        val bitmap = bitmapDrawable.bitmap
        val imageString = convertImagenString(bitmap)
        bundleText.putString("image", imageString)
        Log.d("image", image.id.toString())
        findNavController().navigate(R.id.action_mainFragment_to_newsDetailsFragment, bundleText)
    }



    fun convertImagenString(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val byte_arr: ByteArray = stream.toByteArray()
        return Base64.encodeToString(byte_arr, Base64.DEFAULT)
    }

}