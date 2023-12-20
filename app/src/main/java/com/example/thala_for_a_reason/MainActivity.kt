@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.example.thala_for_a_reason

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.example.thala_for_a_reason.ui.theme.ThalaForAReasonAppTheme
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ThalaForAReasonAppTheme {
                val viewModel = viewModel<MainViewModel>()
                val context = LocalContext.current
                val uriHandler = LocalUriHandler.current

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Scaffold(topBar = {
                        TopAppBar(colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.primaryContainer,
                        ), title = {
                            Text(getString(R.string.thala_for_a_reason_emoji))
                        })
                    }) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 8.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            UserPromptSection(
                                context = context,
                                viewModel = viewModel,
                                modifier = Modifier
                                    .padding(it)
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            )

                            ThalaReasonOutputContainer(
                                context = context, viewModel = viewModel
                            )

                            Spacer(modifier = Modifier.weight(1f, fill = true))

                            Footer(uriHandler = uriHandler)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserPromptSection(
    context: Context, viewModel: MainViewModel, modifier: Modifier = Modifier
) {
    var text by rememberSaveable { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text(text = context.getString(R.string.label_enter_prompt)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 4.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (text.trim().isEmpty()) {
                    Toast.makeText(
                        context, context.getString(R.string.validation), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    keyboardController?.hide()
                    viewModel.generateGeminiContent(text.trim())
                }
            }, modifier = Modifier
        ) {
            Text(
                text = context.getString(R.string.why_thala_for_a_reason),
                modifier = Modifier.padding(all = 8.dp)
            )
        }
    }
}

@Composable
fun ThalaReasonOutputContainer(
    context: Context, viewModel: MainViewModel, modifier: Modifier = Modifier
) {
    val content by viewModel.content.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (!isLoading && content.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = content, textAlign = TextAlign.Center, modifier = Modifier
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedThalaText(context = context, modifier = Modifier)

                Spacer(modifier = Modifier.height(16.dp))

                GifImage(
                    context = context, modifier = Modifier.clip(shape = RoundedCornerShape(8.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ShareButton(context = context, prompt = content)
        }
    }
}

@Composable
fun AnimatedThalaText(
    context: Context, modifier: Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1.0f, animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .size(width = 300.dp, height = 50.dp)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = context.getString(R.string.app_name),
            style = TextStyle(
                fontSize = 24.sp * scale, letterSpacing = 2.sp, fontWeight = FontWeight.Bold
            ),
        )
    }
}


@Composable
fun GifImage(
    context: Context,
    modifier: Modifier = Modifier,
) {
    val imageLoader = ImageLoader.Builder(context).components {
        if (SDK_INT >= 28) {
            add(ImageDecoderDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
    }.build()

    Image(
        painter = rememberAsyncImagePainter(R.raw.ms_dhoni, imageLoader),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.size(200.dp)
    )
}

@Composable
fun ShareButton(
    context: Context, prompt: String
) {
    Button(
        onClick = {
            shareVideo(
                context, "$prompt \n\n${context.getString(R.string.thala_for_a_reason_emoji)}⃣"
            )
        }, modifier = Modifier.padding(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = context.getString(R.string.share),
            tint = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = context.getString(R.string.share), modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun Footer(uriHandler: UriHandler) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Develop by Satish",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.width(12.dp))

        Image(painter = painterResource(id = R.drawable.img_twitter),
            contentDescription = null,
            modifier = Modifier
                .size(30.dp)
                .clickable {
                    uriHandler.openUri("https://twitter.com/satis_fy_")
                })
    }
}


private fun shareVideo(context: Context, text: String) {

    val file = copyRawVideoToCache(context, R.raw.ms_dhoni) ?: File("")
    val videoUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "video/mp4"
    shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri)
    shareIntent.putExtra(Intent.EXTRA_TEXT, text)
    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

    context.startActivity(shareIntent)
}

private fun copyRawVideoToCache(context: Context, rawVideoResourceId: Int): File? {
    val cacheDir = context.cacheDir
    val videoFile = File(cacheDir, "ms_dhoni.gif")

    try {
        val inputStream: InputStream = context.resources.openRawResource(rawVideoResourceId)
        val outputStream = FileOutputStream(videoFile)
        val buffer = ByteArray(1024)
        var read: Int

        while (inputStream.read(buffer).also { read = it } != -1) {
            outputStream.write(buffer, 0, read)
        }

        inputStream.close()
        outputStream.flush()
        outputStream.close()

        return videoFile
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return null
}