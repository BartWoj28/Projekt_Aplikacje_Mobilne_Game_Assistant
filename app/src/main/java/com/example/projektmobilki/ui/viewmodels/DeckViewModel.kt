package com.example.projektmobilki.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.projektmobilki.models.Card
import com.example.projektmobilki.models.Rank
import com.example.projektmobilki.models.Suit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DeckState(
    val deck: List<Card> = emptyList(),
    val drawnCards: List<Card> = emptyList(),
    val isShuffled: Boolean = false
)

class DeckViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeckState())
    val uiState: StateFlow<DeckState> = _uiState.asStateFlow()

    init {
        resetDeck()
    }

    fun resetDeck() {
        val newDeck = Suit.entries.flatMap { suit ->
            Rank.entries.map { rank ->
                Card(suit, rank)
            }
        }
        _uiState.update { 
            DeckState(deck = newDeck, drawnCards = emptyList(), isShuffled = false)
        }
    }

    fun shuffleDeck() {
        _uiState.update { currentState ->
            currentState.copy(
                deck = currentState.deck.shuffled(),
                isShuffled = true
            )
        }
    }

    fun drawCard() {
        _uiState.update { currentState ->
            if (currentState.deck.isEmpty()) return@update currentState
            
            val card = currentState.deck.first()
            currentState.copy(
                deck = currentState.deck.drop(1),
                drawnCards = listOf(card) + currentState.drawnCards
            )
        }
    }

    fun putBackCard(card: Card) {
        _uiState.update { currentState ->
            if (!currentState.drawnCards.contains(card)) return@update currentState
            currentState.copy(
                deck = currentState.deck + card,
                drawnCards = currentState.drawnCards.filter { it != card }
            )
        }
    }
}
