package com.carfax.connect

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.carfax.connect.model.AuthResponse
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val clientId = "gPqPYPjI0a0Kp1vZiJWn125PX97jopSZ"
    val redirectUri = "https://auth.carfax.com/android/com.carfax.connect/callback"
    val connectQuery = "query{dealerReport(vin: \"1HGCP2F64CA104152\") {fourPillars {accident {iconUrl iconText position hasAccidents } } } }"
    val gson = Gson();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        login.setOnClickListener { loginToCarfax() }
    }

    private fun loginToCarfax(){
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object:WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                println("URL: " + request?.url.toString())
                //Toast.makeText(this@MainActivity, request?.url.toString(), Toast.LENGTH_SHORT).show()
                if(request?.url.toString().startsWith(redirectUri)){
                    val code = request?.url?.getQueryParameter("code");
                    fetchAccessToken(code!!)
                }
                return super.shouldOverrideUrlLoading(view, request)
            }
        }
        webView.loadUrl("https://auth.carfax.com/authorize?client_id=$clientId&redirect_uri=$redirectUri&state=NONCE&response_type=code&audience=https%3A%2F%2Fconnect.carfax.com&scope=offline_access")
    }

    private fun fetchAccessToken(code: String){
        val queue = Volley.newRequestQueue(this)
        val url = "https://auth.carfax.com/oauth/token"
        val request = object: StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                val authResponse = gson.fromJson(response, AuthResponse::class.java)
                println("AuthResponse $authResponse")
                authResponse.calculateExpiresAt()
                fetchCarfaxData(authResponse.accessToken);
            },
            Response.ErrorListener { println("That didn't work!") }) {

            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Content-Type" to "application/x-www-form-urlencoded");
            }

            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf("grant_type" to "authorization_code",
                                    "code" to code,
                                    "redirect_uri" to redirectUri,
                                    "client_id" to clientId);
            }
        }
        queue.add(request)
    }

    private fun fetchCarfaxData(accessToken: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://connect.carfax.com/v1/graphql"
        val request = object: StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                println("Carfax Data $response")
            },
            Response.ErrorListener { error ->  println("error ${error.localizedMessage}")}) {

            override fun getHeaders(): MutableMap<String, String> {
                return mutableMapOf("Content-Type" to "application/json",
                                    "Authorization" to "Bearer $accessToken");
            }

            override fun getParams(): MutableMap<String, String> {
                return mutableMapOf("query" to connectQuery);
            }
        }
        queue.add(request)
    }
}
