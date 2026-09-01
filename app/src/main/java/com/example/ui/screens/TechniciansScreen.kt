package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.KodyarTechnician
import com.example.data.model.KodyarUser
import com.example.ui.AssistantViewModel
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TechniciansScreen(
    viewModel: AssistantViewModel,
    liveTechs: StateFlow<List<KodyarTechnician>>,
    currentUser: KodyarUser?,
    onShowAuth: () -> Unit
) {
    val context = LocalContext.current
    val techsList by liveTechs.collectAsState()
    val isTechsLoading by viewModel.isTechniciansLoading.collectAsState()
    val userCity = currentUser?.city?.takeIf { it.isNotBlank() }
    val targetCity = userCity ?: "اراک"

    var sortBy by remember { mutableStateOf("top_rated") }
    var selectedTechForRepair by remember { mutableStateOf<KodyarTechnician?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshTechnicians()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Top Header: Total Nationwide Counter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "تکنسین‌های کدیار",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CodyarNavy
                )
                Text(
                    text = "(${techsList.size} متخصص فعال در سراسر کشور)",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )
            }
            if (isTechsLoading) {
                Surface(
                    color = Color(0xFFFEF3C7),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFD97706)
                        )
                        Text(
                            text = "در حال استعلام...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            } else if (techsList.isNotEmpty()) {
                Surface(
                    color = Color(0xFFE0F2FE),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "🟢 شبکه فعال سراسری",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Clean user location indicator card (Shows customer's province/city without manual switching)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📍", fontSize = 16.sp)
                Text(
                    text = "نمایش تعمیرکاران و تکنسین‌های مجاز در محدوده شما: $targetCity",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E40AF),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val listAvatars = remember {
            listOf(
                "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?w=150&h=150&fit=crop",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop",
                "https://images.unsplash.com/photo-1566492031773-4f4e44671857?w=150&h=150&fit=crop",
                "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=150&h=150&fit=crop",
                "https://images.unsplash.com/photo-1628157582853-a796fa650a6a?w=150&h=150&fit=crop"
            )
        }

        // Sorting Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "مرتب‌سازی:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CodyarTextSecondary
            )
            
            val sortOptions = listOf(
                "top_rated" to "⭐ برترین‌ها (امتیاز بالا)",
                "most_orders" to "🛠️ پرکارترین‌ها",
                "all" to "👤 همه"
            )
            
            sortOptions.forEach { (optionKey, optionLabel) ->
                val isActive = sortBy == optionKey
                Box(
                    modifier = Modifier
                        .background(
                            if (isActive) CodyarNavy.copy(alpha = 0.12f) else Color(0xFFF3F4F6),
                            RoundedCornerShape(30.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isActive) CodyarNavy else Color.Transparent,
                            shape = RoundedCornerShape(30.dp)
                        )
                        .clickable { sortBy = optionKey }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = optionLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) CodyarNavy else CodyarTextPrimary
                    )
                }
            }
        }

        // List - Strictly filtered to the user's city/province
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            val baseFiltered = techsList.filter { tech ->
                val techCity = tech.resolvedCity
                if (techCity.isBlank()) false
                else viewModel.areCitiesCompatible(techCity, targetCity)
            }

            val filtered = when (sortBy) {
                "top_rated" -> baseFiltered.sortedWith(
                    compareByDescending<KodyarTechnician> { it.rating ?: 5.0 }
                        .thenByDescending { it.satisfactionRate ?: 100 }
                        .thenByDescending { it.completedOrders ?: 0 }
                )
                "most_orders" -> baseFiltered.sortedByDescending { it.completedOrders ?: 0 }
                else -> baseFiltered
            }

            if (filtered.isEmpty()) {
                item {
                    if (isTechsLoading) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 3.dp,
                                    color = CodyarNavy
                                )
                                Text(
                                    "در حال استعلام و دریافت لیست تکنسین‌ها از سرور...",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CodyarNavy,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    "ارتباط زنده با پایگاه داده کدیار۲۴ برقرار است",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("📍", fontSize = 28.sp)
                                Text(
                                    "تکنسینی در شهر $targetCity ثبت نشده است",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    "در حال حاضر تکنسین فعالی برای شهر مورد نظر ثبت نشده است. در صورت نیاز به هماهنگی تکنسین یا ثبت نهایی درخواست تعمیرات، لطفاً با پشتیبانی کدیار۲۴ ارتباط برقرار کنید.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            } else {
                items(filtered) { tech ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CodyarSurface),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color(0xFFEAECEF))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(11.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Avatar on the side
                            val avatarUrl = tech.resolvedAvatarUrl
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, CodyarNavy, CircleShape)
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUrl != null) {
                                    AsyncImage(
                                        model = avatarUrl,
                                        contentDescription = tech.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = tech.name,
                                        tint = Color(0xFF94A3B8),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        tech.name ?: "",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CodyarTextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFEAFAF1), RoundedCornerShape(5.dp))
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text("✓ تایید شده", color = Color(0xFF1E8449), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Text(
                                        "📍 ${tech.city ?: "نامشخص"}",
                                        fontSize = 11.sp,
                                        color = CodyarTextSecondary
                                    )
                                    Text("•", fontSize = 11.sp, color = Color.LightGray)
                                    Text(
                                        "🛠️ ${tech.completedOrders ?: 0} سرویس",
                                        fontSize = 11.sp,
                                        color = CodyarTextSecondary
                                    )
                                    Text("•", fontSize = 11.sp, color = Color.LightGray)
                                    
                                    // Star Rating
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB000),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            String.format(java.util.Locale.US, "%.1f", tech.rating ?: 5.0),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = CodyarTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            "(${tech.satisfactionRate ?: 100}% رضایت)",
                                            fontSize = 10.sp,
                                            color = Color(0xFF1E8449),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                if (!tech.bio.isNullOrBlank()) {
                                    Text(
                                        tech.bio,
                                        fontSize = 11.sp,
                                        color = CodyarTextPrimary,
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(bottom = 7.dp)
                                    )
                                }

                                // Categories
                                if (tech.resolvedCategories.isNotEmpty()) {
                                    Row(
                                        modifier = Modifier
                                            .horizontalScroll(rememberScrollState())
                                            .padding(bottom = 9.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        tech.resolvedCategories.forEach { cat ->
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFE8EAF0), RoundedCornerShape(5.dp))
                                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                                            ) {
                                                Text(cat, color = CodyarTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (currentUser == null) {
                                            onShowAuth()
                                            return@Button
                                        }
                                        selectedTechForRepair = tech
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CodyarNavy),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp)
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("اعزام تکنسین", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("تکنسینی ثبت نشده است", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("به زودی تکنسین‌های تایید شده اضافه می‌شوند", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // Bottom CTA for techs
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = CodyarNavy),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("تکنسین هستید؟", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(
                            "با همکاران ما در وب‌سایت کدیار۲۴ تماس بگیرید و پس از تایید مدارک سفارش کار دریافت کنید.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://kodyar24.ir"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "خطا در باز کردن وب‌سایت", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CodyarRed),
                            shape = RoundedCornerShape(9.dp)
                        ) {
                            Text("ثبت‌نام تکنسین در سایت", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG FOR REQUESTING DISPATCH ---
    selectedTechForRepair?.let { tech ->
        val next7Days = remember { calculateNext7JalaliDays() }
        var selectedDayIndex by remember { mutableStateOf(if (next7Days.isNotEmpty()) 0 else 0) }
        val timeSlots = remember {
            listOf(
                "صبح (۹ الی ۱۳)",
                "عصر (۱۴ الی ۱۸)",
                "غروب (۱۸ الی ۲۱)"
            )
        }
        var selectedTimeSlot by remember { mutableStateOf(timeSlots[1]) } // Default to afternoon

        var deviceBrand by remember { mutableStateOf("") }
        var problemDesc by remember { mutableStateOf("") }
        var contactPhone by remember(currentUser) { mutableStateOf(currentUser?.phone ?: "") }
        var isSubmitting by remember { mutableStateOf(false) }

        val chosenDay = next7Days.getOrNull(selectedDayIndex)
        val preferredDate = chosenDay?.let { "${it.formattedLabel} - $selectedTimeSlot" } ?: ""

        AlertDialog(
            onDismissRequest = { if (!isSubmitting) selectedTechForRepair = null },
            title = {
                Text(
                    text = "درخواست اعزام تکنسین (${tech.name ?: "کارشناس"})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = CodyarNavy,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "جهت هماهنگی دقیق مراجعه تکنسین، مشخصات و تاریخ مورد نظر را انتخاب فرمایید:",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 1. Device and Brand input
                    OutlinedTextField(
                        value = deviceBrand,
                        onValueChange = { deviceBrand = it },
                        label = { Text("نوع دستگاه و برند", fontSize = 11.sp) },
                        placeholder = { Text("مثلاً: پکیج بوتان یا لباسشویی ال‌جی", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )

                    // 2. Problem description input
                    OutlinedTextField(
                        value = problemDesc,
                        onValueChange = { problemDesc = it },
                        label = { Text("شرح مشکل دستگاه", fontSize = 11.sp) },
                        placeholder = { Text("مثلاً: خطای E01 می‌دهد یا آب گرم نمی‌شود", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2,
                        maxLines = 3,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )

                    // 3. 7 Days Jalali Date Picker (No typing needed)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "📅 انتخاب روز مراجعه (۷ روز آینده):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CodyarNavy,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            next7Days.forEachIndexed { index, dayItem ->
                                val isSelected = (index == selectedDayIndex)
                                Surface(
                                    onClick = { selectedDayIndex = index },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CodyarNavy else Color(0xFFF1F5F9),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) CodyarNavy else Color(0xFFCBD5E1)
                                    ),
                                    modifier = Modifier.width(92.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = when {
                                                dayItem.isToday -> "امروز"
                                                dayItem.isTomorrow -> "فردا"
                                                else -> dayItem.dayOfWeekName
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFF1E293B)
                                        )
                                        Text(
                                            text = "${dayItem.dayOfMonth} ${dayItem.monthName}",
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color(0xFF93C5FD) else Color(0xFF64748B),
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Time Slot Chips
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⏰ بازه زمانی پیشنهادی:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CodyarNavy,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            timeSlots.forEach { slot ->
                                val isSelected = (selectedTimeSlot == slot)
                                Surface(
                                    onClick = { selectedTimeSlot = slot },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF1E8449) else Color(0xFFF8FAFC),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFF1E8449) else Color(0xFFE2E8F0)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = slot,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Selected Date Preview Card
                    Surface(
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🗓️", fontSize = 14.sp)
                            Text(
                                text = "زمان ثبت‌شده: $preferredDate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D4ED8)
                            )
                        }
                    }

                    // 5. Contact phone input
                    OutlinedTextField(
                        value = contactPhone,
                        onValueChange = { contactPhone = it },
                        label = { Text("شماره همراه جهت هماهنگی", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deviceBrand.isBlank() || problemDesc.isBlank() || contactPhone.isBlank()) {
                            Toast.makeText(context, "لطفاً نوع دستگاه، شرح مشکل و شماره تماس را تکمیل فرمایید", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        val repairCity = currentUser?.resolvedCity?.takeIf { it.isNotBlank() }
                            ?: currentUser?.city?.takeIf { it.isNotBlank() }
                            ?: tech.resolvedCity.takeIf { it.isNotBlank() }
                            ?: tech.city?.takeIf { it.isNotBlank() }
                            ?: "اراک"

                        val formattedDesc = "دستگاه و برند: $deviceBrand\n" +
                                "شرح خرابی: $problemDesc\n" +
                                "زمان پیشنهادی مراجعه کارشناس: $preferredDate\n" +
                                "تلفن تماس هماهنگی: $contactPhone"

                        viewModel.submitRepairRequest(
                            techId = tech.id ?: "",
                            description = formattedDesc,
                            city = repairCity
                        ) { success, err ->
                            isSubmitting = false
                            if (success) {
                                selectedTechForRepair = null
                                Toast.makeText(
                                    context,
                                    "✅ درخواست اعزام با موفقیت ثبت شد. تکنسین در تاریخ انتخابی با شما تماس می‌گیرد.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                Toast.makeText(context, err ?: "خطا در ثبت درخواست", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CodyarNavy),
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("ثبت نهایی درخواست", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { selectedTechForRepair = null },
                    enabled = !isSubmitting
                ) {
                    Text("انصراف", color = Color.Gray, fontSize = 12.sp)
                }
            }
        )
    }
}

data class JalaliScheduleDay(
    val dayOfWeekName: String,
    val dayOfMonth: Int,
    val monthName: String,
    val formattedLabel: String,
    val isToday: Boolean,
    val isTomorrow: Boolean
)

fun calculateNext7JalaliDays(): List<JalaliScheduleDay> {
    val persianMonths = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    val weekDays = mapOf(
        java.util.Calendar.SATURDAY to "شنبه",
        java.util.Calendar.SUNDAY to "یکشنبه",
        java.util.Calendar.MONDAY to "دوشنبه",
        java.util.Calendar.TUESDAY to "سه‌شنبه",
        java.util.Calendar.WEDNESDAY to "چهارشنبه",
        java.util.Calendar.THURSDAY to "پنجشنبه",
        java.util.Calendar.FRIDAY to "جمعه"
    )

    val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 335)
    val list = mutableListOf<JalaliScheduleDay>()
    val cal = java.util.Calendar.getInstance()

    for (step in 0 until 7) {
        val year = cal.get(java.util.Calendar.YEAR)
        val month = cal.get(java.util.Calendar.MONTH) + 1
        val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)

        val gy = year - 1600
        val gm = month - 1
        val gd = day - 1

        var gDayNo = 365 * gy + (gy + 4) / 4 - (gy + 100) / 100 + (gy + 400) / 400
        gDayNo += gDaysInMonth[gm]
        if (gm > 1 && ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0))) {
            gDayNo++
        }
        gDayNo += gd

        var jDayNo = gDayNo - 79
        val jNp = jDayNo / 12053
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * (jDayNo / 1461)
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += (jDayNo - 1) / 365
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        var jd = 0
        for (i in 0..11) {
            val monthLength = if (i < 6) 31 else if (i < 11) 30 else 29
            if (jDayNo < monthLength) {
                jm = i + 1
                jd = jDayNo + 1
                break
            }
            jDayNo -= monthLength
        }

        val monthName = persianMonths.getOrElse(jm - 1) { "" }
        val weekDay = weekDays[dayOfWeek] ?: "شنبه"

        val label = when (step) {
            0 -> "امروز ($weekDay $jd $monthName)"
            1 -> "فردا ($weekDay $jd $monthName)"
            else -> "$weekDay $jd $monthName"
        }

        list.add(
            JalaliScheduleDay(
                dayOfWeekName = weekDay,
                dayOfMonth = jd,
                monthName = monthName,
                formattedLabel = label,
                isToday = (step == 0),
                isTomorrow = (step == 1)
            )
        )

        cal.add(java.util.Calendar.DAY_OF_YEAR, 1)
    }
    return list
}
