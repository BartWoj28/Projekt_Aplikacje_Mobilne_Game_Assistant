package com.example.projektmobilki.ui.viewmodels

import com.example.projektmobilki.models.Suit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class DeckViewModelTest {

    private lateinit var viewModel: DeckViewModel

    @Before
    fun setup() {
        viewModel = DeckViewModel()
    }

    @Test
    fun `deck initialization creates 52 cards`() {
        val state = viewModel.uiState.value
        assertEquals(52, state.deck.size)
        assertEquals(0, state.drawnCards.size)
    }

    @Test
    fun `shuffle deck changes card order`() {
        val initialDeck = viewModel.uiState.value.deck.toList()
        viewModel.shuffleDeck()
        val shuffledDeck = viewModel.uiState.value.deck
        
        assertEquals(initialDeck.size, shuffledDeck.size)
        // Note: There is a very small chance that shuffling results in the same order, 
        // but for 52 cards it's negligible.
        assertNotEquals(initialDeck, shuffledDeck)
        assertTrue(viewModel.uiState.value.isShuffled)
    }

    @Test
    fun `draw card moves card from deck to drawnCards`() {
        val initialSize = viewModel.uiState.value.deck.size
        viewModel.drawCard()
        
        val state = viewModel.uiState.value
        assertEquals(initialSize - 1, state.deck.size)
        assertEquals(1, state.drawnCards.size)
    }

    @Test
    fun `drawing all cards leaves deck empty`() {
        repeat(52) {
            viewModel.drawCard()
        }
        
        val state = viewModel.uiState.value
        assertEquals(0, state.deck.size)
        assertEquals(52, state.drawnCards.size)
    }

    @Test
    fun `draw card from empty deck does nothing`() {
        repeat(52) {
            viewModel.drawCard()
        }
        
        val stateBefore = viewModel.uiState.value
        viewModel.drawCard()
        val stateAfter = viewModel.uiState.value
        
        assertEquals(stateBefore, stateAfter)
    }

    @Test
    fun `reset deck restores 52 cards`() {
        viewModel.drawCard()
        viewModel.drawCard()
        viewModel.resetDeck()
        
        val state = viewModel.uiState.value
        assertEquals(52, state.deck.size)
        assertEquals(0, state.drawnCards.size)
    }

    @Test
    fun `put back card moves card from drawnCards to deck`() {
        viewModel.drawCard()
        val drawnCard = viewModel.uiState.value.drawnCards.first()
        
        viewModel.putBackCard(drawnCard)
        val state = viewModel.uiState.value
        assertEquals(52, state.deck.size)
        assertEquals(0, state.drawnCards.size)
        assertTrue(state.deck.contains(drawnCard))
    }
}
