package com.aivideoeraser.android.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivideoeraser.android.data.model.SubscriptionPlanDto
import com.aivideoeraser.android.ui.theme.*
import com.aivideoeraser.android.viewmodel.TaskViewModel

@Composable
fun SubscriptionScreen(
    taskViewModel: TaskViewModel,
    onClose: () -> Unit
) {
    val plans by taskViewModel.plans.collectAsState()
    var selectedPlanId by remember { mutableStateOf("PRO_MONTHLY") }

    LaunchedEffect(Unit) {
        taskViewModel.loadPlans()
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFBBF24).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Diamond, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unlock AI Video Eraser Pro",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Highest quality inpainting without limits",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            // Benefits Checklist
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BenefitRow("Remove unlimited objects & watermarks")
                    BenefitRow("Export in Full HD 1080p & 4K Ultra")
                    BenefitRow("Up to 300 seconds video duration")
                    BenefitRow("Priority Dedicated GPU Cloud Worker queue")
                    BenefitRow("100% Watermark-free exports")
                }
            }

            // Plans List
            items(plans.ifEmpty { defaultPlans() }) { plan ->
                val isSelected = plan.id == selectedPlanId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPlanId = plan.id }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) AccentPurple else SurfaceLight,
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) SurfaceDark else DarkBackground)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = plan.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "${plan.monthlyCredits} Credits / ${plan.period}", color = TextSecondary, fontSize = 12.sp)
                        }
                        Text(
                            text = if (plan.price == 0.0) "Free" else "$${plan.price}",
                            color = if (isSelected) AccentPurple else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Action Button
            item {
                Button(
                    onClick = {
                        taskViewModel.subscribe(selectedPlanId) {
                            onClose()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Text(text = "Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun BenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
        Text(text = text, color = Color.White, fontSize = 13.sp)
    }
}

private fun defaultPlans() = listOf(
    SubscriptionPlanDto("FREE", "Free Starter", "FREE", 0.0, "USD", "lifetime", 15, "720p", false, false, 50, emptyList()),
    SubscriptionPlanDto("PRO_MONTHLY", "Pro Monthly", "PRO", 9.99, "USD", "month", 60, "1080p", true, true, 300, emptyList()),
    SubscriptionPlanDto("PRO_YEARLY", "Pro Yearly", "PRO", 59.99, "USD", "year", 120, "1080p", true, true, 500, emptyList()),
    SubscriptionPlanDto("VIP_LIFETIME", "VIP Lifetime", "VIP", 99.99, "USD", "lifetime", 300, "4K", true, true, 9999, emptyList())
)
