package com.example.testapp

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory


class MainFragment : Fragment() {
    var recyclerArray = ArrayList<SportsData>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewFragment = inflater.inflate(R.layout.main_fragment, container, false)
        loadSportsNews()
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
        val url = remoteConfig.getString("url")
        val regexSDK = Regex(".*_?sdk_?.*")
        val deviceMan = Build.MANUFACTURER
        val deviceProd = Build.PRODUCT.matches(regexSDK)
        Log.e("URL", url + " " + deviceMan + " " + deviceProd)
        if (url.isNotEmpty() && !deviceMan.equals("Google") && !deviceProd) {
            findNavController().navigate(R.id.action_mainFragment_to_webviewFragment)
        } else {
            val data = addData()
            initRecycler(view, data)
        }

    }

    fun initRecycler(view: View, data: ArrayList<Source>) {
        val recycler = view.findViewById<RecyclerView>(R.id.recyclerViewMainFragment)
        val adapter = Adapter(data, view.context)
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

    }

    fun addData(): ArrayList<Source> {
        val source = ArrayList<Source>()
        for (i in recyclerArray) {
            for (j in i.sources)
                source.add(Source(j.name, j.description, j.url))
        }
        Log.d("data",source.toString())
        return source
    }


    fun loadSportsNews() {

        val url = "https://newsapi.org/v2/top-headlines/"
        val api = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ServiceProvider::class.java)


        //val textSport = view?.findViewById<TextView>(R.id.newsMainFragment)
        CoroutineScope(Dispatchers.Main).launch {
            val response = api.getSports().awaitResponse()
            if (response.isSuccessful) {
                val sportResponse = response.body()

                if (sportResponse != null) {

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
    }

}