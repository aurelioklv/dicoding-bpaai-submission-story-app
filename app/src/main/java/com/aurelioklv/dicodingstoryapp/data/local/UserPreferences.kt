package com.aurelioklv.dicodingstoryapp.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")

class UserPreferences private constructor(private val dataStore: DataStore<Preferences>) {
    suspend fun saveSession(token: String, name: String) {
        dataStore.edit { it[TOKEN_KEY] = token }
        dataStore.edit { it[NAME_KEY] = name }
    }

    suspend fun clearSession() {
        dataStore.edit { it.remove(TOKEN_KEY) }
        dataStore.edit { it.remove(NAME_KEY) }
    }

    fun getToken(): Flow<String?> {
        return dataStore.data.map { it[TOKEN_KEY] }
    }

    fun getName(): Flow<String?> {
        return dataStore.data.map { it[NAME_KEY] }
    }

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
        private val NAME_KEY = stringPreferencesKey("name")

        @Volatile
        private var instance: UserPreferences? = null
        fun getInstance(dataStore: DataStore<Preferences>): UserPreferences {
            return instance ?: synchronized(this) {
                instance ?: UserPreferences(dataStore)
            }.also { instance = it }
        }
    }
}