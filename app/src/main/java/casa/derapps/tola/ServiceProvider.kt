package casa.derapps.tola

import retrofit2.Call
import retrofit2.http.GET

interface ServiceProvider {

    @GET("sources?category=sports&language=en&apiKey=8e8700bfe8274b6b957c1b070583a63f")
    fun getSports(): Call<SportsData>
}