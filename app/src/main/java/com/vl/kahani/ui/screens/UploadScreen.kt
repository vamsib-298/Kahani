package com.vl.kahani.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.vl.kahani.data.AppLanguage
import com.vl.kahani.data.Genre
import com.vl.kahani.data.LocalStore
import com.vl.kahani.ui.components.*
import com.vl.kahani.ui.nav.LocalNavigator
import com.vl.kahani.ui.nav.Screen
import com.vl.kahani.ui.theme.KahaniColors
import com.vl.kahani.ui.theme.KahaniRadius
import com.vl.kahani.ui.theme.KahaniSpacing
import com.vl.kahani.ui.theme.KahaniType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

private enum class UploadStep { STORY, CHAPTERS, SUCCESS, MANAGE, EDIT_CHAPTERS }

@Composable
fun UploadScreen(modifier: Modifier = Modifier) {
    var currentStep by remember { mutableStateOf(UploadStep.MANAGE) }
    var uploadedStoryId by remember { mutableStateOf("") }
    var totalChaptersToUpload by remember { mutableIntStateOf(0) }
    var currentChapterIndex by remember { mutableIntStateOf(1) }
    var editingChapterId by remember { mutableStateOf<String?>(null) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    val store = LocalStore.current

    Column(
        modifier
            .fillMaxSize()
            .background(KahaniColors.Maroon900)
    ) {
        val title = when (currentStep) {
            UploadStep.STORY -> "Create Story"
            UploadStep.CHAPTERS -> if (editingChapterId != null) "Edit Chapter" else "Upload Chapter $currentChapterIndex / $totalChaptersToUpload"
            UploadStep.SUCCESS -> "Upload Complete"
            UploadStep.MANAGE -> "My Stories"
            UploadStep.EDIT_CHAPTERS -> "Manage Chapters"
        }
        ScreenTitleBar(
            title = title,
            onBack = if (currentStep != UploadStep.SUCCESS && currentStep != UploadStep.MANAGE) {
                {
                    when (currentStep) {
                        UploadStep.CHAPTERS -> {
                            if (editingChapterId != null) {
                                currentStep = UploadStep.EDIT_CHAPTERS
                                editingChapterId = null
                            } else if (currentChapterIndex > 1) {
                                currentChapterIndex--
                            } else {
                                currentStep = UploadStep.STORY
                            }
                        }
                        UploadStep.STORY -> currentStep = UploadStep.MANAGE
                        UploadStep.EDIT_CHAPTERS -> currentStep = UploadStep.MANAGE
                        else -> {}
                    }
                }
            } else null,
            actions = {
                if (currentStep == UploadStep.MANAGE) {
                    IconTapTarget(onClick = { currentStep = UploadStep.STORY }) {
                        PlusGlyph(tint = KahaniColors.Saffron)
                    }
                }
            }
        )

        when (currentStep) {
            UploadStep.MANAGE -> {
                MyStoriesList(
                    onEditStory = { id ->
                        uploadedStoryId = id
                        currentStep = UploadStep.STORY
                    },
                    onManageChapters = { id ->
                        uploadedStoryId = id
                        currentStep = UploadStep.EDIT_CHAPTERS
                    },
                    onAddNext = { id, count ->
                        uploadedStoryId = id
                        totalChaptersToUpload = count + 1
                        currentChapterIndex = count + 1
                        currentStep = UploadStep.CHAPTERS
                    },
                    onStartNew = { currentStep = UploadStep.STORY },
                    onWithdraw = { showWithdrawDialog = true }
                )
            }
            UploadStep.EDIT_CHAPTERS -> {
                ManageChaptersScreen(
                    storyId = uploadedStoryId,
                    onEdit = { chId, num ->
                        editingChapterId = chId
                        currentChapterIndex = num
                        currentStep = UploadStep.CHAPTERS
                    }
                )
            }
            UploadStep.STORY -> {
                StoryUploadForm(
                    initialId = if (uploadedStoryId.isNotEmpty() && currentStep == UploadStep.STORY) uploadedStoryId else null,
                    onSuccess = { id, count ->
                        uploadedStoryId = id
                        totalChaptersToUpload = count
                        currentStep = UploadStep.CHAPTERS
                    }
                )
            }
            UploadStep.CHAPTERS -> {
                ChapterUploadForm(
                    storyId = uploadedStoryId,
                    chapterNum = currentChapterIndex,
                    editingId = editingChapterId,
                    onSuccess = {
                        if (editingChapterId != null) {
                            currentStep = UploadStep.EDIT_CHAPTERS
                            editingChapterId = null
                        } else if (currentChapterIndex < totalChaptersToUpload) {
                            currentChapterIndex++
                        } else {
                            currentStep = UploadStep.SUCCESS
                        }
                    }
                )
            }
            UploadStep.SUCCESS -> {
                UploadSuccessScreen(onReset = {
                    currentStep = UploadStep.MANAGE
                    currentChapterIndex = 1
                    uploadedStoryId = ""
                })
            }
        }
    }

    if (showWithdrawDialog) {
        WithdrawDialog(onDismiss = { showWithdrawDialog = false })
    }
}

@Composable
private fun WithdrawDialog(onDismiss: () -> Unit) {
    var upiId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    val context = LocalContext.current

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        KahaniCard(Modifier.fillMaxWidth(), elevatedSurface = true) {
            Column(verticalArrangement = Arrangement.spacedBy(KahaniSpacing.md)) {
                Text("Withdraw Earnings", style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
                Text("Enter your UPI ID to receive payments.", style = KahaniType.Micro, color = KahaniColors.TextMuted)
                
                UploadField(label = "UPI ID (e.g. name@upi)", value = upiId, onValueChange = { upiId = it })
                UploadField(label = "Amount to Withdraw", value = amount, onValueChange = { amount = it })

                Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.sm)) {
                    GhostButton("Cancel", onClick = onDismiss, modifier = Modifier.weight(1f))
                    PrimaryButton(
                        text = if (isSending) "Sending..." else "Request Payout",
                        enabled = !isSending && upiId.contains("@") && amount.isNotEmpty(),
                        onClick = {
                            isSending = true
                            // In real app, write to 'withdrawals' collection
                            Toast.makeText(context, "Payout request sent to Admin!", Toast.LENGTH_LONG).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyStoriesList(
    onEditStory: (String) -> Unit, 
    onManageChapters: (String) -> Unit, 
    onAddNext: (String, Int) -> Unit,
    onStartNew: () -> Unit,
    onWithdraw: () -> Unit
) {
    val store = LocalStore.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userStories = store.catalog.filter { it.uploaderId == FirebaseAuth.getInstance().currentUser?.uid }

    if (userStories.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(KahaniSpacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("✨", fontSize = 48.sp)
            Spacer(Modifier.height(KahaniSpacing.md))
            Text("Post and Earn!", style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
            Spacer(Modifier.height(KahaniSpacing.xs))
            Text(
                "Share your stories and earn real money based on listeners. Reach thousands of fans!",
                style = KahaniType.UiBody,
                color = KahaniColors.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(KahaniSpacing.lg))
            PrimaryButton(text = "Start Your First Story", onClick = onStartNew)
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(KahaniSpacing.md),
            verticalArrangement = Arrangement.spacedBy(KahaniSpacing.sm)
        ) {
            items(userStories.size) { index ->
                val story = userStories[index]
                Column {
                    if (index == 0) {
                        KahaniCard(
                            Modifier.fillMaxWidth().padding(bottom = KahaniSpacing.md),
                            elevatedSurface = true
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Creator Earnings", style = KahaniType.MicroBold, color = KahaniColors.Saffron)
                                    Text("Withdraw to Bank", style = KahaniType.CardTitle, color = KahaniColors.TextPrimary)
                                }
                                Text("₹0.00", style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
                            }
                        }
                        
                        KahaniCard(
                            Modifier.fillMaxWidth().padding(bottom = KahaniSpacing.md),
                            onClick = onWithdraw
                        ) {
                            Text("Withdraw Earnings", color = KahaniColors.Saffron, style = KahaniType.UiBold)
                        }

                        Text("My Published Works", style = KahaniType.SectionLabel, color = KahaniColors.TextPrimary, modifier = Modifier.padding(bottom = KahaniSpacing.sm))
                    }
                    KahaniCard(Modifier.fillMaxWidth().clickable { onManageChapters(story.id) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(story.title, style = KahaniType.CardTitle, color = KahaniColors.TextPrimary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${story.publishStatus}", style = KahaniType.Micro, color = if (story.publishStatus == "PUBLISHED") Color.Green else KahaniColors.Saffron)
                                    if (story.publishStatus == "REJECTED" && story.rejectionReason != null) {
                                        Spacer(Modifier.width(4.dp))
                                        Text("• Reason: ${story.rejectionReason}", style = KahaniType.Micro, color = Color.Red.copy(alpha = 0.7f))
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StatMini(label = "Plays", value = story.playCount)
                                    StatMini(label = "Reads", value = story.readCount)
                                    StatMini(label = "Watches", value = story.watchCount)
                                }
                            }
                            Row {
                                IconTapTarget(onClick = { onEditStory(story.id) }) {
                                    Text("✏️", fontSize = 16.sp)
                                }
                                IconTapTarget(onClick = { onAddNext(story.id, story.totalChapters) }) {
                                    Text("➕", fontSize = 16.sp)
                                }
                                IconTapTarget(onClick = {
                                    coroutineScope.launch {
                                        try {
                                            FirebaseFirestore.getInstance().collection("series").document(story.id).delete().await()
                                            Toast.makeText(context, "Story Deleted", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error deleting", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }) {
                                    TrashGlyph(tint = Color.Red.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ManageChaptersScreen(storyId: String, onEdit: (String, Int) -> Unit) {
    val store = LocalStore.current
    val chapters = store.chapters(storyId)

    if (chapters.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No chapters found.", color = KahaniColors.TextMuted)
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(KahaniSpacing.md),
            verticalArrangement = Arrangement.spacedBy(KahaniSpacing.sm)
        ) {
            items(chapters.size) { index ->
                val chapter = chapters[index]
                KahaniCard(Modifier.fillMaxWidth().clickable { onEdit(chapter.id, chapter.chapterNumber) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Chapter ${chapter.chapterNumber}: ${chapter.title}", style = KahaniType.CardTitle, color = KahaniColors.TextPrimary)
                            Text("${chapter.wordCount} words", style = KahaniType.Micro, color = KahaniColors.TextMuted)
                        }
                        Text("Edit", color = KahaniColors.Saffron, style = KahaniType.MicroBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryUploadForm(initialId: String? = null, onSuccess: (String, Int) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var title by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf(Genre.THRILLER) }
    var language by remember { mutableStateOf(AppLanguage.HINDI) }
    var totalChapters by remember { mutableStateOf("3") }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(initialId) {
        if (initialId != null) {
            val doc = FirebaseFirestore.getInstance().collection("series").document(initialId).get().await()
            title = doc.getString("title") ?: ""
            synopsis = doc.getString("synopsis") ?: ""
            genre = Genre.valueOf(doc.getString("genre")?.uppercase() ?: "THRILLER")
            language = AppLanguage.fromCode(doc.getString("language") ?: "hi")
            totalChapters = doc.getLong("totalChapters")?.toString() ?: "3"
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { coverUri = it }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { videoUri = it }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(KahaniSpacing.md),
        verticalArrangement = Arrangement.spacedBy(KahaniSpacing.md)
    ) {
        Text("Step 1: Story Details", style = KahaniType.SectionLabel, color = KahaniColors.TextPrimary)
        
        UploadField(label = "Story Title", value = title, onValueChange = { title = it }, maxChars = 100)
        UploadField(label = "Synopsis", value = synopsis, onValueChange = { synopsis = it }, isMultiline = true, maxChars = 500)
        
        Row(horizontalArrangement = Arrangement.spacedBy(KahaniSpacing.sm)) {
            DropdownField(label = "Genre", options = Genre.entries.toList(), selected = genre, onSelected = { genre = it }, modifier = Modifier.weight(1f))
            DropdownField(label = "Language", options = listOf(AppLanguage.TELUGU, AppLanguage.HINDI, AppLanguage.ENGLISH), selected = language, onSelected = { language = it }, modifier = Modifier.weight(1f))
        }

        UploadField(label = "Total Chapters", value = totalChapters, onValueChange = { totalChapters = it }, maxChars = 3)

        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(KahaniRadius.card))
                    .background(KahaniColors.Maroon800)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (coverUri != null) {
                    Text("Cover Selected ✅", color = KahaniColors.Saffron, fontWeight = FontWeight.Bold)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🖼️", fontSize = 32.sp)
                        Text("Select Cover Art", style = KahaniType.Micro, color = KahaniColors.TextMuted)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("Recommended ratio 3:4 (Portrait). Max 1MB.", style = KahaniType.Micro, color = KahaniColors.TextMuted)
        }

        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(KahaniRadius.card))
                    .background(KahaniColors.Maroon800)
                    .clickable { videoLauncher.launch("video/*") },
                contentAlignment = Alignment.Center
            ) {
                if (videoUri != null) {
                    Text("Video Selected ✅", color = KahaniColors.Saffron, fontWeight = FontWeight.Bold)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎬", fontSize = 28.sp)
                        Text("Add Short Video (Optional)", style = KahaniType.Micro, color = KahaniColors.TextMuted)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("MP4 format for story highlight.", style = KahaniType.Micro, color = KahaniColors.TextMuted)
        }

        PrimaryButton(
            text = if (isUploading) "Creating Story..." else "Create Story & Add Chapters",
            onClick = {
                coroutineScope.launch {
                    isUploading = true
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val storage = FirebaseStorage.getInstance()
                        val user = FirebaseAuth.getInstance().currentUser
                        val docId = initialId ?: title.lowercase().trim().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
                        
                        var coverUrl = ""
                        coverUri?.let { uri ->
                            val compressedData = compressImage(context, uri)
                            val ref = storage.reference.child("stories/$docId/cover.webp")
                            ref.putBytes(compressedData).await()
                            coverUrl = ref.downloadUrl.await().toString()
                        }

                        var videoUrl = ""
                        videoUri?.let { uri ->
                            val ref = storage.reference.child("stories/$docId/short.mp4")
                            ref.putFile(uri).await()
                            videoUrl = ref.downloadUrl.await().toString()
                        }

                        val data = mutableMapOf(
                            "title" to title,
                            "synopsis" to synopsis,
                            "genre" to genre.name,
                            "language" to language.code,
                            "totalChapters" to (totalChapters.toIntOrNull() ?: 1),
                            "status" to "ONGOING",
                            "publishStatus" to "PENDING",
                            "uploaderId" to user?.uid,
                            "uploaderName" to (user?.displayName ?: "User"),
                            "createdAt" to com.google.firebase.Timestamp.now()
                        )
                        if (coverUrl.isNotEmpty()) data["coverUrl"] = coverUrl
                        if (videoUrl.isNotEmpty()) data["videoUrl"] = videoUrl
                        
                        db.collection("series").document(docId).set(data, SetOptions.merge()).await()
                        onSuccess(docId, totalChapters.toIntOrNull() ?: 1)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isUploading = false
                    }
                }
            },
            enabled = !isUploading && title.isNotBlank() && (coverUri != null || initialId != null),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ChapterUploadForm(storyId: String, chapterNum: Int, editingId: String? = null, onSuccess: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var title by remember(chapterNum, editingId) { mutableStateOf("") }
    var textContent by remember(chapterNum, editingId) { mutableStateOf("") }
    var audioUri by remember(chapterNum, editingId) { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    LaunchedEffect(editingId) {
        if (editingId != null) {
            val doc = FirebaseFirestore.getInstance().collection("series").document(storyId).collection("chapters").document(editingId).get().await()
            title = doc.getString("title") ?: ""
            textContent = doc.getString("textContent") ?: ""
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { audioUri = it }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(KahaniSpacing.md),
        verticalArrangement = Arrangement.spacedBy(KahaniSpacing.md)
    ) {
        Text(if (editingId != null) "Edit Chapter $chapterNum" else "Step 2: Upload Chapter $chapterNum", style = KahaniType.SectionLabel, color = KahaniColors.TextPrimary)
        
        UploadField(label = "Chapter Title", value = title, onValueChange = { title = it }, maxChars = 100)
        UploadField(label = "Story Content", value = textContent, onValueChange = { textContent = it }, isMultiline = true, maxChars = 10000)

        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(KahaniRadius.card))
                    .background(KahaniColors.Maroon800)
                    .clickable { launcher.launch("audio/*") },
                contentAlignment = Alignment.Center
            ) {
                if (audioUri != null) {
                    Text("Audio File Selected ✅", color = KahaniColors.Saffron, fontWeight = FontWeight.Bold)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎧", fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text("Select Audio (Optional)", style = KahaniType.Micro, color = KahaniColors.TextMuted)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text("MP3 files are recommended for faster streaming.", style = KahaniType.Micro, color = KahaniColors.TextMuted)
        }

        PrimaryButton(
            text = if (isUploading) "Uploading Chapter..." else if (editingId != null) "Save Changes" else "Upload & Continue",
            onClick = {
                coroutineScope.launch {
                    isUploading = true
                    try {
                        val db = FirebaseFirestore.getInstance()
                        val storage = FirebaseStorage.getInstance()
                        
                        var audioUrl = ""
                        var duration = 0
                        audioUri?.let { uri ->
                            val ref = storage.reference.child("stories/$storyId/audio/ch$chapterNum.mp3")
                            ref.putFile(uri).await()
                            audioUrl = ref.downloadUrl.await().toString()
                            
                            try {
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(context, uri)
                                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                duration = (time?.toLong() ?: 0L).toInt() / 1000
                                retriever.release()
                            } catch (e: Exception) { duration = 300 }
                        }

                        val data = mutableMapOf(
                            "chapterNumber" to chapterNum,
                            "title" to title,
                            "textContent" to textContent,
                            "isFreePreview" to (chapterNum <= 3),
                            "unlockCost" to (if (chapterNum <= 3) 0 else 10),
                            "wordCount" to textContent.split("\\s+".toRegex()).size,
                            "lastUpdated" to com.google.firebase.Timestamp.now()
                        )
                        if (audioUrl.isNotEmpty()) {
                            data["audioUrl"] = audioUrl
                            data["durationSeconds"] = duration
                        }
                        
                        val docId = editingId ?: "ch$chapterNum"
                        db.collection("series").document(storyId).collection("chapters")
                            .document(docId).set(data, SetOptions.merge()).await()
                        
                        onSuccess()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    } finally {
                        isUploading = false
                    }
                }
            },
            enabled = !isUploading && title.isNotBlank() && textContent.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun UploadSuccessScreen(onReset: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(KahaniSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("✨", fontSize = 64.sp)
        Spacer(Modifier.height(KahaniSpacing.md))
        Text("Story Uploaded Successfully!", style = KahaniType.ChapterTitle, color = KahaniColors.TextPrimary)
        Spacer(Modifier.height(KahaniSpacing.xs))
        Text("Your story is now live in the catalog.", style = KahaniType.UiBody, color = KahaniColors.TextMuted)
        
        Spacer(Modifier.height(KahaniSpacing.xxl))
        PrimaryButton(text = "Go to My Stories", onClick = onReset, modifier = Modifier.fillMaxWidth())
    }
}

private suspend fun compressImage(context: android.content.Context, uri: Uri): ByteArray = withContext(Dispatchers.IO) {
    val inputStream = context.contentResolver.openInputStream(uri)
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    val maxDimension = 1024
    val scale = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
        val widthScale = maxDimension.toFloat() / originalBitmap.width
        val heightScale = maxDimension.toFloat() / originalBitmap.height
        minOf(widthScale, heightScale)
    } else 1f
    val scaledBitmap = if (scale < 1f) {
        Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt(), true)
    } else originalBitmap
    val outputStream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)
    outputStream.toByteArray()
}

@Composable
private fun UploadField(label: String, value: String, onValueChange: (String) -> Unit, isMultiline: Boolean = false, maxChars: Int = 10000) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = KahaniType.MicroBold, color = KahaniColors.Saffron)
            if (isMultiline) {
                Text("${value.length}/$maxChars", style = KahaniType.Micro, color = KahaniColors.TextMuted)
            }
        }
        Spacer(Modifier.height(4.dp))
        TextField(
            value = value,
            onValueChange = { if (it.length <= maxChars) onValueChange(it) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = KahaniColors.Maroon800,
                unfocusedContainerColor = KahaniColors.Maroon800,
                focusedTextColor = KahaniColors.TextPrimary,
                unfocusedTextColor = KahaniColors.TextPrimary,
                focusedIndicatorColor = KahaniColors.Saffron,
                unfocusedIndicatorColor = Color.Transparent
            ),
            minLines = if (isMultiline) 8 else 1,
            maxLines = if (isMultiline) 25 else 1
        )
    }
}

@Composable
private fun StatMini(label: String, value: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$label: ", style = KahaniType.Micro, color = KahaniColors.TextMuted)
        Text("$value", style = KahaniType.MicroBold, color = KahaniColors.TextPrimary)
    }
}

@Composable
private fun <T> DropdownField(label: String, options: List<T>, selected: T, onSelected: (T) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = KahaniType.MicroBold, color = KahaniColors.Saffron)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.fillMaxWidth().height(56.dp).background(KahaniColors.Maroon800, RoundedCornerShape(4.dp)).clickable { expanded = true }.padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(selected.toString(), color = KahaniColors.TextPrimary)
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(KahaniColors.Maroon800)) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option.toString(), color = KahaniColors.TextPrimary) }, onClick = { onSelected(option); expanded = false })
                }
            }
        }
    }
}
