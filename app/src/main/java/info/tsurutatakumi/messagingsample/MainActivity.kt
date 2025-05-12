package info.tsurutatakumi.messagingsample

import android.os.Bundle
import android.content.res.Configuration
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import info.tsurutatakumi.messagingsample.ui.theme.TutorialTheme
import jp.shiguredo.sora.sdk.channel.SoraMediaChannel
import jp.shiguredo.sora.sdk.channel.option.SoraChannelRole
import jp.shiguredo.sora.sdk.channel.option.SoraMediaOption
import jp.shiguredo.sora.sdk.error.SoraErrorReason
import jp.shiguredo.sora.sdk.util.SoraLogger
import java.nio.ByteBuffer

class MainActivity : ComponentActivity() {

    companion object{
       const val TAG = "MainActivity"
    }
    private var mediaChannel: SoraMediaChannel? = null

    private val _messages = mutableStateListOf<Message>()
    val messages: List<Message> get() = _messages

    private val channelListener = object: SoraMediaChannel.Listener {
        override fun onConnect(mediaChannel: SoraMediaChannel) {
            Log.i(TAG, "Connected to Sora")
        }

        override fun onClose(mediaChannel: SoraMediaChannel) {
            Log.i(TAG, "Disconnected from Sora")
            close()
        }

        override fun onError(mediaChannel: SoraMediaChannel, reason: SoraErrorReason) {
            Log.e(TAG, "Error occurred: $reason")
            close()
        }

        override fun onError(
            mediaChannel: SoraMediaChannel,
            reason: SoraErrorReason,
            message: String
        ) {
            Log.e(TAG, "Error occurred: $reason, message: $message")
            close()
        }

        override fun onDataChannel(
            mediaChannel: SoraMediaChannel,
            dataChannels: List<Map<String, Any>>?
        ) {
            Log.d(TAG, "Data channel opened: $dataChannels")
        }

        override fun onDataChannelMessage(
            mediaChannel: SoraMediaChannel,
            label: String,
            data: ByteBuffer
        ) {
            val message = mediaChannel.dataToString(data)
            Log.d(TAG, "Data channel message received: $label, message: $message")
            // messages に追加して UI を更新する
            // いったん zztkm が送ってきたことにする
            _messages.add(Message("zztkm", message))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Sora SDK のログを有効化
        SoraLogger.enabled = true

        //enableEdgeToEdge()
        setContent {
            TutorialTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()     // 画面全体を使う
                        .padding(16.dp)    // 全体にマージン
                ) {
                    Conversation(
                        messages,
                        modifier = Modifier
                            .weight(1f)    // 残り高をすべて占有
                            .fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))  // 会話とボタンの間隔
                    ConnectButton(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        connect()
                    }
                }
            }
        }
    }

    private fun connect() {
        Log.i(TAG, "Connecting to Sora")
        val option = SoraMediaOption().apply {
            // Sora Android SDK 2025.1.0 まではこのオプションを指定する必要があるが
            // 2025.2.0 以降はデフォルトでマルチストリームが有効になる状態になるので
            // このオプションを指定する必要はない
            enableMultistream()
        }
        option.role = SoraChannelRole.SENDRECV
        val dataChannels = listOf(
            mapOf(
                "label" to "#msg",
                "direction" to "sendrecv"
            ),
        )
        mediaChannel = SoraMediaChannel(
            context = this,
            signalingEndpoint = BuildConfig.SIGNALING_ENDPOINT,
            channelId = BuildConfig.CHANNEL_ID,
            signalingMetadata = Gson().fromJson(BuildConfig.SIGNALING_METADATA, Map::class.java),
            mediaOption = option,
            listener = channelListener,
            // DataChannel のリアルタイムメッセージングのみで接続する
            dataChannelSignaling = true,
            dataChannels = dataChannels,
        )

        mediaChannel!!.connect()
    }

    private fun close() {
        Log.i(TAG, "Closing Sora connection")
        mediaChannel?.disconnect()
        mediaChannel = null
    }
}

@Composable
fun ConnectButton(modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = "Connect",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Preview
@Composable
fun PreviewConnectButton() {
    TutorialTheme {
        ConnectButton(modifier = Modifier, onClick = {})
    }
}

data class Message(
    val author: String,
    val body: String,
)

@Composable
fun MessageCard(msg: Message) {
    Row (modifier = Modifier.padding(all = 8.dp)){
        Image(
            painter = painterResource(R.drawable.zztkm),
            contentDescription = "Content profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        Spacer(modifier = Modifier.width(8.dp))

        var isExpanded by remember { mutableStateOf(false) }
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
        )

        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                color = surfaceColor,
                modifier = Modifier.animateContentSize().padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style =  MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview(name = "Light mode")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark mode"
)
@Composable
fun PreviewMessageCard() {
    TutorialTheme {
        Surface {
            MessageCard(Message("zztkm", "Hello, Jetpack Compose!"))
        }
    }
}

@Composable
fun Conversation(messages: List<Message>, modifier: Modifier) {
    val listState = rememberLazyListState()
    // メッセージリストが更新されたら一番下までスクロールする
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            // NOTE(zztkm): scrollToItem と animateScrollToItem の違いが正直わかってない...
            // animateScrollToItem のほうがスムーズなアニメーションな気がしたので使っている
            // listState.scrollToItem(messages.size - 1)
            listState.animateScrollToItem(messages.size -1)
        }
    }
    LazyColumn(
        modifier = modifier,
        state = listState
    ) {
        items(messages) { message ->
            MessageCard(message)
        }
    }
}
