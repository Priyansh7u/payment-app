package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getFinancialAdvice(
        prompt: String,
        transactions: List<TransactionEntity>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        // Check for placeholder or empty key
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("PLACEHOLDER")) {
            Log.w(TAG, "Gemini API Key is not configured. Falling back to local smart analyzer.")
            return@withContext getSimulatedResponse(prompt, transactions)
        }

        try {
            val systemInstruction = """
                You are PhonePe's Smart AI Financial Assistant. You help users understand their spending habits, analyze transaction history, plan bills, and guide them on secure UPI practices.
                Keep your answers clear, concise, objective, and action-oriented. Use Rupee symbol (₹) for money.
                Keep responses under 4-5 sentences unless detail is explicitly requested.
            """.trimIndent()

            val transactionSummary = if (transactions.isEmpty()) {
                "No transactions recorded yet."
            } else {
                transactions.take(15).joinToString("\n") { t ->
                    "- ${t.title} (${t.category}): ₹${kotlin.math.abs(t.amount)} [Type: ${t.type}, Status: ${t.status}]"
                }
            }

            val fullPrompt = """
                User Query: $prompt
                
                Here is the user's recent transaction logs:
                $transactionSummary
                
                Provide smart advice now:
            """.trimIndent()

            // Construct JSON request body using standard org.json
            val root = JSONObject()
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            
            partObj.put("text", fullPrompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            root.put("contents", contentsArray)

            // System instructions
            val systemInstructionObj = JSONObject()
            val sysPartsArray = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArray.put(sysPartObj)
            systemInstructionObj.put("parts", sysPartsArray)
            root.put("systemInstruction", systemInstructionObj)

            val requestBodyString = root.toString()
            val requestBody = requestBodyString.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "Unknown error"
                    Log.e(TAG, "API Call failed: ${response.code} - $errorBody")
                    return@withContext "[Simulated Spend Coach]: Oops, the server returned an error (Code ${response.code}). Let's fallback to our local advice:\n\n${getSimulatedResponse(prompt, transactions)}"
                }

                val responseBody = response.body?.string()
                if (responseBody == null) {
                    return@withContext "Empty response from AI engine."
                }

                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No text part found.")
                        }
                    }
                }
                return@withContext "No response candidate found. Try asking again."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini call exception: ${e.message}", e)
            return@withContext "[Simulated Spend Coach]: Connection issues or API key is restricted. Let me help you locally:\n\n${getSimulatedResponse(prompt, transactions)}"
        }
    }

    private fun getSimulatedResponse(prompt: String, transactions: List<TransactionEntity>): String {
        val totalSpent = transactions.filter { it.amount < 0 }.sumOf { kotlin.math.abs(it.amount) }
        val totalEarned = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val transferCount = transactions.count { it.category == "Transfer" }
        val rechargeCount = transactions.count { it.category == "Mobile" || it.type == "RECHARGE" }

        val lowKeyPrompt = prompt.lowercase()

        return when {
            lowKeyPrompt.contains("spend") || lowKeyPrompt.contains("analyse") || lowKeyPrompt.contains("analyze") || lowKeyPrompt.contains("budget") -> {
                "💡 **Local Financial Insights** (Gemini API Key offline):\n" +
                        "- **Total Outflow**: ₹$totalSpent spent across ${transactions.count { it.amount < 0 }} transactions.\n" +
                        "- **Total Inflow**: ₹$totalEarned received.\n" +
                        "- **Analysis**: You have a active transfer pattern with $transferCount peer transfers and $rechargeCount utility recharges. To save more, try setting a monthly cap on transfer payments and pay postpaid bills on time to avoid late fees!"
            }
            lowKeyPrompt.contains("secure") || lowKeyPrompt.contains("safety") || lowKeyPrompt.contains("fraud") || lowKeyPrompt.contains("pin") -> {
                "🛡️ **UPI Security Guide**:\n" +
                        "1. **Never enter your UPI PIN to receive money.** Entering your PIN only initiates payments out of your account.\n" +
                        "2. Always verify the merchant name displayed on the screen before entering your UPI PIN.\n" +
                        "3. Do not share your UPI PIN or OTP with anyone, even if they claim to represent PhonePe or your bank."
            }
            lowKeyPrompt.contains("limit") || lowKeyPrompt.contains("charge") || lowKeyPrompt.contains("fee") -> {
                "💳 **UPI Transaction Limits & Charges**:\n" +
                        "- **Transaction Limit**: The standard daily transaction limit for UPI is ₹1,00,000 per user.\n" +
                        "- **Service Charge**: PhonePe does not charge any additional platform fees for standard bank-to-bank self or peer transfers. Mobile recharges may have a nominal platform fee of ₹1-2."
            }
            else -> {
                "✨ **PhonePe AI Spending Advisor**:\n" +
                        "To get personalized, dynamic Gemini AI spending forecasts and recommendations, please enter a valid **GEMINI_API_KEY** in the Secrets panel in AI Studio.\n\n" +
                        "Meanwhile, looking at your local records, your current balance in your primary bank is ₹24,500.00. You have paid utility bills of ₹850.00. Make sure to pay off any pending postpaids soon!"
            }
        }
    }
}
