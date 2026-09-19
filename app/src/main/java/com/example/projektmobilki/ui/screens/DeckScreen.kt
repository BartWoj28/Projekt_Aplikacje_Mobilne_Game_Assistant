package com.example.projektmobilki.ui.screens

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projektmobilki.ui.components.CardView
import com.example.projektmobilki.ui.theme.ProjektMobilkiTheme
import com.example.projektmobilki.ui.viewmodels.DeckState
import com.example.projektmobilki.ui.viewmodels.DeckViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DeckScreen(
    onBack: () -> Unit,
    showHeader: Boolean = true,
    viewModel: DeckViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    if (showHeader) {
        Scaffold(
            topBar = {
                DeckTopAppBar(onBack, viewModel, uiState)
            }
        ) { innerPadding ->
            DeckContent(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                scrollState = scrollState,
                viewModel = viewModel
            )
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            DeckTopAppBar(onBack = {}, viewModel = viewModel, uiState = uiState)
            DeckContent(
                modifier = Modifier.weight(1f),
                uiState = uiState,
                scrollState = scrollState,
                viewModel = viewModel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeckTopAppBar(
    onBack: () -> Unit,
    viewModel: DeckViewModel,
    uiState: DeckState
) {
    TopAppBar(
        title = { Text("Virtual Deck") },
        navigationIcon = {
            if (onBack != {}) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = {
            IconButton(onClick = { viewModel.drawCard() }, enabled = uiState.deck.isNotEmpty()) {
                Icon(Icons.Rounded.Add, contentDescription = "Draw Card")
            }
            IconButton(onClick = { viewModel.shuffleDeck() }, enabled = uiState.deck.isNotEmpty()) {
                Icon(Icons.Rounded.Shuffle, contentDescription = "Shuffle")
            }
            IconButton(onClick = { viewModel.resetDeck() }) {
                Icon(Icons.Rounded.Refresh, contentDescription = "Reset")
            }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DeckContent(
    uiState: DeckState,
    scrollState: ScrollState,
    viewModel: DeckViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Deck Info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Cards Remaining",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${uiState.deck.size}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (uiState.isShuffled) {
                    Text(
                        text = "Deck is shuffled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Drawn Cards
        Text(
            text = "Drawn Cards (${uiState.drawnCards.size})",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.drawnCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No cards drawn yet. Use the + button to draw.",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        } else {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.drawnCards.forEach { card ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        CardView(
                            card = card, 
                            modifier = Modifier
                                .width(120.dp)
                                .aspectRatio(0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = { viewModel.putBackCard(card) },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Put Back", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun DeckScreenPreview() {
    ProjektMobilkiTheme {
        DeckScreen(onBack = {})
    }
}
