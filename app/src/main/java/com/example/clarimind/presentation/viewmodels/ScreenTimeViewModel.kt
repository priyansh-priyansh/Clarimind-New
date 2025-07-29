package com.example.clarimind.presentation.viewmodels

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clarimind.presentation.screens.AppUsage
import com.example.clarimind.presentation.screens.ScreenTimeData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import com.example.clarimind.data.AppUsageEntity
import com.example.clarimind.data.UsageDatabase
import com.example.clarimind.data.FirebaseSyncHelper

data class ScreenTimeUiState(
    val screenTimeData: ScreenTimeData? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val hasPermission: Boolean = false
)

data class UsageStatsData(
    val packageName: String,
    val totalTimeInForeground: Long,
    val firstTimeStamp: Long,
    val lastTimeStamp: Long
)

class ScreenTimeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ScreenTimeUiState())
    val uiState: StateFlow<ScreenTimeUiState> = _uiState.asStateFlow()

    fun checkPermission(context: Context) {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )

        val hasPermission = mode == AppOpsManager.MODE_ALLOWED
        android.util.Log.d("ScreenTime", "Permission check - Mode: $mode, HasPermission: $hasPermission")

        _uiState.value = _uiState.value.copy(hasPermission = hasPermission)

        if (hasPermission) {
            android.util.Log.d("ScreenTime", "Permission granted, loading data...")
            loadScreenTimeData(context)
        } else {
            android.util.Log.d("ScreenTime", "Permission denied, cannot load data")
        }
    }

    fun refreshPermission(context: Context) {
        checkPermission(context)
    }

    fun requestPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    fun loadScreenTimeData(context: Context) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

                // Get current time boundaries - FIXED: Use proper time boundaries
                val endTime = System.currentTimeMillis()
                val startTime = getStartOfDay(endTime)

                // For weekly data, get exactly 7 days of data
                val weekStartTime = getStartOfDay(endTime - (6 * 24 * 60 * 60 * 1000L))

                android.util.Log.d("ScreenTime", "Time boundaries - Start: ${Date(startTime)}, End: ${Date(endTime)}")
                android.util.Log.d("ScreenTime", "Week boundaries - Start: ${Date(weekStartTime)}, End: ${Date(endTime)}")

                // FIXED: Use INTERVAL_BEST for more accurate data
                val todayStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    startTime,
                    endTime
                ).filter { it.totalTimeInForeground > 0 }

                val weeklyStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    weekStartTime,
                    endTime
                ).filter { it.totalTimeInForeground > 0 }

                android.util.Log.d("ScreenTime", "Today stats: ${todayStats.size}, Weekly: ${weeklyStats.size}")

                val screenTimeData = processUsageStatsFixed(todayStats, weeklyStats, context)
                // Save today's per-app usage to Room
                saveTodayAppUsageToDb(context, screenTimeData.mostUsedApps)
                _uiState.value = _uiState.value.copy(
                    screenTimeData = screenTimeData,
                    isLoading = false
                )

            } catch (e: Exception) {
                android.util.Log.e("ScreenTime", "Error loading screen time data", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load screen time data: ${e.message}"
                )
            }
        }
    }

    private suspend fun saveTodayAppUsageToDb(context: Context, appUsages: List<AppUsage>) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = dateFormat.format(Date())
        val entities = appUsages.map {
            AppUsageEntity(
                appName = it.appName,
                packageName = it.packageName,
                usageTime = it.usageTime,
                date = today
            )
        }
        val db = UsageDatabase.getInstance(context)
        db.usageDao().insertAll(entities)
        // Upload to Firestore
        entities.forEach { FirebaseSyncHelper.uploadAppUsage(it) }
    }

    private fun processUsageStatsFixed(
        todayStats: List<UsageStats>,
        weeklyStats: List<UsageStats>,
        context: Context
    ): ScreenTimeData {

        android.util.Log.d("ScreenTime", "Processing ${todayStats.size} today stats, ${weeklyStats.size} weekly stats")

        // FIXED: Properly aggregate app usage for today
        val todayAppUsage = mutableMapOf<String, Long>()
        val currentTime = System.currentTimeMillis()
        val startOfToday = getStartOfDay(currentTime)

        todayStats.forEach { stat ->
            // FIXED: Only count usage that actually happened today
            if (stat.lastTimeStamp >= startOfToday && stat.totalTimeInForeground > 0) {
                val packageName = stat.packageName
                val currentUsage = todayAppUsage[packageName] ?: 0L
                todayAppUsage[packageName] = currentUsage + stat.totalTimeInForeground

                android.util.Log.d("ScreenTime", "Today - ${packageName}: ${stat.totalTimeInForeground}ms")
            }
        }

        // Calculate total screen time for today
        val totalScreenTime = todayAppUsage.values.sum() / (1000 * 60) // Convert to minutes

        android.util.Log.d("ScreenTime", "Total screen time today: ${totalScreenTime} minutes")

        // FIXED: Calculate weekly data properly
        val weeklyAppUsage = mutableMapOf<String, Long>()
        val weekStart = getStartOfDay(currentTime - (6 * 24 * 60 * 60 * 1000L))

        weeklyStats.forEach { stat ->
            if (stat.lastTimeStamp >= weekStart && stat.totalTimeInForeground > 0) {
                val packageName = stat.packageName
                val currentUsage = weeklyAppUsage[packageName] ?: 0L
                weeklyAppUsage[packageName] = currentUsage + stat.totalTimeInForeground
            }
        }

        val weeklyTotal = weeklyAppUsage.values.sum() / (1000 * 60)
        val dailyAverage = weeklyTotal / 7

        android.util.Log.d("ScreenTime", "Weekly total: ${weeklyTotal} minutes, Daily average: ${dailyAverage} minutes")

        // Get most used apps from today's data
        val mostUsedApps = todayAppUsage.entries
            .filter { it.value > 60000 } // Filter apps used more than 1 minute
            .sortedByDescending { it.value }
            .take(5)
            .map { (packageName, time) ->
                AppUsage(
                    appName = getAppDisplayName(packageName),
                    packageName = packageName,
                    usageTime = time / (1000 * 60) // Convert to minutes
                )
            }

        android.util.Log.d("ScreenTime", "Most used apps: ${mostUsedApps.size}")
        mostUsedApps.forEach { app ->
            android.util.Log.d("ScreenTime", "App: ${app.appName} = ${app.usageTime}min")
        }

        // FIXED: Create proper daily breakdown
        val usageByDay = createFixedDailyBreakdown(weeklyStats)

        return ScreenTimeData(
            totalScreenTime = totalScreenTime,
            dailyAverage = dailyAverage,
            weeklyTotal = weeklyTotal,
            mostUsedApps = mostUsedApps,
            usageByDay = usageByDay
        )
    }

    // FIXED: Proper daily breakdown calculation
    private fun createFixedDailyBreakdown(weeklyStats: List<UsageStats>): Map<String, Long> {
        val usageByDay = mutableMapOf<String, Long>()
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Create day-wise breakdown for the past 7 days
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)

            val dayStart = getStartOfDay(calendar.timeInMillis)
            val dayEnd = dayStart + (24 * 60 * 60 * 1000L) - 1
            val dayName = dateFormat.format(calendar.time)

            // FIXED: Group apps by day and sum their usage
            val dayAppUsage = mutableMapOf<String, Long>()

            weeklyStats.forEach { stat ->
                // Check if this usage stat falls within the current day
                if (stat.lastTimeStamp in dayStart..dayEnd) {
                    val packageName = stat.packageName
                    val currentUsage = dayAppUsage[packageName] ?: 0L
                    dayAppUsage[packageName] = currentUsage + stat.totalTimeInForeground
                }
            }

            val dayTotal = dayAppUsage.values.sum() / (1000 * 60) // Convert to minutes
            usageByDay[dayName] = dayTotal

            android.util.Log.d("ScreenTime", "$dayName: ${dayTotal} minutes")
        }

        return usageByDay
    }

    private fun getStartOfDay(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun createFallbackData(): ScreenTimeData {
        android.util.Log.d("ScreenTime", "Creating fallback data")
        return ScreenTimeData(
            totalScreenTime = 420, // 7 hours
            dailyAverage = 480, // 8 hours
            weeklyTotal = 3360, // 56 hours
            mostUsedApps = listOf(
                AppUsage("ClariMind", "com.example.clarimind", 120),
                AppUsage("Chrome", "com.android.chrome", 180),
                AppUsage("Settings", "com.android.settings", 90),
                AppUsage("System UI", "com.android.systemui", 60),
                AppUsage("Messages", "com.google.android.apps.messaging", 30)
            ),
            usageByDay = mapOf(
                "Monday" to 420L,
                "Tuesday" to 380L,
                "Wednesday" to 450L,
                "Thursday" to 400L,
                "Friday" to 480L,
                "Saturday" to 520L,
                "Sunday" to 290L
            )
        )
    }

    private fun getAppDisplayName(packageName: String): String {
        return when (packageName) {
            "com.example.clarimind" -> "ClariMind"
            "com.whatsapp" -> "WhatsApp"
            "com.facebook.katana" -> "Facebook"
            "com.instagram.android" -> "Instagram"
            "com.twitter.android" -> "Twitter"
            "com.google.android.youtube" -> "YouTube"
            "com.google.android.apps.maps" -> "Maps"
            "com.google.android.gm" -> "Gmail"
            "com.android.chrome" -> "Chrome"
            "com.android.settings" -> "Settings"
            "com.android.systemui" -> "System UI"
            "com.google.android.apps.photos" -> "Photos"
            "com.spotify.music" -> "Spotify"
            "com.netflix.mediaclient" -> "Netflix"
            "com.discord" -> "Discord"
            "com.snapchat.android" -> "Snapchat"
            "com.reddit.frontpage" -> "Reddit"
            "com.pinterest" -> "Pinterest"
            "com.linkedin.android" -> "LinkedIn"
            "com.microsoft.teams" -> "Teams"
            "com.skype.raider" -> "Skype"
            "com.zoom.us" -> "Zoom"
            "com.google.android.apps.docs.editors.docs" -> "Google Docs"
            "com.google.android.apps.docs.editors.sheets" -> "Google Sheets"
            "com.google.android.apps.docs.editors.slides" -> "Google Slides"
            "com.google.android.calendar" -> "Calendar"
            "com.google.android.keep" -> "Keep"
            "com.google.android.apps.drive" -> "Drive"
            "com.google.android.apps.meetings" -> "Meet"
            "com.google.android.apps.classroom" -> "Classroom"
            "com.google.android.apps.translate" -> "Translate"
            "com.google.android.apps.messaging" -> "Messages"
            "com.google.android.apps.contacts" -> "Contacts"
            "com.google.android.apps.phone" -> "Phone"
            "com.google.android.apps.camera" -> "Camera"
            "com.google.android.apps.gallery" -> "Gallery"
            "com.google.android.apps.music" -> "Music"
            "com.google.android.apps.videos" -> "Videos"
            "com.google.android.apps.books" -> "Books"
            "com.google.android.apps.magazines" -> "News"
            "com.google.android.apps.weather" -> "Weather"
            "com.google.android.apps.fitness" -> "Fitness"
            "com.google.android.apps.walletnfcrel" -> "Wallet"
            "com.google.android.apps.pay" -> "Google Pay"
            "com.google.android.apps.tachyon" -> "Duo"
            "com.google.android.dialer" -> "Phone"
            "com.android.launcher" -> "Home Screen"
            "com.oplus.sos" -> "Emergency"
            "com.amazon.dee.app" -> "Amazon"
            "com.oplus.wirelesssettings" -> "WiFi Settings"
            "com.google.android.contacts" -> "Contacts"
            "com.coloros.alarmclock" -> "Clock"
            "com.oplus.camera" -> "Camera"
            "com.oppo.quicksearchbox" -> "Search"
            "com.iodkols.onekeylockscreen" -> "Lock Screen"
            "com.google.android.permissioncontroller" -> "Permissions"
            "com.android.server.telecom" -> "Phone Service"
            "com.google.android.gms" -> "Google Services"
            "com.oplus.romupdate" -> "System Update"
            "com.android.vending" -> "Play Store"
            "in.amazon.mShop.android.shopping" -> "Amazon Shopping"
            "com.google.android.googlequicksearchbox" -> "Google Search"
            "org.telegram.messenger" -> "Telegram"
            "com.coloros.weather2" -> "Weather"
            "com.coloros.weather.service" -> "Weather Service"
            "com.oplus.healthservice" -> "Health Service"
            "com.oplus.screenshot" -> "Screenshot"
            "com.oplus.wallpapers" -> "Wallpapers"
            "com.oplus.battery" -> "Battery"
            "com.oplus.gesture" -> "Gestures"
            "com.oplus.securitypermission" -> "Security"
            "com.oplus.account" -> "Account"
            "com.oplus.appplatform" -> "App Platform"
            "com.oplus.notificationmanager" -> "Notifications"
            "com.oplus.mediacontroller" -> "Media Controller"
            "com.oplus.powermonitor" -> "Power Monitor"
            "com.oplus.trafficmonitor" -> "Traffic Monitor"
            "com.oplus.statistics.rom" -> "Statistics"
            "com.oplus.cosa" -> "COSA"
            "com.oplus.lfeh" -> "LFEH"
            "com.oplus.athena" -> "Athena"
            "com.oplus.deepthinker" -> "Deep Thinker"
            "com.oplus.nas" -> "NAS"
            "com.oplus.nhs" -> "NHS"
            "com.oplus.olc" -> "OLC"
            "com.oplus.sau" -> "SAU"
            "com.oplus.rom" -> "ROM"
            "com.oplus.ota" -> "OTA"
            "com.oplus.crashbox" -> "Crash Box"
            "com.oplus.exsystemservice" -> "System Service"
            "com.oplus.subsys" -> "Subsystem"
            "com.oplus.atlas" -> "Atlas"
            "com.oplus.stdid" -> "STDID"
            "com.oplus.melody" -> "Melody"
            "com.oplus.wifibackuprestore" -> "WiFi Backup"
            "com.oplus.accessory" -> "Accessory"
            "com.oplus.audio.effectcenter" -> "Audio Effects"
            "com.oplus.location" -> "Location"
            "com.oplus.screenrecorder" -> "Screen Recorder"
            "com.oplus.backuprestore" -> "Backup Restore"
            "com.oplus.photopicker" -> "Photo Picker"
            else -> packageName.substringAfterLast(".")
        }
    }

    // App category mapping
    private val appCategoryMap = mapOf(
        "com.instagram.android" to "Social",
        "com.facebook.katana" to "Social",
        "com.twitter.android" to "Social",
        "com.snapchat.android" to "Social",
        "com.whatsapp" to "Messaging",
        "org.telegram.messenger" to "Messaging",
        "com.google.android.gm" to "Productivity",
        "com.microsoft.teams" to "Productivity",
        "com.google.android.apps.docs.editors.docs" to "Productivity",
        "com.google.android.youtube" to "Entertainment",
        "com.netflix.mediaclient" to "Entertainment",
        "com.spotify.music" to "Entertainment",
        // Add more mappings as needed
    )

    // Analyze user behavior and return insights
    fun analyzeUserBehavior(appUsages: List<AppUsage>): List<String> {
        val usageByCategory = mutableMapOf<String, Long>()
        for (app in appUsages) {
            val category = appCategoryMap[app.packageName] ?: "Other"
            usageByCategory[category] = (usageByCategory[category] ?: 0) + app.usageTime
        }
        val insights = mutableListOf<String>()
        if ((usageByCategory["Social"] ?: 0) > 120) {
            insights.add("You’ve spent over 2 hours on social media today. Consider taking a break.")
        }
        if ((usageByCategory["Messaging"] ?: 0) > 60) {
            insights.add("A lot of time spent messaging. Try to disconnect for a while.")
        }
        if ((usageByCategory["Entertainment"] ?: 0) > 90) {
            insights.add("High entertainment app usage detected. Balance it with other activities.")
        }
        if ((usageByCategory["Productivity"] ?: 0) > 180) {
            insights.add("Great job staying productive! Remember to take breaks to avoid burnout.")
        }
        if (insights.isEmpty()) {
            insights.add("Your app usage looks balanced today. Keep it up!")
        }
        return insights
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getAppUsageForDate(context: Context, date: String, onResult: (List<AppUsageEntity>) -> Unit) {
        viewModelScope.launch {
            val db = UsageDatabase.getInstance(context)
            val usages = db.usageDao().getUsagesForDate(date)
            onResult(usages)
        }
    }
}