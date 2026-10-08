package com.nothatcher.sproutbook.features.memories

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nothatcher.sproutbook.data.Child
import com.nothatcher.sproutbook.data.Memory
import com.nothatcher.sproutbook.services.PhotoStore
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** A broad leaf body leaves a safe reading area between its pointed tip and stem. */
private fun memoryLeafOutline(size: Size) = Path().apply {
    val w = size.width
    val h = size.height
    moveTo(w * .88f, h * .015f)
    cubicTo(w * .97f, h * .15f, w, h * .26f, w * .985f, h * .46f)
    cubicTo(w, h * .71f, w * .97f, h * .82f, w * .84f, h * .90f)
    cubicTo(w * .68f, h, w * .38f, h * .97f, w * .12f, h * .97f)
    cubicTo(w * .19f, h * .85f, w * .02f, h * .80f, w * .015f, h * .53f)
    cubicTo(0f, h * .32f, w * .045f, h * .17f, w * .18f, h * .11f)
    cubicTo(w * .36f, h * .015f, w * .62f, h * .13f, w * .88f, h * .015f)
    close()
}

private object MemoryLeafShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density) =
        Outline.Generic(memoryLeafOutline(size))
}

@Composable
internal fun MemoryLeafCard(
    memory: Memory,
    child: Child,
    readOnly: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val date = remember(memory.occurredOn) { LocalDate.ofEpochDay(memory.occurredOn) }
    val age = remember(child.birthday, date) {
        child.birthday?.let(LocalDate::ofEpochDay)?.takeIf { date >= it }?.let { birthday ->
            Period.between(birthday, date).let {
                val parts = listOf(it.years to "year", it.months to "month", it.days to "day")
                    .filter { (count, _) -> count > 0 }
                    .joinToString(", ") { (count, unit) -> "$count $unit${if (count == 1) "" else "s"}" }
                "Age at this moment · ${parts.ifEmpty { "Their very first day" }}"
            }
        }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        BoxWithConstraints(
            Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val leafWidth = minOf(maxWidth, 520.dp)
            val leafHeight = minOf(maxHeight, 780.dp)
            Box(
                Modifier.width(leafWidth).height(leafHeight)
                    .testTag("memory-leaf-card")
                    .semantics { paneTitle = "Memory keepsake" }
                    .shadow(10.dp, MemoryLeafShape, clip = false)
                    .drawBehind {
                        val outline = memoryLeafOutline(size)
                        drawPath(outline, Brush.linearGradient(
                            listOf(colors.primaryContainer, colors.surfaceContainerLow, colors.surface),
                            start = Offset(size.width, 0f), end = Offset(0f, size.height),
                        ))
                        drawPath(outline, colors.primary.copy(alpha = .28f), style = Stroke(1.dp.toPx()))
                        // Quiet veins live in the outer margin, away from every line of the story.
                        val veins = Path().apply {
                            moveTo(size.width * .11f, size.height * .94f)
                            cubicTo(size.width * .17f, size.height * .85f, size.width * .11f, size.height * .42f,
                                size.width * .16f, size.height * .30f)
                            moveTo(size.width * .16f, size.height * .30f)
                            quadraticTo(size.width * .22f, size.height * .15f, size.width * .79f, size.height * .075f)
                            moveTo(size.width * .13f, size.height * .55f)
                            quadraticTo(size.width * .065f, size.height * .52f, size.width * .045f, size.height * .43f)
                            moveTo(size.width * .14f, size.height * .76f)
                            quadraticTo(size.width * .075f, size.height * .72f, size.width * .055f, size.height * .64f)
                            moveTo(size.width * .14f, size.height * .85f)
                            quadraticTo(size.width * .34f, size.height * .925f, size.width * .65f, size.height * .93f)
                        }
                        drawPath(veins, colors.primary.copy(alpha = .19f),
                            style = Stroke(1.2.dp.toPx(), cap = StrokeCap.Round))
                        drawLine(colors.primary.copy(alpha = .55f),
                            Offset(size.width * .13f, size.height * .94f),
                            Offset(size.width * .085f, size.height * .99f), 2.dp.toPx(), StrokeCap.Round)
                    },
            ) {
                // Content has its own rectangular viewport. The leaf's corners never clip text,
                // photos or controls, even with a long title, larger fonts or a short window.
                Column(
                    Modifier.fillMaxSize().padding(
                        start = leafWidth * .18f, end = leafWidth * .15f,
                        top = leafHeight * .19f, bottom = leafHeight * .19f,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(
                        Modifier.weight(1f).fillMaxWidth().testTag("memory-leaf-scroll")
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("A moment to keep", style = MaterialTheme.typography.labelLarge,
                            color = colors.primary)
                        Text(memory.title, style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.semantics { heading() })
                        Text("${child.name}'s story · Chapter ${memory.chapter + 1}",
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        HorizontalDivider(color = colors.primary.copy(alpha = .18f))
                        Text(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(memory.category, style = MaterialTheme.typography.labelLarge, color = colors.primary)
                        age?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
                        if (readOnly) Text("Read-only keepsake", style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant)
                        memory.photo?.let { MemoryLeafPhoto(it, memory.title) }
                        if (memory.notes.isNotBlank()) Text(memory.notes, style = MaterialTheme.typography.bodyLarge)
                        Text("A little part of ${child.name}'s story, kept close.",
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    MemoryLeafActions(readOnly, onDismiss, onEdit)
                }
            }
        }
    }
}

@Composable
private fun MemoryLeafActions(readOnly: Boolean, onDismiss: () -> Unit, onEdit: () -> Unit) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val labelStyle = MaterialTheme.typography.labelLarge
    val padding = ButtonDefaults.ContentPadding
    val horizontalPadding = padding.calculateStartPadding(direction) + padding.calculateEndPadding(direction)
    fun buttonWidth(label: String) = maxOf(ButtonDefaults.MinWidth, with(density) {
        textMeasurer.measure(AnnotatedString(label), style = labelStyle, softWrap = false).size.width.toDp()
    } + horizontalPadding + 1.dp)
    val closeWidth = buttonWidth("Close")
    val editWidth = buttonWidth("Edit memory")
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (!readOnly && closeWidth + editWidth + 8.dp <= maxWidth) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, contentPadding = padding,
                    modifier = Modifier.width(closeWidth).heightIn(min = 48.dp)) {
                    Text("Close", style = labelStyle, textAlign = TextAlign.Center)
                }
                Button(onClick = onEdit, contentPadding = padding,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)) {
                    Text("Edit memory", style = labelStyle, textAlign = TextAlign.Center)
                }
            }
        } else {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, contentPadding = padding,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Close", style = labelStyle, textAlign = TextAlign.Center)
                }
                if (!readOnly) Button(onClick = onEdit, contentPadding = padding,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Edit memory", style = labelStyle, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun MemoryLeafPhoto(filename: String, title: String) {
    val context = LocalContext.current
    val photo by produceState<Pair<Bitmap?, Boolean>>(null to false, filename) {
        value = PhotoStore.load(context, filename) to true
    }
    val bitmap = photo.first
    if (bitmap != null) {
        Image(bitmap.asImageBitmap(), "Photo for $title", Modifier.fillMaxWidth().heightIn(max = 240.dp),
            contentScale = ContentScale.Fit)
    } else {
        Text(if (photo.second) "Photo unavailable on this device" else "Opening your photo…",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
