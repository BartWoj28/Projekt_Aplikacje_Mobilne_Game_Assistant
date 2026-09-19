package com.example.projektmobilki.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class NavRoute : NavKey

@Serializable
data object MainMenu : NavRoute()

@Serializable
data class GameSession(val gameId: String = "default") : NavRoute()

@Serializable
data object Settings : NavRoute()

@Serializable
data object Randomizer : NavRoute()

@Serializable
data object VirtualDeck : NavRoute()

@Serializable
data object GameHistory : NavRoute()
