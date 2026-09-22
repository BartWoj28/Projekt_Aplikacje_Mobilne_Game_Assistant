package com.example.projektmobilki.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Style
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.projektmobilki.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.projektmobilki.ui.theme.ProjektMobilkiTheme

@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    hasActiveGame: Boolean,
    onOpenChess: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenRandomizer: () -> Unit,
    onOpenVirtualDeck: () -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Game Assistant",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        LargeFloatingActionButton(
            onClick = onStartGame,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (hasActiveGame) "Continue" else "Start Game")
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenChess,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(painterResource(id = R.drawable.ic_pawn), contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chess Match")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenRandomizer,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Rounded.Casino, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Randomizer")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenVirtualDeck,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Rounded.Style, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Virtual Deck")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenHistory,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Rounded.Style, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Game History")
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(Icons.Rounded.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Settings")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainMenuScreenPreview() {
    ProjektMobilkiTheme {
        MainMenuScreen(
            onStartGame = {},
            hasActiveGame = false,
            onOpenChess = {},
            onOpenSettings = {},
            onOpenRandomizer = {},
            onOpenVirtualDeck = {},
            onOpenHistory = {}
        )
    }
}
