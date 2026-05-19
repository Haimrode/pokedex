package com.example.pokedex

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import com.example.pokedex.data.preload.PreloadManager
// No coroutine objects needed here; PreloadManager gère son scope.

@HiltAndroidApp
class PokedexApp : Application() {
	@Inject
	lateinit var preloadManager: PreloadManager

	// onCreate est le bon endroit pour déclencher le preload non bloquant
	override fun onCreate() {
		super.onCreate()
		try {
			// Lancement non-bloquant du preload en arrière-plan
			preloadManager.preload()
		} catch (_: Exception) {
			// Ne pas crash l'application si l'injection/preload échoue
		}
	}
}
