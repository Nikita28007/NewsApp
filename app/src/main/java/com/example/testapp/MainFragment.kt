package com.example.testapp

import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings


class MainFragment : Fragment() {

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
        val url = remoteConfig.getString("url")
        val regexSDK = Regex(".*_?sdk_?.*")
        val deviceMan = Build.MANUFACTURER
        val deviceProd = Build.PRODUCT.matches(regexSDK)
        Log.e("URL", url + " " + deviceMan + " " + deviceProd)
//        if (url.isNotEmpty() && !deviceMan.equals("Google") && !deviceProd) {
//            findNavController().navigate(R.id.action_mainFragment_to_webviewFragment)
//        }
        if (url.isNotEmpty()) {
            val bundleURL = Bundle()
            val url2 = "https://github.com/"
            bundleURL.putString("URL",url2)
            findNavController().navigate(R.id.action_mainFragment_to_webviewFragment,bundleURL)
        }


    }
}