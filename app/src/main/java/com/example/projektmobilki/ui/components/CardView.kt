package com.example.projektmobilki.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projektmobilki.models.Card
import com.example.projektmobilki.models.Rank
import com.example.projektmobilki.models.Suit
import com.example.projektmobilki.ui.theme.ProjektMobilkiTheme

@Composable
fun CardView(
    card: Card,
    modifier: Modifier = Modifier
) {
    val contentColor = if (card.suit.isRed) Color.Red else Color.Black

    ElevatedCard(
        modifier = modifier
            .width(160.dp)
            .height(240.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = Color.White,
            contentColor = contentColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Top Left
            Column(
                modifier = Modifier.align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = card.rank.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                SuitIcon(suit = card.suit, fontSize = 16.sp)
            }

            // Center
            Text(
                text = card.suit.toSymbol(),
                modifier = Modifier.align(Alignment.Center),
                fontSize = 72.sp
            )

            // Bottom Right
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SuitIcon(suit = card.suit, fontSize = 16.sp)
                Text(
                    text = card.rank.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SuitIcon(suit: Suit, fontSize: TextUnit) {
    Text(
        text = suit.toSymbol(),
        fontSize = fontSize
    )
}

fun Suit.toSymbol(): String = when (this) {
    Suit.HEARTS -> "♥"
    Suit.DIAMONDS -> "♦"
    Suit.CLUBS -> "♣"
    Suit.SPADES -> "♠"
}

@Preview(showBackground = true)
@Composable
fun CardViewPreview() {
    ProjektMobilkiTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardView(card = Card(Suit.SPADES, Rank.ACE))
            CardView(card = Card(Suit.HEARTS, Rank.KING))
        }
    }
}
