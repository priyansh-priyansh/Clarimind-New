# Gemini AI Integration Setup

This guide will help you set up the Gemini AI integration for the ClariMind chatbot.

## Prerequisites

1. A Google account
2. Access to Google AI Studio (formerly MakerSuite)

## Step 1: Get Your Gemini API Key

1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated API key

## Step 2: Configure the API Key

1. Open `app/src/main/java/com/example/clarimind/config/GeminiConfig.kt`
2. Replace `YOUR_GEMINI_API_KEY_HERE` with your actual API key:

```kotlin
const val API_KEY = "your_actual_api_key_here"
```

## Step 3: Test the Integration

1. Build and run the app
2. Complete the emotion detection and questionnaire
3. Click "Talk to Chatbot" on the dashboard
4. The chatbot should now respond using the Gemini AI

## Features

The chatbot is trained to:

- Provide empathetic, supportive responses
- Offer practical mental health advice
- Suggest activities based on the user's mood and PHI scores
- Help users understand their emotional state
- Provide coping strategies when needed
- Always prioritize user safety and well-being

## Fallback Mode

If the API key is not configured or there's an error, the chatbot will fall back to pre-programmed responses based on the user's emotional state.

## Security Notes

- Never commit your API key to version control
- For production apps, consider using encrypted storage or a backend proxy
- The API key is currently stored in plain text for development purposes

## Troubleshooting

1. **Chatbot not responding**: Check your internet connection and API key
2. **API errors**: Verify your API key is correct and has proper permissions
3. **Slow responses**: This is normal for AI API calls; responses may take 1-3 seconds

## API Usage

The chatbot uses the `gemini-1.5-flash` model, which is optimized for:

- Fast response times
- Conversational interactions
- Mental health support scenarios
- Cost-effective usage

## Cost Considerations

- Gemini API has usage limits and costs
- Monitor your usage in the Google AI Studio dashboard
- Consider implementing rate limiting for production apps
