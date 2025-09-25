package com.example.app

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.corapana.swipematch.GenericCardViewModel
import com.corapana.swipematch.SwipeCardStack
import com.corapana.swipematch.SwipeConfig
import com.corapana.swipematch.SwipeDirection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                DemoSwipeableCard()
            }
        }

    }
}

@Composable
fun DemoSwipeableCard() {
    val initialData: List<CardData> = emptyList()

    val demoCards = remember {
        mutableStateListOf(
            CardData(1, "Alice", "https://randomuser.me/api/portraits/women/1.jpg"),
            CardData(2, "Bob", "https://randomuser.me/api/portraits/men/2.jpg"),
            CardData(3, "Clara", "https://randomuser.me/api/portraits/women/3.jpg"),
            CardData(4, "Dave", "https://randomuser.me/api/portraits/men/4.jpg")
        )
    }

    val swipeConfig = SwipeConfig(
        enabledDirections = setOf(SwipeDirection.LEFT, SwipeDirection.RIGHT, SwipeDirection.UP),
        directionLabels = mapOf(
            SwipeDirection.LEFT to "NOPE",
            SwipeDirection.RIGHT to "LIKE",
            SwipeDirection.UP to "SUPER"
        )
    )

    var i = 0
    val viewModel = remember {
        GenericCardViewModel(
            initialData,
            loadNextCard = {
                if (demoCards.size > i) {
                    demoCards.elementAt(i++)
                } else {
                    null
                }
            })
    }


    SwipeCardStack(
        viewModel = viewModel,
        swipeConfig = swipeConfig,
        onSwipe = { card, dir -> Log.d("Swipe", "${card.name} swiped $dir") },
        onChangeFilters = {},
    ) { card, isTop, swipeDirection ->
        CardItem(
            card = card,
            isTop = isTop,
            swipeDirection = swipeDirection.value
        )
    }

}

@Composable
fun CardItem(
    card: CardData,
    modifier: Modifier = Modifier,
    isTop: Boolean = false,
    swipeDirection: SwipeDirection? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = card.imageUrl,
            contentDescription = card.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000))
                    )
                )
        )

        Text(
            text = card.name,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 20.dp, bottom = 24.dp)
        )

        if (isTop && swipeDirection != null) {
            when (swipeDirection) {
                SwipeDirection.RIGHT -> SwipeOverlay("LIKE", Color(0xFF4CAF50), Alignment.TopStart)
                SwipeDirection.LEFT -> SwipeOverlay("NOPE", Color(0xFFF44336), Alignment.TopEnd)
                SwipeDirection.UP  -> SwipeOverlay("SUPER\nLIKE", Color(0xFF03A9F4), Alignment.TopCenter)
                SwipeDirection.DOWN -> SwipeOverlay("NOPE", Color(0xFFF44336), Alignment.BottomEnd)
            }
        }
    }
}

@Composable
fun SwipeOverlay(text: String, color: Color, alignment: Alignment) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            modifier = Modifier
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                .border(3.dp, color, RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DemoSwipeableCard()
}
