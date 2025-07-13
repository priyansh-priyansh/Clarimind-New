# Real Screen Time Data Setup

Your ClariMind app now fetches real screen time data from your device! Here's how it works:

## 🔧 **What's New:**

✅ **Real Data Integration** - Uses Android's `UsageStatsManager`  
✅ **Permission Handling** - Automatic permission requests  
✅ **Live Statistics** - Actual app usage and screen time  
✅ **Error Handling** - Graceful fallbacks and retry options  
✅ **Smart App Names** - Recognizes popular apps by package name

## 📱 **How to Enable:**

### **Step 1: Grant Permission**

1. Open ClariMind app
2. Navigate to Dashboard → "View Screen Time"
3. Tap "Grant Permission" when prompted
4. You'll be taken to Android's Usage Access settings

### **Step 2: Enable Usage Access**

1. Find "ClariMind" in the list
2. Toggle the switch to enable usage access
3. Return to the app

### **Step 3: View Real Data**

The app will now show:

- **Today's actual screen time**
- **Your most used apps** (with real usage times)
- **Weekly usage patterns**
- **Daily breakdown** of the past week

## 📊 **Data Collected:**

- **Screen time duration** (in hours and minutes)
- **App usage statistics** (top 5 most used apps)
- **Daily and weekly patterns**
- **Usage trends** over time

## 🔒 **Privacy & Security:**

- **Local processing only** - Data stays on your device
- **No data transmission** - Nothing sent to servers
- **Permission-based** - Only collects what you allow
- **Transparent** - Clear permission requests

## 🛠 **Technical Details:**

### **Permissions Required:**

```xml
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" />
```

### **API Used:**

- `UsageStatsManager` - Android's official usage statistics API
- `AppOpsManager` - Permission checking
- `Calendar` - Date/time calculations

### **Data Processing:**

- Converts milliseconds to hours/minutes
- Aggregates daily and weekly statistics
- Maps package names to readable app names
- Handles edge cases and errors

## 🎯 **Features:**

### **Real-Time Data:**

- Live screen time tracking
- Actual app usage statistics
- Current day and historical data

### **Smart App Recognition:**

Recognizes 50+ popular apps including:

- Social Media: WhatsApp, Facebook, Instagram, Twitter
- Productivity: Gmail, Chrome, Maps, Calendar
- Entertainment: YouTube, Netflix, Spotify
- Communication: Teams, Zoom, Skype
- And many more!

### **User-Friendly Display:**

- Clean, card-based layout
- Easy-to-read time formats
- Visual charts and graphs
- Helpful digital wellness tips

## 🚀 **Testing:**

1. **Build and run** the app
2. **Complete the flow**: Login → Welcome → Camera → Questions → Dashboard
3. **Click "View Screen Time"**
4. **Grant permission** when prompted
5. **Explore your real usage data!**

## 🔧 **Troubleshooting:**

### **Permission Issues:**

- Make sure to enable "Usage Access" in Android settings
- Some devices may require additional permissions
- Check if your device supports usage statistics

### **Data Accuracy:**

- Data may take a few minutes to update
- Some system apps may not show usage time
- Background app restrictions may affect data

### **Performance:**

- Data loading may take 1-3 seconds
- Large usage histories may slow down the app
- Consider implementing caching for better performance

## 📈 **Future Enhancements:**

Potential improvements:

- **App usage notifications** - Get alerts when you exceed limits
- **Usage goals** - Set daily screen time targets
- **Detailed analytics** - Hour-by-hour breakdown
- **Export data** - Save usage reports
- **Family sharing** - Monitor family device usage

Your screen time feature is now powered by real device data! 📱⏰
