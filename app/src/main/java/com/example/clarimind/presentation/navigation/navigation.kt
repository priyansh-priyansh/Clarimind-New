package com.example.clarimind.presentation.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.clarimind.LoginActivity
import com.example.clarimind.WelcomeActivity
import com.example.clarimind.presentation.model.PHIScore
import com.example.clarimind.presentation.model.User
import com.example.clarimind.presentation.screens.ChatbotScreen
import com.example.clarimind.presentation.screens.DashBoardScreen
import com.example.clarimind.presentation.screens.EmotionCameraScreen
import com.example.clarimind.presentation.screens.QuestionnaireScreen
import com.example.clarimind.presentation.screens.ScreenTimeScreen
import com.example.clarimind.presentation.viewmodels.DashboardViewModel
import com.example.clarimind.utils.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.example.clarimind.presentation.navigation.BoxBreathingScreen
import com.example.clarimind.presentation.navigation.FourSevenEightBreathingScreen
import com.example.clarimind.presentation.navigation.AlternateNostrilBreathingScreen
import com.example.clarimind.presentation.screens.AlternateNostrilBreathingExerciseScreen
import com.example.clarimind.presentation.screens.BoxBreathingExerciseScreen
import com.example.clarimind.presentation.screens.BreathingExerciseType
import com.example.clarimind.presentation.screens.FourSevenEightBreathingExerciseScreen
import com.example.clarimind.presentation.screens.HappinessHistoryScreen
import com.example.clarimind.presentation.navigation.HappinessHistoryScreen as HappinessHistoryScreenRoute

@SuppressLint("ContextCastToActivity")
@Composable
fun NavGraph() {

     val context = LocalContext.current
     val navController = rememberNavController()
     val firebaseAuth = FirebaseAuth.getInstance()
     val userPreferences = remember { com.example.clarimind.utils.UserPreferences(context) }
     
     // Determine start destination based on whether user has completed the test
     val startDestination = if (userPreferences.hasCompletedTest()) {
          // User has completed test, get saved PHI scores and mood
          val phiScores = userPreferences.getLastPHIScores()
          val lastMood = userPreferences.getLastMood() ?: "Neutral"
          
          if (phiScores != null) {
               // We have all the data needed for the dashboard
               DashBoardScreen(
                    mood = lastMood,
                    rememberedWellBeing = phiScores.first,
                    experiencedWellBeing = phiScores.second,
                    combinedPHI = phiScores.third
               )
          } else {
               // Missing PHI scores, start with emotion camera
               EmotionCameraScreen
          }
     } else {
          // User hasn't completed test, start with emotion camera
          EmotionCameraScreen
     }

     NavHost(
          navController = navController,
          startDestination = startDestination
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
                         // Save test completion status and PHI scores
                         userPreferences.setTestCompleted(true)
                         userPreferences.setLastMood(args.mood)
                         userPreferences.savePHIScores(
                              phiScore.rememberedWellBeing,
                              phiScore.experiencedWellBeing,
                              phiScore.combinedPHI
                         )
                         
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
                    onLogout = {
                         // Clear test completion status when logging out
                         userPreferences.clearAll()
                         firebaseAuth.signOut()
                         val intent = Intent(context,LoginActivity::class.java)
                         context.startActivity(intent)
                         (context as? Activity)?.finish()
                    },
                    onRetakeAssessment = {
                         // Reset test completion status when retaking assessment
                         userPreferences.setTestCompleted(false)
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
                    },
                    onViewScreenTime = {
                         navController.navigate(ScreenTimeScreen)
                    },
                    onBreathingExerciseSelected = {
                         when (it) {
                              BreathingExerciseType.BOX -> navController.navigate(BoxBreathingScreen)
                              BreathingExerciseType.FOUR_SEVEN_EIGHT -> navController.navigate(FourSevenEightBreathingScreen)
                              BreathingExerciseType.ALTERNATE_NOSTRIL -> navController.navigate(AlternateNostrilBreathingScreen)
                         }
                    },
                    onViewHistory = {
                         navController.navigate(HappinessHistoryScreenRoute)
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

          composable<ScreenTimeScreen> {
               ScreenTimeScreen(
                    onBackPressed = {
                         navController.popBackStack()
                    }
               )
          }

          composable<BoxBreathingScreen> {
               BoxBreathingExerciseScreen(onBack = { navController.popBackStack() })
          }
          composable<FourSevenEightBreathingScreen> {
               FourSevenEightBreathingExerciseScreen(onBack = { navController.popBackStack() })
          }
          composable<AlternateNostrilBreathingScreen> {
               AlternateNostrilBreathingExerciseScreen(onBack = { navController.popBackStack() })
          }
          composable<HappinessHistoryScreenRoute> {
               HappinessHistoryScreen(onBack = { navController.popBackStack() })
          }
     }
}