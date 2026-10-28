package com.abhinav.caisarra.data.remote

import com.abhinav.caisarra.data.local.TokenManager
import com.abhinav.caisarra.data.remote.api.AnalysisService
import com.abhinav.caisarra.data.remote.api.AuthService
import com.abhinav.caisarra.data.remote.api.BotGameApiService
import com.abhinav.caisarra.data.remote.api.FriendsService
import com.abhinav.caisarra.data.remote.api.GamesService
import com.abhinav.caisarra.data.remote.api.MatchmakingService
import com.abhinav.caisarra.data.remote.api.NearbyService
import com.abhinav.caisarra.data.remote.api.ProfileService
import com.abhinav.caisarra.data.remote.api.RatingService
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit
import com.abhinav.caisarra.data.remote.api.CommunityService
import com.abhinav.caisarra.data.remote.api.PuzzleApiService
import com.abhinav.caisarra.data.remote.api.ClubService
import com.abhinav.caisarra.data.remote.api.LeaderboardService
import com.abhinav.caisarra.data.remote.api.DirectMessageService
import com.abhinav.caisarra.data.remote.api.PrivacyService
object RetrofitInstance {
    const val BASE_URL = "https://caisaara.duckdns.org"
    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (com.abhinav.caisarra.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val plainClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private val refreshClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val socketClient: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(20, TimeUnit.SECONDS)
        .build()

    val plainService: AuthService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(plainClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
        .create()

    @Volatile
    private var authenticatedClient: OkHttpClient? = null

    @Volatile
    private var tokenRefresher: TokenRefresher? = null

    fun getTokenRefresher(tokenManager: TokenManager): TokenRefresher =
        tokenRefresher ?: synchronized(this) {
            tokenRefresher ?: TokenRefresher(tokenManager, refreshClient)
                .also { tokenRefresher = it }
        }

    private fun getAuthenticatedClient(tokenManager: TokenManager): OkHttpClient =
        authenticatedClient ?: synchronized(this) {
            authenticatedClient ?: plainClient.newBuilder()
                .addInterceptor(AuthInterceptor(tokenManager))
                .authenticator(TokenAuthenticator(getTokenRefresher(tokenManager)))
                .build()
                .also { authenticatedClient = it }
        }

    private fun authenticatedRetrofit(tokenManager: TokenManager): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getAuthenticatedClient(tokenManager))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    fun createAuthService(tokenManager: TokenManager): AuthService =
        authenticatedRetrofit(tokenManager).create()
    fun createInviteService(tokenManager: TokenManager): InviteService =
        authenticatedRetrofit(tokenManager).create()
    fun createChatService(tokenManager: TokenManager): ChatService =
        authenticatedRetrofit(tokenManager).create()
    fun createGamesService(tokenManager: TokenManager): GamesService =
        authenticatedRetrofit(tokenManager).create()
    fun createProfileService(tokenManager: TokenManager): ProfileService =
        authenticatedRetrofit(tokenManager).create()
    fun createDrawService(tokenManager: TokenManager): DrawService =
        authenticatedRetrofit(tokenManager).create()
    fun createRatingService(tokenManager: TokenManager): RatingService =
        authenticatedRetrofit(tokenManager).create()
    fun createMatchmakingService(tokenManager: TokenManager): MatchmakingService =
        authenticatedRetrofit(tokenManager).create()
    fun createBotGameService(tokenManager: TokenManager): BotGameApiService =
        authenticatedRetrofit(tokenManager).create(BotGameApiService::class.java)
    fun createAnalysisService(tokenManager: TokenManager): AnalysisService =
        authenticatedRetrofit(tokenManager).create()
    fun createTournamentService(tokenManager: TokenManager): TournamentService =
        authenticatedRetrofit(tokenManager).create()
    fun createFriendsService(tokenManager: TokenManager): FriendsService =
        authenticatedRetrofit(tokenManager).create()
    fun createNearbyService(tokenManager: TokenManager): NearbyService =
        authenticatedRetrofit(tokenManager).create(NearbyService::class.java)
    fun createCommunityService(tokenManager: TokenManager): CommunityService =
        authenticatedRetrofit(tokenManager).create()
    fun createPuzzleService(tokenManager: TokenManager): PuzzleApiService =
        authenticatedRetrofit(tokenManager).create()
    fun createClubService(tokenManager: TokenManager): ClubService = authenticatedRetrofit(tokenManager).create()
    fun createLeaderboardService(tokenManager: TokenManager): LeaderboardService =
        authenticatedRetrofit(tokenManager).create()
    fun createDirectMessageService(tokenManager: TokenManager): DirectMessageService { return authenticatedRetrofit(tokenManager)
            .create(DirectMessageService::class.java)
    }
    fun createPrivacyService(tokenManager: TokenManager): PrivacyService =
        authenticatedRetrofit(tokenManager).create()
}
