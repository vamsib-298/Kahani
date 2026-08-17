package com.vl.kahani.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.vl.kahani.data.AppLanguage
import com.vl.kahani.data.LocalStore
import com.vl.kahani.data.LocalStrings
import com.vl.kahani.ui.components.*
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val store = LocalStore.current
    val strings = LocalStrings.current
    val nav = LocalNavigator.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var currentName by remember { mutableStateOf("") }

    LaunchedEffect(store.userIdentifier) {
        val doc = FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser?.uid ?: "").get().await()
        currentName = doc.getString("displayName") ?: store.userIdentifier
    }

    var profileImageUri by remember { mutableStateOf<Uri?>(null) }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                isUploading = true
                try {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val ref = FirebaseStorage.getInstance().reference.child("users/${user.uid}/profile.jpg")
                        ref.putFile(uri).await()
                        val downloadUrl = ref.downloadUrl.await()
                        profileImageUri = downloadUrl
                        FirebaseFirestore.getInstance().collection("users").document(user.uid)
                            .update("profileUrl", downloadUrl.toString())
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                } finally {
                    isUploading = false
                }
            }
        }
    }

    Column(
        modifier
            .fillMaxSize()
            .background(KahaniColors.Maroon900)
            .verticalScroll(rememberScrollState()),
    ) {
        ScreenTitleBar(
            title = "Profile",
            actions = {
                var showLangMenu by remember { mutableStateOf(false) }
                Box {
                    IconTapTarget(onClick = { showLangMenu = true }) {
                        Text("🌐", fontSize = 20.sp)
                    }
                    DropdownMenu(
                        expanded = showLangMenu,
                        onDismissRequest = { showLangMenu = false },
                        modifier = Modifier.background(KahaniColors.Maroon800)
                    ) {
                        listOf(AppLanguage.TELUGU, AppLanguage.HINDI, AppLanguage.ENGLISH).forEach { lang ->
                            DropdownMenuItem(
                                text = { Text(lang.nativeName, color = KahaniColors.TextPrimary) },
                                onClick = {
                                    store.applyUiLanguage(lang)
                                    showLangMenu = false
                                }
                            )
                        }
                    }
                }
            }
        )

        // User Header
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = KahaniSpacing.lg, vertical = KahaniSpacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(KahaniColors.Maroon700)
                        .clickable(enabled = !isUploading) { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    if (profileImageUri != null) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Profile Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = store.userIdentifier.firstOrNull()?.toString()?.uppercase() ?: "K",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = KahaniColors.Saffron,
                        )
                    }
                    if (isUploading) {
                        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = KahaniColors.Saffron)
                        }
                    } else {
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .background(KahaniColors.Saffron, CircleShape)
                                .border(2.dp, KahaniColors.Maroon900, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✏️", fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(KahaniSpacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = currentName, style = KahaniType.CardTitle, color = KahaniColors.TextPrimary)
                    IconTapTarget(onClick = { showNameDialog = true }, modifier = Modifier.size(32.dp)) {
                        Text("✏️", fontSize = 12.sp)
                    }
                }
            }
        }

        if (showNameDialog) {
            NameEditDialog(
                initialName = currentName,
                onDismiss = { showNameDialog = false },
                onSave = { newName ->
                    coroutineScope.launch {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                        FirebaseFirestore.getInstance().collection("users").document(uid).update("displayName", newName)
                        currentName = newName
                        showNameDialog = false
                    }
                }
            )
        }

        // Coins & Activity Card
        Column(Modifier.padding(horizontal = KahaniSpacing.md, vertical = KahaniSpacing.xs)) {
            KahaniCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    StatItem(
                        label = strings.following, 
                        value = store.followedAuthorIds.size.toString(),
                        onClick = { nav.go(Screen.Following) }
                    )
                    StatItem(label = strings.navLibrary, value = store.savedSeriesIds.size.toString())
                    Column(
                        Modifier.clickable { nav.selectTab(Screen.Wallet) },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = store.coinBalance.toString(), style = KahaniType.ChapterTitle, color = KahaniColors.Saffron)
                        Text(text = strings.coinBalance, style = KahaniType.Micro, color = KahaniColors.TextMuted)
                    }
                }
            }
        }

        Spacer(Modifier.height(KahaniSpacing.sm))

        // Preferred Languages
        SettingsSection("Preferred Content") {
            val languages = listOf(AppLanguage.TELUGU, AppLanguage.HINDI, AppLanguage.ENGLISH)
            Row(
                Modifier.padding(KahaniSpacing.sm).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.xs),
            ) {
                languages.forEach { language ->
                    KahaniChip(
                        label = language.nativeName,
                        selected = store.contentLanguages.contains(language),
                        onClick = { store.toggleContentLanguage(language) },
                    )
                }
            }
        }

        // Feature: Data Saver & Support
        SettingsSection("Preferences & Support") {
            SettingToggleRow(
                title = strings.dataSaver,
                body = strings.dataSaverBody,
                checked = store.dataSaverMode,
                onCheckedChange = { store.setDataSaver(it) }
            )
            HairlineDivider()
            NavRow(
                title = strings.helpSupport,
                onClick = {
                    val msg = "needed help and support for the kahani"
                    val url = "https://api.whatsapp.com/send?phone=918500717800&text=${Uri.encode(msg)}"
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }
            )
        }

        // Notifications
        SettingsSection(strings.notificationPrefs) {
            SettingToggleRow(title = strings.newChapterAlerts, body = null, checked = store.newChapterAlerts, onCheckedChange = { store.updateNewChapterAlerts(it) })
            HairlineDivider()
            SettingToggleRow(title = strings.editorialAlerts, body = null, checked = store.editorialAlerts, onCheckedChange = { store.updateEditorialAlerts(it) })
            HairlineDivider()
            SettingToggleRow(title = strings.lowCoinAlerts, body = null, checked = store.lowCoinAlerts, onCheckedChange = { store.updateLowCoinAlerts(it) })
        }

        SettingsSection(strings.account) {
            NavRow(title = "Invite Friends", onClick = {
                val sendIntent = Intent().apply { action = Intent.ACTION_SEND; putExtra(Intent.EXTRA_TEXT, "Read stories on Kahani: https://kahani.app"); type = "text/plain" }
                context.startActivity(Intent.createChooser(sendIntent, null))
            })
            HairlineDivider()
            NavRow(title = strings.logOut, onClick = { showLogoutDialog = true })
        }

        Spacer(Modifier.height(KahaniSpacing.xxl))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = { PrimaryButton(text = "Log Out", onClick = { store.logout(); nav.selectTab(Screen.Home); showLogoutDialog = false }) },
            dismissButton = { GhostButton(text = "Cancel", onClick = { showLogoutDialog = false }) },
            containerColor = KahaniColors.Maroon800,
            titleContentColor = KahaniColors.TextPrimary,
            textContentColor = KahaniColors.TextMuted,
        )
    }
}

