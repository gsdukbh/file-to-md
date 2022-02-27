// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.


import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.concurrent.ScheduledThreadPoolExecutor
import javax.swing.JFileChooser


fun isDirectory(file: File, list: ArrayList<FileEntity>) {
    if (file.isDirectory) {
        val files = file.listFiles()
        for (i in files) {
            isDirectory(i, list)
        }
    } else {
        val fileEntity = FileEntity(file.name, file.canonicalPath);
        list.add(fileEntity)
    }
}

class FileEntity(var name: String, var path: String)

var pool = ScheduledThreadPoolExecutor(5)
fun saveFile(filePath: File, onSuccess: () -> Unit) {
    pool.execute {
        val mdFile = FileOutputStream(filePath.canonicalPath + "/生成的目录索引.md")
        val list = ArrayList<FileEntity>()
        isDirectory(filePath, list)
        for (i in list) {
            if (i.name.endsWith(".DS_Store")) {
                continue
            }
            val info = "[" + i.name + "](" + i.path.removePrefix(filePath.canonicalPath + "/") + ")\n"
            mdFile.write(info.toByteArray())
        }
        mdFile.close()
        onSuccess()
    }
}


@Composable
@Preview
fun app(a: FrameWindowScope) {
    val scope = rememberCoroutineScope()
    var isDialogOpen by remember { mutableStateOf(false) }
    var path by remember { mutableStateOf<Path?>(null) }
    var isSaveDialogOpen by remember { mutableStateOf(false) }
    MaterialTheme {
        Column {
            Row() {
                Button(onClick = {
                    isDialogOpen = true
                }) {
                    Text("Open")
                }
                Button(modifier = Modifier.padding(start = 20.dp), onClick = { isSaveDialogOpen = true }) {
                    Text("Save")
                }
            }
            if (isDialogOpen) {

//                val s = FileChooser()

                val s = JFileChooser()
                s.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                val res = s.showOpenDialog(null)
                if (res == JFileChooser.APPROVE_OPTION) {
                    val file = s.selectedFile
                    path = file.toPath()
                    isDialogOpen = false
                }
//                a.FileDialog(
//                    title = "Open",
//                    isLoad = true,
//                    onResult = {
//                        isDialogOpen = false
//                        path = it
//                    }
//                )
            }

            if (path != null) {
                val c = path!!.parent
//                val filePath = File(c.toString())
                val filePath = File(path.toString())
                Text("读取的文件目录 Path: ${filePath.canonicalPath}")
                Text("生成的文件 ${filePath.canonicalPath + "/生成的目录索引.md"}")
                var isSaveSuccess by remember { mutableStateOf(false) }
                if (isSaveDialogOpen) {
                    saveFile(filePath) {
                        isSaveSuccess = true
                    }
                    if (isSaveSuccess) {
                        Dialog(
                            title = "保存成功",
                            onCloseRequest = { isSaveDialogOpen = false }
                        ) {
                            Column {
                                Text("生成的目录索引.md 已经保存到 ${filePath.canonicalPath}")
                                Button(onClick = {
                                    Desktop.getDesktop().open(File(filePath.canonicalPath + "/生成的目录索引.md"))
                                }) {
                                    Text("打开 生成的目录索引.md")
                                }
                            }
                        }
                    }

                }
            }
            Text(text = "werls.top ")
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "file to markdown") {
        app(this)
    }
}
