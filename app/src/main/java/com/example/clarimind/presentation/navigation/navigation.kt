package com.example.clarimind.presentation.navigation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.clarimind.presentation.model.PHIScore
import com.example.clarimind.presentation.model.User
import com.example.clarimind.presentation.screens.ChatbotScreen
import com.example.clarimind.presentation.screens.DashBoardScreen
import com.example.clarimind.presentation.screens.EmotionCameraScreen
import com.example.clarimind.presentation.screens.QuestionnaireScreen
import com.example.clarimind.presentation.viewmodels.DashboardViewModel
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("ContextCastToActivity")
@Composable
fun NavGraph() {

     val context = LocalContext.current
     val navController = rememberNavController()

     NavHost(
          navController = navController,
          startDestination = EmotionCameraScreen
     ) {
          composable<EmotionCameraScreen> {
               EmotionCameraScreen(
                    onEmotionDetected = { emotion ->
                         navController.navigate(QuestionsScreen(emotion)) {
                              popUpTo<EmotionCameraScreen> { inclusive = true }
                         }
                    },
                    onBackPressed = {
                         (context as? Activity)?.finishAffinity()
                    }
               )
          }

          composable<QuestionsScreen> {
               val args = it.toRoute<QuestionsScreen>()
               QuestionnaireScreen(
                    mood = args.mood,
                    onResultsCalculated = { phiScore ->
                         navController.navigate(
                              DashBoardScreen(
                                   mood = args.mood,
                                   rememberedWellBeing = phiScore.rememberedWellBeing,
                                   experiencedWellBeing = phiScore.experiencedWellBeing,
                                   combinedPHI = phiScore.combinedPHI
                              )
                         ) {
                              popUpTo<QuestionsScreen> { inclusive = true }
                         }
                    }
               )
          }

          composable<DashBoardScreen> {
               val args = it.toRoute<DashBoardScreen>()
               val dashboardViewModel: DashboardViewModel = viewModel()
               val user = FirebaseAuth.getInstance().currentUser

               // Reconstruct PHIScore from individual parameters
               val phiScore = PHIScore(
                    rememberedWellBeing = args.rememberedWellBeing,
                    experiencedWellBeing = args.experiencedWellBeing,
                    combinedPHI = args.combinedPHI
               )

               val currentUser = User(
                    name = user?.displayName.toString(),
                    email = user?.email.toString(),
                    profilePhotoUrl = user?.photoUrl.toString()
               )

               DashBoardScreen(
                    mood = args.mood,
                    phiScore = phiScore,
                    user = currentUser,
                    viewModel = dashboardViewModel,
                    onRetakeAssessment = {
                         navController.navigate(EmotionCameraScreen) {
                              popUpTo<DashBoardScreen> { inclusive = true }
                         }
                    },
                    onChatbotClick = {
                         navController.navigate(
                              ChatbotScreen(
                                   mood = args.mood,
                                   rememberedWellBeing = args.rememberedWellBeing,
                                   experiencedWellBeing = args.experiencedWellBeing,
                                   combinedPHI = args.combinedPHI
                              )
                         )
                    }
               )
          }

          composable<ChatbotScreen> {
               val args = it.toRoute<ChatbotScreen>()
               val phiScore = PHIScore(
                    rememberedWellBeing = args.rememberedWellBeing,
                    experiencedWellBeing = args.experiencedWellBeing,
                    combinedPHI = args.combinedPHI
               )
               
               ChatbotScreen(
                    mood = args.mood,
                    phiScore = phiScore,
                    onBackPressed = {
                         navController.popBackStack()
                    }
               )
          }
     }
}