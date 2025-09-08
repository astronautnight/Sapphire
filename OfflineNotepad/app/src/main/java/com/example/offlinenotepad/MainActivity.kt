package com.example.offlinenotepad

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SapphireApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SapphireApp() {
    val ctx = LocalContext.current
    val baseDir = remember { ctx.getExternalFilesDir(null) ?: ctx.filesDir }

    var drawerOpen by remember { mutableStateOf(false) }
    var files by remember { mutableStateOf(listFiles(baseDir)) }
    var currentFile by remember { mutableStateOf<File?>(null) }
    var text by remember { mutableStateOf("") }
    var showSaveAs by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("untitled.txt") }
    var showOverflow by remember { mutableStateOf(false) }

    fun loadFile(f: File) {
        currentFile = f
        text = runCatching { f.readText() }.getOrElse { "" }
    }

    fun save(to: File? = currentFile) {
        val target = to ?: run { showSaveAs = true; return }
        runCatching { target.writeText(text) }.onSuccess {
            if (!files.contains(target)) files = listFiles(baseDir)
            currentFile = target
        }
    }

    fun openInBrowser() {
        val f = currentFile ?: return
        val name = f.name.lowercase()
        if (!(name.endsWith(".html") || name.endsWith(".htm"))) return
        val uri: Uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f)
        val i = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "text/html")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(Intent.createChooser(i, "Open with"))
    }

    // UI
    ModalNavigationDrawer(
        drawerState = rememberDrawerState(if (drawerOpen) DrawerValue.Open else DrawerValue.Closed),
        drawerContent = {
            // Translucent sidebar
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(280.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp, start = 12.dp, end = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Files", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    files.forEach { f ->
                        Button(
                            onClick = {
                                loadFile(f)
                                drawerOpen = false
                            },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Text(f.name)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            newName = "untitled.txt"
                            showSaveAs = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("New file") }
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentFile?.name ?: "Sapphire") },
                    navigationIcon = {
                        IconButton(onClick = { drawerOpen = !drawerOpen }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Sidebar",
                                tint = Color(0xFFE53935) // red icon
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { save() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                        // Overflow only when current file is .html
                        val isHtml = currentFile?.name?.lowercase()?.let { it.endsWith(".html") || it.endsWith(".htm") } == true
                        if (isHtml) {
                            IconButton(onClick = { openInBrowser() }) {
                                Icon(Icons.Outlined.OpenInBrowser, contentDescription = "Open in Browser")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            // Editor
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                placeholder = { Text("Text....") },
                textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                minLines = 20
            )
        }
    }

    // Save As dialog
    if (showSaveAs) {
        Dialog(onDismissRequest = { showSaveAs = false }) {
            Surface(tonalElevation = 4.dp, shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(20.dp)) {
                    Text("Save as", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    TextField(value = newName, onValueChange = { newName = it }, singleLine = true)
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showSaveAs = false }) { Text("Cancel") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = {
                            val f = File(baseDir, newName.trim())
                            save(f)
                            files = listFiles(baseDir)
                            showSaveAs = false
                        }) { Text("Save") }
                    }
                }
            }
        }
    }
}

private fun listFiles(baseDir: File): List<File> =
    baseDir.listFiles()?.filter { it.isFile }?.sortedBy { it.name.lowercase() } ?: emptyList()
