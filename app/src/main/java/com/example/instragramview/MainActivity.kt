package com.example.instragramview

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.inputmethod.EditorInfo

import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONObject
import android.content.SharedPreferences
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.net.ConnectivityManager
import android.view.View
import android.widget.ProgressBar


class MainActivity : AppCompatActivity() {
    lateinit var imagelist: ArrayList<String>
    var searchview: EditText? = null
    var progresscircular: ProgressBar? = null

    var asyncHttpClient: AsyncHttpClient? = null
    lateinit var recyclview: RecyclerView
    lateinit var url: String
    private var scrollListener: EndlessRecyclerViewScrollListener? = null
    var nextpageurl = ""
    var ImageName = ""
    var isnextpage = false

    var shareprefence: SharedPreferences? = null
    var sharedPrefereenceeditor: SharedPreferences.Editor? = null

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        shareprefence = getSharedPreferences("instagramviewshare", MODE_APPEND)

        recyclview = findViewById<RecyclerView>(R.id.recyclerview)
        asyncHttpClient = AsyncHttpClient()
        asyncHttpClient!!.setTimeout(120000)
        asyncHttpClient!!.setConnectTimeout(120000)
        asyncHttpClient!!.setResponseTimeout(120000)
        asyncHttpClient!!.setUserAgent(getString(R.string.useragent))
        asyncHttpClient!!.addHeader(
            "Authorization",
            "563492ad6f91700001000001bbe6d278259b41bd92c0abff78f7e06b"
        )



        searchview = findViewById(R.id.searchview)
        progresscircular = findViewById(R.id.progress_circular)

        searchview!!.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                imagelist = ArrayList()
                ImageName = searchview!!.text.toString()
                url = "https://api.pexels.com/v1/search?query=${ImageName}&per_page=50"
                isnextpage = false

                getImagesList(url, ImageName)
                return@OnEditorActionListener true
            }
            false
        })

        imagelist = ArrayList()
        ImageName = "cartoon"
        url = "https://api.pexels.com/v1/search?query=${ImageName}&per_page=50"
        isnextpage = false

        getImagesList(url, ImageName)
    }

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo!!.isConnected
    }

    private fun getImagesList(url: String, ImageName: String) {
        searchview!!.clearFocus()
        val `in`: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        `in`.hideSoftInputFromWindow(searchview!!.getWindowToken(), 0)

        if (isNetworkConnected()) {
            if (!isnextpage)
                progresscircular!!.visibility = View.VISIBLE
            asyncHttpClient!!.get(
                url,
                object :
                    TextHttpResponseHandler() {
                    override fun onSuccess(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseString: String
                    ) {
                        progresscircular!!.visibility = View.GONE

                        sharedPrefereenceeditor = shareprefence!!.edit()
                        sharedPrefereenceeditor!!.putString(ImageName, responseString)
                        sharedPrefereenceeditor!!.commit()

                        showresultfromSharePerf()

                    }

                    override fun onFailure(
                        statusCode: Int,
                        headers: Array<out Header>?,
                        responseString: String?,
                        throwable: Throwable?
                    ) {
                        progresscircular!!.visibility = View.GONE

                        Toast.makeText(this@MainActivity, "Api Failed", Toast.LENGTH_SHORT).show()
                    }
                })
        } else {
            showresultfromSharePerf()

        }
    }

    private fun showresultfromSharePerf() {
        if (!shareprefence!!.getString(ImageName, "").equals("")) {
            val jsonObject = JSONObject(shareprefence!!.getString(ImageName, ""))
            try {
                Log.d("ApiResponse", jsonObject.toString());
                if (jsonObject.getInt("total_results").equals(0)) {
                    Toast.makeText(
                        this@MainActivity,
                        "No data Found",
                        Toast.LENGTH_SHORT
                    ).show()
                    isnextpage = false
                } else {
                    val jsonArray = jsonObject.getJSONArray("photos")
                    try {
                        nextpageurl = jsonObject.getString("next_page")
                    } catch (e: Exception) {
                        isnextpage = false
                    }
                    for (i in 0 until jsonArray.length()) {
                        val ImageData = jsonArray.getJSONObject(i)
                        val srcdata = ImageData.getJSONObject("src")
                        imagelist!!.add(srcdata.getString("medium"))
                    }
                    if (isnextpage) {
                        recyclview.adapter!!.notifyDataSetChanged()
                    } else {
                        ShowImage()
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            if (!isNetworkConnected())
                Toast.makeText(this, "Check internet connectivity", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()

        }
    }

    private fun ShowImage() {
        recyclview = findViewById(R.id.recyclerview)
        recyclview.adapter = AdapterView(this, imagelist)
        val layoutmanager = GridLayoutManager(this, 2)
        recyclview.layoutManager = layoutmanager
        recyclview.setHasFixedSize(true)
        scrollListener = object : EndlessRecyclerViewScrollListener(layoutmanager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                isnextpage = true
                if (isNetworkConnected())
                    getImagesList(nextpageurl, ImageName)
            }
        }
        recyclview.addOnScrollListener(scrollListener!!)
    }

}