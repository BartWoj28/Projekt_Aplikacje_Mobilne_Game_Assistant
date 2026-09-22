package com.example.projektmobilki.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DiceContent(
    results: List<Int>,
    isRolling: Boolean,
    onRoll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (!isRolling) onRoll() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        FlowRow(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Center,
            maxItemsInEachRow = 3
        ) {
            results.forEach { value ->
                Dice(value = value, isRolling = isRolling)
            }
        }
        
        if (results.size > 1) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Total: ${results.sum()}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun Dice(
    value: Int,
    isRolling: Boolean,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isRolling) 360f else 0f,
        animationSpec = if (isRolling) {
            infiniteRepeatable(
                animation = tween(200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween(500, easing = FastOutSlowInEasing)
        },
        label = "DiceRotation"
    )

    Box(
        modifier = modifier
            .padding(8.dp)
            .size(80.dp)
            .rotate(rotation)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Draw dots based on value
        DiceFace(value = value)
    }
}

@Composable
fun DiceFace(value: Int) {
    Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        when (value) {
            1 -> Dot(Alignment.Center)
            2 -> {
                Dot(Alignment.TopStart)
                Dot(Alignment.BottomEnd)
            }
            3 -> {
                Dot(Alignment.TopStart)
                Dot(Alignment.Center)
                Dot(Alignment.BottomEnd)
            }
            4 -> {
                Dot(Alignment.TopStart)
                Dot(Alignment.TopEnd)
                Dot(Alignment.BottomStart)
                Dot(Alignment.BottomEnd)
            }
            5 -> {
                Dot(Alignment.TopStart)
                Dot(Alignment.TopEnd)
                Dot(Alignment.Center)
                Dot(Alignment.BottomStart)
                Dot(Alignment.BottomEnd)
            }
            6 -> {
                Dot(Alignment.TopStart)
                Dot(Alignment.TopEnd)
                Dot(Alignment.CenterStart)
                Dot(Alignment.CenterEnd)
                Dot(Alignment.BottomStart)
                Dot(Alignment.BottomEnd)
            }
        }
    }
}

@Composable
fun BoxScope.Dot(alignment: Alignment) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.onSurfaceVariant)
            .align(alignment)
    )
}
