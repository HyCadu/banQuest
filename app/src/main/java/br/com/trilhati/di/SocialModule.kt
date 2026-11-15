package br.com.trilhati.di

import br.com.trilhati.BuildConfig
import br.com.trilhati.data.remote.HttpTransport
import br.com.trilhati.data.remote.SocialApi
import br.com.trilhati.data.remote.SupabaseConfig
import br.com.trilhati.data.remote.SupabaseSocialApi
import br.com.trilhati.data.remote.UrlConnectionTransport
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SocialModule {
    @Binds
    abstract fun bindTransport(impl: UrlConnectionTransport): HttpTransport

    @Binds
    @Singleton
    abstract fun bindSocialApi(impl: SupabaseSocialApi): SocialApi

    companion object {
        /** URL e chave pública vêm do gradle.properties (trilha.supabaseUrl / trilha.supabaseKey) via BuildConfig. */
        @Provides
        @Singleton
        fun provideConfig(): SupabaseConfig = SupabaseConfig(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY)

        /** Versão instalada, para comparar com a mais nova publicada. */
        @Provides
        fun provideAppVersion(): br.com.trilhati.data.update.AppVersion =
            br.com.trilhati.data.update.AppVersion(BuildConfig.VERSION_CODE, BuildConfig.VERSION_NAME)
    }
}
