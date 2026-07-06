package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.viewmodel.GroceryViewModel
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SpinWheelDialog(
    viewModel: GroceryViewModel,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spinAvailable by viewModel.spinWheelAvailable.collectAsState()
    val rewardPoints by viewModel.rewardPoints.collectAsState()
    val scope = rememberCoroutineScope()

    var isSpinning by remember { mutableStateOf(false) }
    var wonText by remember { mutableStateOf("") }
    val rotationAngle = remember { Animatable(0f) }

    val slices = listOf(
        Pair("+50 Pts", Color(0xFF108A43)),
        Pair("Try Again", Color(0xFFF7C11E)),
        Pair("+100 Pts", Color(0xFF00E5FF)),
        Pair("Coupon code", Color(0xFF8B5CF6)),
        Pair("+20 Pts", Color(0xFFEF4444)),
        Pair("Jackpot!", Color(0xFFEC4899))
    )

    Dialog(onDismissRequest = { if (!isSpinning) onDismiss() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // dark tech neon background
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Rewards",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSpinning,
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Points status
                Surface(
                    color = BrandGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Points", tint = BrandYellow, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "My Points: $rewardPoints Pts",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Draw sector wheel with indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(240.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
                        val radius = canvasWidth / 2f

                        // Apply animated rotation to the sectors
                        rotate(degrees = rotationAngle.value, pivot = center) {
                            val sweepAngle = 360f / slices.size
                            slices.forEachIndexed { index, slice ->
                                val startAngle = index * sweepAngle
                                drawArc(
                                    color = slice.second,
                                    startAngle = startAngle,
                                    sweepAngle = sweepAngle,
                                    useCenter = true,
                                    size = Size(canvasWidth, canvasHeight)
                                )

                                // Draw sector names
                                val textAngle = startAngle + (sweepAngle / 2f)
                                val textRad = textAngle * PI / 180f
                                val textX = center.x + (radius * 0.6f) * cos(textRad).toFloat()
                                val textY = center.y + (radius * 0.6f) * sin(textRad).toFloat()

                                drawIntoCanvas { innerCanvas ->
                                    val paint = android.graphics.Paint().apply {
                                        color = android.graphics.Color.WHITE
                                        textSize = 10.dp.toPx()
                                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                                        textAlign = android.graphics.Paint.Align.CENTER
                                    }
                                    innerCanvas.nativeCanvas.drawText(
                                        slice.first,
                                        textX,
                                        textY,
                                        paint
                                    )
                                }
                            }
                        }

                        // Outer gold boundary ring
                        drawCircle(
                            color = Color(0xFFF7C11E),
                            radius = radius,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
                        )
                    }

                    // Static Pointer indicating chosen prize (pointing downwards)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = (-14).dp)
                            .size(24.dp)
                            .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                        )
                    }

                    // Center click-to-spin core button
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .size(60.dp)
                            .clickable(enabled = spinAvailable && !isSpinning) {
                                isSpinning = true
                                scope.launch {
                                    // Animate spinning. Random additions of angles.
                                    val spins = (3..6).random() * 360f
                                    // Target Slice 3 (index 3: "Coupon code")
                                    val targetAngle = spins + (360f - (3 * 60f) - 30f)
                                    
                                    rotationAngle.animateTo(
                                        targetValue = targetAngle,
                                        animationSpec = tween(
                                            durationMillis = 4000,
                                            easing = { t ->
                                                // Cubic deceleration easing function
                                                val f = t - 1
                                                f * f * f + 1
                                            }
                                        )
                                    )

                                    // Complete spin & award points/coupons
                                    viewModel.spinWheel()
                                    wonText = "CONGRATS! 🎉\nYou won 150 Reward Points and applied 20% OFF Coupon Code: AI3DPROMO!"
                                    isSpinning = false
                                }
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (spinAvailable) "SPIN" else "DONE",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rewards results
                if (wonText.isNotEmpty()) {
                    Text(
                        text = wonText,
                        color = BrandYellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                } else if (!spinAvailable) {
                    Text(
                        text = "You've already spun the wheel today!\nYou received +150 Points and Applied Coupon **AI3DPROMO**!",
                        color = Color.LightGray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                } else {
                    Text(
                        text = "Spin the wheel to win up to 150 loyalty reward points and applied grocery discounts!",
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    enabled = !isSpinning,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}
