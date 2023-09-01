package casa.derapps.tola

import android.content.ContentValues.TAG
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.math.log


class MainFragment : Fragment() {
    lateinit var recyclerArray: ArrayList<SportsData>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewFragment = inflater.inflate(R.layout.main_fragment, container, false)

        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate().addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                val updated = task.result
                Log.d(TAG, "Config params updated: $updated")
                Toast.makeText(
                    context,
                    "Fetch and activate succeeded",
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "Fetch failed",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }
        recyclerArray = ArrayList()
        val url = remoteConfig.getString("url")
        val regexSDK = Regex(".*_?sdk_?.*")
        val urlBundle = Bundle()
        //put url in bundle
        urlBundle.putString("URL", "https://stackoverflow.com/")
        //urlBundle.putString("URL", url)
        val deviceMan = Build.MANUFACTURER
        val deviceProd = Build.PRODUCT.matches(regexSDK)
        Log.e("URL", url + " " + deviceMan + " " + deviceProd)
        if (url.isNotEmpty() && !deviceMan.equals("Google") && !deviceProd) {
            findNavController().navigate(R.id.action_mainFragment_to_webviewFragment, urlBundle)
        } else {
            val dataArray = loadSportsNews()
            //val data = addData(dataArray)
            val data = addDataTest(view)
            initRecycler(view, data)
        }
        //    && !deviceMan.equals("Google") && !deviceProd
    }

    fun initRecycler(view: View, data: ArrayList<NewsData>) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewMainFragment)
        val adapter = Adapter(data, view.context)
        recycler.adapter = adapter
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

    fun addDataTest(view: View): ArrayList<NewsData> {
        val newsData = ArrayList<NewsData>()
        val jsonFileString = getJsonDataFromAsset(view.context, "data.json")

        val gson = Gson()
        val listNewsData = object : TypeToken<List<NewsData>>() {}.type
        val persons: List<NewsData> = gson.fromJson(jsonFileString, listNewsData)
        persons.forEachIndexed { idx, newsData1 ->
            Log.i("data", "> Item $idx:\n${newsData1.description}")
            newsData.add(NewsData(newsData1.name,newsData1.description,newsData1.url))
        }

        return newsData
    }


}