@Composable
private fun NameEditDialog(initialName: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Name") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = KahaniColors.Maroon800,
                        unfocusedContainerColor = KahaniColors.Maroon800,
                        focusedTextColor = KahaniColors.TextPrimary,
                        unfocusedTextColor = KahaniColors.TextPrimary,
                        focusedIndicatorColor = KahaniColors.Saffron
                    )
                )
            }
        },
        confirmButton = {
            PrimaryButton(text = "Save", onClick = { onSave(name) }, enabled = name.isNotBlank())
        },
        dismissButton = {
            GhostButton(text = "Cancel", onClick = onDismiss)
        },
        containerColor = KahaniColors.Maroon800
    )
}

@Composable
private fun SettingsSection(label: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(horizontal = KahaniSpacing.md)) {
        SectionHeader(label = label, modifier = Modifier.padding(top = KahaniSpacing.md, bottom = KahaniSpacing.xs))
        KahaniCard(Modifier.fillMaxWidth()) { Column { content() } }
    }
}

@Composable
private fun StatItem(label: String, value: String, onClick: (() -> Unit)? = null) {
    Column(
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = value, style = KahaniType.ChapterTitle, color = KahaniColors.Saffron)
        Text(text = label, style = KahaniType.Micro, color = KahaniColors.TextMuted)
    }
}
