package net.blakelee.sdandroid.di

import android.net.Uri
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.blakelee.sdandroid.AppState
import net.blakelee.sdandroid.network.StableDiffusionService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import javax.inject.Singleton

private const val LOCALHOST = "https://localhost"

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(LOCALHOST)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideOkHttpClient(appState: AppState): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                chain.proceed(
                    request.newBuilder()
                        .url(
                            request.url
                                .toString()
                                .replace(LOCALHOST, Uri.parse(appState.url).toString())
                        )
                        .build()
                )
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideStableDiffusionService(retrofit: Retrofit) =
        retrofit.create<StableDiffusionService>()
}