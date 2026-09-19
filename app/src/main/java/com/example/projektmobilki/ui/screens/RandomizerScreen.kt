package com.example.projektmobilki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projektmobilki.ui.viewmodels.RandomizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomizerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RandomizerViewModel = viewModel()
) {
    val diceResults by viewModel.diceResults.collectAsState()
    val isHeads by viewModel.isHeads.collectAsState()
    val isRolling by viewModel.isRolling.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startListening()
        onDispose {
            viewModel.stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Randomizer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { viewModel.triggerAction() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = if (activeTab == 0) "Roll Dice" else "Flip Coin",
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    text = { Text("Dice") },
                    icon = { Icon(Icons.Rounded.Casino, contentDescription = null) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    text = { Text("Coin") },
                    icon = { Icon(Icons.Rounded.MonetizationOn, contentDescription = null) }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (activeTab == 0) {
                    DiceContent(results = diceResults, isRolling = isRolling)
                } else {
                    CoinContent(isHeads = isHeads, isRolling = isRolling)
                }
            }

            if (activeTab == 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Number of Dice:")
                    (1..3).forEach { count ->
                        FilterChip(
                            selected = diceResults.size == count,
                            onClick = { viewModel.setDiceCount(count) },
                            label = { Text(count.toString()) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }
    }
}
