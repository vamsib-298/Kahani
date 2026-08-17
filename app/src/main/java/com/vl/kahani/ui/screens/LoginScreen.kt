package com.vl.kahani.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.ui.components.PrimaryButton
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import com.vl.kahani.ui.theme.Narrative
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(modifier: Modifier = Modifier, onLoginSuccess: (String) -> Unit = {}) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val auth = remember { FirebaseAuth.getInstance() }

    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var usePhone by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            isLoading = true
            auth.signInWithCredential(credential)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        onLoginSuccess(auth.currentUser?.phoneNumber ?: phoneNumber)
                    }
                }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            isLoading = false
            Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
            // On internal error like 17006, we can't do much from code, but let's not auto-login
        }

        override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
            isLoading = false
            verificationId = id
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(KahaniColors.Maroon900),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(KahaniSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(KahaniSpacing.xxl))
            Text(
                text = strings.appName,
                fontFamily = Narrative,
                fontSize = 48.sp,
                color = KahaniColors.Saffron,
            )
            Spacer(Modifier.height(KahaniSpacing.md))
            Text(
                text = strings.tagline,
                style = KahaniType.UiBody,
                color = KahaniColors.TextMuted,
            )

            Spacer(Modifier.height(KahaniSpacing.xxl))
            Text(
                text = if (usePhone) "Sign in with Phone" else "Sign in with Email",
                style = KahaniType.SectionLabel,
                color = KahaniColors.TextPrimary,
            )
            Spacer(Modifier.height(KahaniSpacing.md))

            if (usePhone) {
                if (verificationId == null) {
                    TextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it.take(10) },
                        label = { Text("Phone Number", color = KahaniColors.TextMuted) },
                        placeholder = { Text("10-digit number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KahaniSpacing.md),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = KahaniColors.Maroon800,
                            unfocusedContainerColor = KahaniColors.Maroon800,
                            focusedTextColor = KahaniColors.TextPrimary,
                            unfocusedTextColor = KahaniColors.TextPrimary,
                            focusedLabelColor = KahaniColors.Saffron,
                            cursorColor = KahaniColors.Saffron,
                            focusedIndicatorColor = KahaniColors.Saffron,
                            unfocusedIndicatorColor = KahaniColors.Maroon700,
                        ),
                        singleLine = true,
                    )
                } else {
                    TextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it.take(6) },
                        label = { Text("Enter OTP", color = KahaniColors.TextMuted) },
                        placeholder = { Text("6-digit code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = KahaniSpacing.md),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = KahaniColors.Maroon800,
                            unfocusedContainerColor = KahaniColors.Maroon800,
                            focusedTextColor = KahaniColors.TextPrimary,
                            unfocusedTextColor = KahaniColors.TextPrimary,
                            focusedLabelColor = KahaniColors.Saffron,
                            cursorColor = KahaniColors.Saffron,
                            focusedIndicatorColor = KahaniColors.Saffron,
                            unfocusedIndicatorColor = KahaniColors.Maroon700,
                        ),
                        singleLine = true,
                    )
                }
            } else {
                // Email field remains for demo fallback
                TextField(
                    value = phoneNumber, // Reusing phoneNumber field as email for simplicity in this branch
                    onValueChange = { phoneNumber = it },
                    label = { Text("Email", color = KahaniColors.TextMuted) },
                    placeholder = { Text("your@email.com") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = KahaniSpacing.md),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = KahaniColors.Maroon800,
                        unfocusedContainerColor = KahaniColors.Maroon800,
                        focusedTextColor = KahaniColors.TextPrimary,
                        unfocusedTextColor = KahaniColors.TextPrimary,
                        focusedLabelColor = KahaniColors.Saffron,
                        cursorColor = KahaniColors.Saffron,
                        focusedIndicatorColor = KahaniColors.Saffron,
                        unfocusedIndicatorColor = KahaniColors.Maroon700,
                    ),
                    singleLine = true,
                )
            }

            Spacer(Modifier.height(KahaniSpacing.lg))
            PrimaryButton(
                text = when {
                    isLoading -> "Loading..."
                    usePhone && verificationId == null -> "Send OTP"
                    usePhone -> "Verify OTP"
                    else -> "Continue"
                },
                onClick = {
                    if (usePhone) {
                        if (verificationId == null) {
                            isLoading = true
                            val fullNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
                            val options = PhoneAuthOptions.newBuilder(auth)
                                .setPhoneNumber(fullNumber)
                                .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
                                .setActivity(context as android.app.Activity)
                                .setCallbacks(callbacks)
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        } else {
                            isLoading = true
                            val credential = PhoneAuthProvider.getCredential(verificationId!!, verificationCode)
                            auth.signInWithCredential(credential)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        onLoginSuccess(auth.currentUser?.phoneNumber ?: phoneNumber)
                                    }
                                }
                        }
                    }
                },
                enabled = !isLoading && (
                    (usePhone && verificationId == null && phoneNumber.length == 10) ||
                    (usePhone && verificationId != null && verificationCode.length == 6) ||
                    (!usePhone && phoneNumber.contains("@"))
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = KahaniSpacing.md),
            )

            Spacer(Modifier.height(KahaniSpacing.xxl))
            Text(
                text = "Or continue with",
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(KahaniSpacing.md))
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                                serverClientId = "72252954148-rid2t8moaj8d5rjhjvlc117endnebvmh.apps.googleusercontent.com"
                            ).build()

                            val request = GetCredentialRequest.Builder()
                                .addCredentialOption(signInWithGoogleOption)
                                .build()

                            val result = credentialManager.getCredential(
                                request = request,
                                context = context,
                            )
                            
                            val credential = result.credential
                            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                
                                // Real Firebase Authentication
                                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                                isLoading = true
                                auth.signInWithCredential(firebaseCredential)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            val user = auth.currentUser
                                            onLoginSuccess(user?.email ?: user?.uid ?: "user@google.com")
                                        } else {
                                            Toast.makeText(context, "Google Sign-in failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        } catch (e: GetCredentialException) {
                            Toast.makeText(context, "Credential Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = KahaniSpacing.md),
                colors = ButtonDefaults.buttonColors(
                    containerColor = KahaniColors.Maroon800,
                    contentColor = KahaniColors.TextPrimary,
                ),
                shape = RoundedCornerShape(KahaniRadius.pill),
            ) {
                Text("Sign in with Google", style = KahaniType.UiBody)
            }

            Spacer(Modifier.height(KahaniSpacing.xxl))
            Text(
                text = "By signing in, you agree to our Terms of Service and Privacy Policy.",
                style = KahaniType.Micro,
                color = KahaniColors.TextMuted,
                modifier = Modifier.padding(horizontal = KahaniSpacing.md),
            )
        }
    }
}
