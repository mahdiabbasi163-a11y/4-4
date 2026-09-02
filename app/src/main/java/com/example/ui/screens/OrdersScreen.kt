package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.KodyarRepairOrder
import com.example.data.model.PartPurchaseOrder
import com.example.ui.AssistantViewModel

@Composable
fun OrdersScreen(
    viewModel: AssistantViewModel,
    repairOrders: List<KodyarRepairOrder>,
    isRepairsLoading: Boolean,
    onBack: () -> Unit,
    onNavigateToTechs: () -> Unit
) {
    val partPurchases by viewModel.partPurchases.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isTechnicianOnline by viewModel.isTechnicianOnline.collectAsState()
    val isTechStatusUpdating by viewModel.isTechStatusUpdating.collectAsState()
    val isTech = currentUser?.isTechnicianUser == true || (currentUser?.role == "technician" || currentUser?.role == "tech" || currentUser?.role == "repairman")

    var selectedSubTab by remember { mutableStateOf(0) } // 0: Repair Orders, 1: Part Purchases
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadRepairs()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CodyarSurface,
                    contentColor = CodyarTextPrimary
                ),
                border = BorderStroke(1.dp, Color(0xFFEAECEF)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("بازگشت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = {
                    viewModel.loadRepairs()
                    Toast.makeText(context, "در حال همگام‌سازی اطلاعات...", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "بروزرسانی وضعیت", tint = CodyarNavy)
            }
        }

        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = CodyarSurface,
            contentColor = CodyarNavy,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Text(
                        text = if (isTech) "سفارشات تعمیر (${repairOrders.size})" else "درخواست‌های تعمیر (${repairOrders.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Text(
                        text = if (isTech) "خریدهای قطعه یدکی (${partPurchases.size})" else "سفارش‌های قطعات (${partPurchases.size})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }

        if (selectedSubTab == 0) {
            // Repair Orders Tab

            // Technician Online / Vacation Status Card
            if (isTech) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isTechnicianOnline) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, if (isTechnicianOnline) Color(0xFFBBF7D0) else Color(0xFFFECACA)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(if (isTechnicianOnline) Color(0xFF16A34A) else Color(0xFFDC2626), CircleShape)
                            )
                            Column {
                                Text(
                                    text = if (isTechnicianOnline) "وضعیت تکنسین: آماده به کار 🟢" else "وضعیت تکنسین: در حال مرخصی 🏖️",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTechnicianOnline) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = if (isTechnicianOnline) "سفارش‌های جدید شهر ${currentUser?.city ?: ""} بلافاصله به شما اعلام می‌شود" else "سفارش جدیدی از منطقه برای شما ارسال نمی‌شود",
                                    fontSize = 10.sp,
                                    color = if (isTechnicianOnline) Color(0xFF15803D) else Color(0xFFB91C1C)
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (!isTechStatusUpdating) {
                                    val willBeOnline = !isTechnicianOnline
                                    viewModel.toggleTechnicianStatus { success, err ->
                                        if (success) {
                                            val msg = if (willBeOnline) "وضعیت: آماده به کار و دریافت سفارش ✅" else "وضعیت: مرخصی (عدم دریافت سفارش) 🏖️"
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, err ?: "خطا در تغییر وضعیت", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isTechnicianOnline) Color(0xFFDC2626) else Color(0xFF16A34A)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            if (isTechStatusUpdating) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                            } else {
                                Text(
                                    text = if (isTechnicianOnline) "رفتن به مرخصی" else "خروج از مرخصی",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Commission Debt Alert Banner for Technician
            if (isTech && currentUser?.hasCommissionDebt == true) {
                val debtAmount = currentUser?.resolvedDebt ?: 0.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔒", fontSize = 24.sp)
                            Column {
                                Text(
                                    "هشدار تسویه کمیسیون و بدهی پلتفرم",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    if (debtAmount > 0) "مبلغ بدهی کمیسیون: ${formatToman(debtAmount)} تومان" else "حساب شما دارای بدهی کمیسیون است",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                        Text(
                            "تکنسین گرامی! بر اساس قوانین کدیار۲۴، جهت فعال‌سازی مجدد و مشاهده اطلاعات تماس سفارش‌های جدید و قبول مسئولیت کار، لطفاً نسبت به تسویه آنلاین کمیسیون اقدام فرمایید. به محض تسویه، قفل سفارش‌ها به‌صورت آنی باز خواهد شد.",
                            fontSize = 12.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 18.sp
                        )
                        Button(
                            onClick = {
                                viewModel.openCommissionSettlement(context)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💳 تسویه آنلاین کمیسیون (درگاه شتاب)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            if (isTech && currentUser?.isApprovedUser != true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFCE8)),
                    border = BorderStroke(1.dp, Color(0xFFFEF08A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📋", fontSize = 24.sp)
                            Column {
                                Text(
                                    "وضعیت حساب تکنسین: در انتظار بررسی و تایید مدیریت ⏳",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF854D0E)
                                )
                                Text(
                                    "شهر فعالیت: ${currentUser?.city ?: "ثبت نشده"}",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA16207)
                                )
                            }
                        }
                        HorizontalDivider(color = Color(0xFFFEF08A))
                        Text(
                            "تکنسین گرامی (${currentUser?.full_name ?: ""})! مدارک سه‌گانه شما ثبت شده و برای مدیریت ارسال گردیده است. پس از تایید مدیریت، سفارش‌های درخواست تعمیر مشتریان در شهر ${currentUser?.city ?: ""} برای شما فعال خواهد شد.",
                            fontSize = 12.sp,
                            color = Color(0xFF713F12),
                            lineHeight = 18.sp
                        )
                        Text(
                            "💡 توجه: کلیه امکانات عمومی برنامه شامل کدهای خطا، مشکلات متداول، خرید قطعات یدکی و تهیه اشتراک برای شما مانند سایر کاربران فعال و قابل استفاده است.",
                            fontSize = 11.sp,
                            color = Color(0xFF854D0E),
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.checkTechnicianApprovalStatus { _, message ->
                                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFCA8A04))
                            ) {
                                Text("🔄 استعلام وضعیت تایید از سرور مدیریت", fontSize = 11.sp, color = Color(0xFF854D0E), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            if (isRepairsLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CodyarNavy)
                }
            } else if (repairOrders.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(if (isTech) (if (!isTechnicianOnline) "🏖️" else "📋") else "🔧", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isTech) (if (!isTechnicianOnline) "شما در حالت مرخصی هستید" else "سفارش تعمیری برای انجام وجود ندارد") else "سفارشی ثبت نشده است",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = CodyarTextPrimary
                    )
                    Text(
                        text = if (isTech) (if (!isTechnicianOnline) "در زمان مرخصی، سفارش‌های جدید شهر دریافت نمی‌شوند. جهت دریافت سفارش، وضعیت خود را به آماده‌به‌کار تغییر دهید." else "سفارش‌های جدید ارجاع شده از طرف مشتریان در این بخش قرار می‌گیرند") else "جهت ثبت درخواست با تکنسین تماس حاصل فرمایید",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 20.dp),
                        textAlign = TextAlign.Center
                    )
                    if (isTech) {
                        Button(
                            onClick = { viewModel.loadRepairs() },
                            colors = ButtonDefaults.buttonColors(containerColor = CodyarNavy)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بروزرسانی سفارش‌ها")
                        }
                    } else {
                        Button(
                            onClick = onNavigateToTechs,
                            colors = ButtonDefaults.buttonColors(containerColor = CodyarNavy)
                        ) {
                            Text("لیست تکنسین‌ها")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(repairOrders.size) { i ->
                        val o = repairOrders[i]
                        val rawStatus = (o.status ?: "").lowercase().trim()
                        val customLabel = o.status_label_fa ?: o.statusLabelFa

                        val (statusText, textCol, bgCol) = when {
                            !customLabel.isNullOrBlank() ->
                                Triple(customLabel, Color(0xFF1E8449), Color(0xFFEAFAF1))
                            rawStatus in listOf("assigned", "accepted", "تایید شده", "ارجاع به تکنسین", "اختصاص داده شد") ->
                                Triple(if (isTech) "ارجاع شده به شما 👨‍🔧" else "تکنسین اختصاص داده شد 👨‍🔧", Color(0xFF1E8449), Color(0xFFEAFAF1))
                            rawStatus in listOf("in_progress", "ongoing", "processing", "در حال انجام", "در مسیر") ->
                                Triple(if (isTech) "در حال انجام کار 🛠️" else "تکنسین در مسیر / در حال انجام 🛠️", Color(0xFF2563EB), Color(0xFFEFF6FF))
                            rawStatus in listOf("completed", "done", "تکمیل شده", "انجام شد") ->
                                Triple("تعمیر پایان یافت ✅", Color(0xFF0F766E), Color(0xFFF0FDFA))
                            rawStatus in listOf("cancelled", "rejected", "لغو شده", "رد شد") ->
                                Triple("لغو شده ❌", Color(0xFFC0392B), Color(0xFFFDF0EE))
                            else ->
                                Triple(if (isTech) "سفارش جدید ⏳" else "در صف بررسی مدیریت ⏳", Color(0xFFD68910), Color(0xFFFEF9E7))
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CodyarSurface),
                            border = BorderStroke(1.dp, Color(0xFFEAECEF)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isTech) "سفارش تعمیر #${o.order_id ?: o.id ?: (i + 1)}" else "درخواست تعمیر #${o.order_id ?: o.id ?: (i + 1)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CodyarTextPrimary
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(bgCol, RoundedCornerShape(7.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = statusText,
                                            color = textCol,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (o.resolvedDescription.isNotBlank()) {
                                    Text(
                                        text = o.resolvedDescription,
                                        fontSize = 13.sp,
                                        color = CodyarTextPrimary,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .background(Color(0xFFF7F8FA), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    )
                                }

                                if (!o.city.isNullOrBlank()) {
                                    Text(
                                        text = "📍 شهر / آدرس: ${o.city}${if (!o.address.isNullOrBlank()) " - ${o.address}" else ""}",
                                        fontSize = 12.sp,
                                        color = CodyarTextSecondary,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                if (o.resolvedCategory.isNotBlank() || o.resolvedBrand.isNotBlank()) {
                                    val catBrand = listOf(o.resolvedCategory, o.resolvedBrand, o.model).filter { !it.isNullOrBlank() }.joinToString(" - ")
                                    Text(
                                        text = "🔧 دستگاه: $catBrand",
                                        fontSize = 12.sp,
                                        color = CodyarTextSecondary,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                // If Technician: show Customer Contact Info (Protected by Commission Debt Check)
                                if (isTech) {
                                    val isLockedByDebt = currentUser?.hasCommissionDebt == true && rawStatus !in listOf("in_progress", "completed", "done")
                                    val hasCustomerInfo = o.resolvedCustomerName.isNotBlank() || o.resolvedCustomerPhone.isNotBlank()

                                    if (isLockedByDebt) {
                                        // 🔒 Locked Info due to platform commission debt
                                        Surface(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            color = Color(0xFFFEF2F2),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, Color(0xFFFECACA))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "🔒 اطلاعات تماس و آدرس قفل است",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF991B1B),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "جهت مشاهده شماره تماس مشتری و پذیرش سفارش، بدهی کمیسیون قبلی را تسویه کنید.",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFFB91C1C),
                                                        lineHeight = 16.sp
                                                    )
                                                }
                                                Button(
                                                    onClick = {
                                                        viewModel.openCommissionSettlement(context)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("تسویه کمیسیون", fontSize = 11.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    } else if (hasCustomerInfo) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = "👤 مشتری: ${o.resolvedCustomerName.ifBlank { "مشتری کدیار" }}",
                                                    fontSize = 12.sp,
                                                    color = Color(0xFF1E40AF),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (o.resolvedCustomerPhone.isNotBlank()) {
                                                    Text(
                                                        text = "📞 تلفن: ${o.resolvedCustomerPhone}",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFF3B82F6),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    )
                                                }
                                            }

                                            if (o.resolvedCustomerPhone.isNotBlank()) {
                                                Button(
                                                    onClick = {
                                                        try {
                                                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${o.resolvedCustomerPhone}"))
                                                            dialIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                            context.startActivity(dialIntent)
                                                        } catch (_: Exception) {
                                                            Toast.makeText(context, "شماره مشتری: ${o.resolvedCustomerPhone}", Toast.LENGTH_LONG).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D4ED8)),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                        Text("تماس با مشتری", fontSize = 11.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Action buttons for Technician (Accept, Start, Complete)
                                    val orderIdToAct = o.order_id ?: o.id ?: ""
                                    val isAssignedToCurrentTech = !o.technician_id.isNullOrBlank() && (o.technician_id == currentUser?.id || o.technician_name == currentUser?.full_name)
                                    val isAcceptedOrAssigned = rawStatus in listOf("assigned", "accepted", "تایید شده", "ارجاع به تکنسین", "اختصاص داده شد") || isAssignedToCurrentTech
                                    val isOngoing = rawStatus in listOf("in_progress", "ongoing", "processing", "در حال انجام", "در مسیر")
                                    val isDone = rawStatus in listOf("completed", "done", "تکمیل شده", "انجام شد", "cancelled", "rejected", "لغو شده")

                                    if (orderIdToAct.isNotBlank() && !isDone) {
                                        if (isOngoing) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    viewModel.updateOrderStatus(orderIdToAct, "completed") { success, err ->
                                                        if (success) {
                                                            Toast.makeText(context, "سفارش پایان یافت و کمیسیون ۱۵٪ ثبت گردید ✅", Toast.LENGTH_LONG).show()
                                                        } else {
                                                            Toast.makeText(context, err ?: "خطا", Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text("اتمام تعمیر و دریافت دستمزد از مشتری ✅", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (isAcceptedOrAssigned) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = {
                                                        viewModel.updateOrderStatus(orderIdToAct, "in_progress") { success, err ->
                                                            if (success) {
                                                                Toast.makeText(context, "وضعیت به در حال انجام تغییر یافت", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, err ?: "خطا", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("شروع کار / اعزام 🛠️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Button(
                                                    onClick = {
                                                        viewModel.updateOrderStatus(orderIdToAct, "completed") { success, err ->
                                                            if (success) {
                                                                Toast.makeText(context, "سفارش با موفقیت پایان یافت و کمیسیون ۱۵٪ کسر گردید ✅", Toast.LENGTH_LONG).show()
                                                            } else {
                                                                Toast.makeText(context, err ?: "خطا", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("اتمام و تسویه با مشتری ✅", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        } else {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            if (currentUser?.hasCommissionDebt == true) {
                                                Button(
                                                    onClick = {
                                                        viewModel.openCommissionSettlement(context)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text("🔒 تسویه کمیسیون جهت قبول سفارش", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                }
                                            } else {
                                                Button(
                                                    onClick = {
                                                        viewModel.acceptRepairOrder(orderIdToAct) { success, err ->
                                                            if (success) {
                                                                Toast.makeText(context, "سفارش با موفقیت به شما اختصاص یافت ✅", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, err ?: "خطا در قبول سفارش", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = if (currentUser?.isFirstOrderFree == true) "قبول و ثبت به نام من (سفارش اول رایگان 🎁)" else "قبول و ثبت به نام من 🤝",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // If Customer: show Assigned Technician Contact Info
                                if (!isTech && o.resolvedTechnicianName.isNotBlank()) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                            .border(1.dp, Color(0xFFDCFCE7), RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "👨‍🔧 تکنسین اعزامی: ${o.resolvedTechnicianName}",
                                                fontSize = 12.sp,
                                                color = Color(0xFF166534),
                                                fontWeight = FontWeight.Bold
                                            )
                                            if (o.resolvedScheduledInfo.isNotBlank()) {
                                                Text(
                                                    text = "📅 زمان مراجعه: ${o.resolvedScheduledInfo}",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF15803D),
                                                    modifier = Modifier.padding(top = 3.dp)
                                                )
                                            }
                                        }

                                        if (o.resolvedTechnicianPhone.isNotBlank()) {
                                            Button(
                                                onClick = {
                                                    try {
                                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${o.resolvedTechnicianPhone}"))
                                                        dialIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                        context.startActivity(dialIntent)
                                                    } catch (_: Exception) {
                                                        Toast.makeText(context, "شماره تکنسین: ${o.resolvedTechnicianPhone}", Toast.LENGTH_LONG).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                                    Text("تماس با تکنسین", fontSize = 11.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (o.resolvedDate.isNotBlank()) {
                                    Text(
                                        text = "📅 تاریخ ثبت: ${o.resolvedDate}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF9AA3AF),
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Part Purchases Tab
            if (partPurchases.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 50.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("📦", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("هیچ سفارش قطعه‌ای ثبت نشده است", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CodyarTextPrimary)
                    Text("با مراجعه به فروشگاه می‌توانید قطعه مورد نظر خود را سفارش دهید", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 20.dp), textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(partPurchases.size) { i ->
                        val purchase = partPurchases[i]

                        // Status styling: pending -> در انتظار بررسی/پرداخت, approved/processing -> تایید شده (آماده‌سازی), sent/shipped -> ارسال شده به پست, completed/delivered -> تحویل مشتری
                        val rawStatus = (purchase.status).lowercase().trim()
                        val customLabel = purchase.resolvedStatusLabelFa

                        val (statusText, textCol, bgCol) = when {
                            !customLabel.isNullOrBlank() ->
                                Triple(customLabel, Color(0xFF1E8449), Color(0xFFEAFAF1))
                            rawStatus in listOf("approved", "accepted", "processing", "تایید شد", "تایید شده", "در حال آماده سازی", "در حال آماده‌سازی") ->
                                Triple("تایید شده (در حال آماده‌سازی) ⚙️", Color(0xFF1E8449), Color(0xFFEAFAF1))
                            rawStatus in listOf("sent", "shipped", "posted", "ارسال شد", "ارسال شده", "ارسال به پست", "تحویل پست", "تیپاکس") ->
                                Triple("ارسال شده به پست/تیپاکس 📦", Color(0xFF1D4ED8), Color(0xFFEFF6FF))
                            rawStatus in listOf("delivered", "completed", "تحویل شد", "تحویل داده شده") ->
                                Triple("تحویل داده شده ✅", Color(0xFF0F766E), Color(0xFFF0FDFA))
                            rawStatus in listOf("rejected", "cancelled", "رد شد", "لغو شده") ->
                                Triple("لغو شده ❌", Color(0xFFC0392B), Color(0xFFFDEDEC))
                            else ->
                                Triple("در انتظار تایید پرداخت ⏳", Color(0xFFD68910), Color(0xFFFEF9E7))
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = CodyarSurface),
                            border = BorderStroke(1.dp, Color(0xFFEAECEF)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = purchase.resolvedPartName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = CodyarTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(bgCol, RoundedCornerShape(7.dp))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = statusText,
                                            color = textCol,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "تعداد: ${purchase.quantity} عدد",
                                        fontSize = 13.sp,
                                        color = CodyarTextSecondary
                                    )
                                    Text(
                                        text = "مبلغ کل: ${formatToman(purchase.resolvedTotalPrice)} تومان",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = CodyarRed
                                    )
                                }

                                // Tracking code box if present from website / postal dispatch
                                if (purchase.resolvedTrackingCode.isNotBlank()) {
                                    Surface(
                                        color = Color(0xFFEFF6FF),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "کد رهگیری مرسوله (پست/تیپاکس):",
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF1E40AF),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = purchase.resolvedTrackingCode,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1D4ED8),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                    val clip = ClipData.newPlainText("tracking_code", purchase.resolvedTrackingCode)
                                                    clipboard.setPrimaryClip(clip)
                                                    Toast.makeText(context, "کد رهگیری کپی شد: ${purchase.resolvedTrackingCode}", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "کپی کد رهگیری", tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = Color(0xFFEAECEF)
                                )

                                if (purchase.resolvedDate.isNotBlank()) {
                                    Text(
                                        text = "📅 تاریخ سفارش: ${purchase.resolvedDate}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF718096),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                if (!purchase.address.isNullOrBlank()) {
                                    Text(
                                        text = "📍 آدرس ارسال: ${purchase.address}",
                                        fontSize = 12.sp,
                                        color = CodyarTextSecondary,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }

                                if (!purchase.notes.isNullOrBlank()) {
                                    Text(
                                        text = "📝 توضیحات: ${purchase.notes}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF718096)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
