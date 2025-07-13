package com.example.clarimind.presentation.navigation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.clarimind.presentation.screens.EmotionCameraScreen
import com.example.clarimind.presentation.screens.QuestionnaireScreen

@SuppressLint("ContextCastToActivity")
@Composable
fun NavGraph() {

     val context = LocalContext.current
     val navController = rememberNavController()
     
     NavHost(
          navController = navController,
          startDestination = Routes.EmotionCameraScreen
     ) {
          composable<Routes.EmotionCameraScreen> {
               EmotionCameraScreen(
                    onEmotionDetected = {
                         navController.navigate(Routes.QuestionsScreen) {
                              popUpTo(Routes.EmotionCameraScreen) {inclusive = true}
                         }
                    },
                    onBackPressed = {
                         (context as? Activity)?.finishAffinity()
                    }
               )
          }

          composable<Routes.QuestionsScreen> {
               QuestionnaireScreen()
          }
     }
}