package com.nothing.card.di

import android.content.Context
import com.nothing.card.util.BiometricHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BiometricModule {

    @Provides
    @Singleton
    fun provideBiometricHelper(@ApplicationContext context: Context): BiometricHelper {
        return BiometricHelper(context)
    }
}
