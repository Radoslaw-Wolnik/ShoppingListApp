package com.example.shoppinglistapp.data.remote.api;

import com.example.shoppinglistapp.BuildConfig;
import com.example.shoppinglistapp.data.remote.BackendUrl;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public interface ApiKeyProvider {
        String getApiKey();
    }

    public static ApiService createService(String baseUrl, ApiKeyProvider apiKeyProvider) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new ApiKeyInterceptor(apiKeyProvider));

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            clientBuilder.addInterceptor(logging);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(normalizeBaseUrl(baseUrl))
                .addConverterFactory(GsonConverterFactory.create())
                .client(clientBuilder.build())
                .build();

        return retrofit.create(ApiService.class);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        return BackendUrl.normalizeBaseUrl(baseUrl);
    }

    private static class ApiKeyInterceptor implements Interceptor {
        private final ApiKeyProvider apiKeyProvider;

        ApiKeyInterceptor(ApiKeyProvider apiKeyProvider) {
            this.apiKeyProvider = apiKeyProvider;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request original = chain.request();
            String apiKey = apiKeyProvider != null ? apiKeyProvider.getApiKey() : null;
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return chain.proceed(original);
            }

            Request request = original.newBuilder()
                    .header("X-API-Key", apiKey)
                    .build();
            return chain.proceed(request);
        }
    }
}
