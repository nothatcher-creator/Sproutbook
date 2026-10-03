package com.nothatcher.sproutbook.features.memories

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.core.BranchPoint

/** Static, original woodland artwork. Memory centers always come from TreeLayout. */
internal data class MemoryTreePalette(
    val canopy: Color,
    val foliage: Color,
    val light: Color,
    val bark: Color,
    val barkShade: Color,
    val barkLight: Color,
    val ground: Color,
    val leafOutline: Color,
    val night: Boolean = false,
)

internal fun memoryTreePalette(style: String, dark: Boolean): MemoryTreePalette =
    when (style) {
        "Autumn" -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF956F42 else 0xFFC6A36A),
            foliage = Color(if (dark) 0xFFB88C52 else 0xFFD5B17B),
            light = Color(0xFFF2D8A1),
            bark = Color(if (dark) 0xFFB99870 else 0xFF967247),
            barkShade = Color(if (dark) 0xFF75604C else 0xFF654E36),
            barkLight = Color(if (dark) 0xFFE3C399 else 0xFFC5A479),
            ground = Color(if (dark) 0xFF93825A else 0xFFAB996D),
            leafOutline = Color(0xFF624D35),
        )
        "Night" -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF3D6664 else 0xFF719794),
            foliage = Color(if (dark) 0xFF638A7C else 0xFF9AB6A5),
            light = Color(0xFFD6E8CE),
            bark = Color(if (dark) 0xFFABB7A6 else 0xFF788D7A),
            barkShade = Color(if (dark) 0xFF647A71 else 0xFF4E695E),
            barkLight = Color(if (dark) 0xFFD6DFCA else 0xFFA8BCA4),
            ground = Color(if (dark) 0xFF617F70 else 0xFF91A993),
            leafOutline = Color(0xFF355547),
            night = true,
        )
        else -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF527344 else 0xFF87A06B),
            foliage = Color(if (dark) 0xFF769354 else 0xFFA5BB80),
            light = Color(0xFFD5E3AF),
            bark = Color(if (dark) 0xFFB7A07C else 0xFF947B54),
            barkShade = Color(if (dark) 0xFF756751 else 0xFF67573B),
            barkLight = Color(if (dark) 0xFFDDCBA4 else 0xFFC1AE82),
            ground = Color(if (dark) 0xFF6D8756 else 0xFF96AB78),
            leafOutline = Color(0xFF3C5735),
        )
    }

private data class CrownWash(val x: Float, val y: Float, val width: Float, val height: Float, val tilt: Float)

private val crownWashes = listOf(
    CrownWash(.30f, .27f, .22f, .18f, -16f),
    CrownWash(.51f, .23f, .23f, .21f, 4f),
    CrownWash(.72f, .31f, .21f, .19f, 14f),
    CrownWash(.20f, .47f, .17f, .20f, -20f),
    CrownWash(.43f, .46f, .25f, .23f, -7f),
    CrownWash(.69f, .49f, .24f, .21f, 12f),
    CrownWash(.35f, .64f, .21f, .14f, -10f),
    CrownWash(.67f, .65f, .21f, .13f, 14f),
)

private val limbs = listOf(
    Triple(Offset(.5f, .76f), Offset(.32f, .65f), listOf(17, 18, 23, 24)),
    Triple(Offset(.5f, .74f), Offset(.68f, .63f), listOf(21, 22, 25, 26, 31)),
    Triple(Offset(.5f, .60f), Offset(.28f, .44f), listOf(5, 6, 11, 12, 30)),
    Triple(Offset(.51f, .59f), Offset(.74f, .42f), listOf(9, 10, 15, 16, 29)),
    Triple(Offset(.5f, .46f), Offset(.35f, .25f), listOf(0, 1, 7, 27)),
    Triple(Offset(.5f, .43f), Offset(.64f, .25f), listOf(3, 4, 8, 28)),
)

internal fun DrawScope.drawMemoryTreeScenery(points: List<BranchPoint>, palette: MemoryTreePalette) {
    val w = size.width
    val h = size.height
    // Large irregular washes give the crown depth without suggesting extra tappable leaves.
    crownWashes.forEachIndexed { i, wash ->
        val center = Offset(w * wash.x, h * wash.y)
        val rx = w * wash.width
        val ry = h * wash.height
        val crown = Path().apply {
            moveTo(center.x - rx, center.y)
            cubicTo(center.x - rx * 1.06f, center.y - ry * .36f,
                center.x - rx * .85f, center.y - ry * .65f,
                center.x - rx * .57f, center.y - ry * .64f)
            cubicTo(center.x - rx * .59f, center.y - ry * 1.05f,
                center.x - rx * .13f, center.y - ry * 1.11f,
                center.x + rx * .12f, center.y - ry * .82f)
            cubicTo(center.x + rx * .43f, center.y - ry * 1.03f,
                center.x + rx * .84f, center.y - ry * .74f,
                center.x + rx * .83f, center.y - ry * .39f)
            cubicTo(center.x + rx * 1.12f, center.y - ry * .09f,
                center.x + rx, center.y + ry * .42f,
                center.x + rx * .68f, center.y + ry * .53f)
            cubicTo(center.x + rx * .60f, center.y + ry * .97f,
                center.x + rx * .11f, center.y + ry,
                center.x - rx * .16f, center.y + ry * .76f)
            cubicTo(center.x - rx * .43f, center.y + ry * .93f,
                center.x - rx * .91f, center.y + ry * .63f,
                center.x - rx * .83f, center.y + ry * .30f)
            cubicTo(center.x - rx * .98f, center.y + ry * .25f,
                center.x - rx * 1.03f, center.y + ry * .11f,
                center.x - rx, center.y)
            close()
        }
        rotate(wash.tilt, center) {
            drawPath(crown, Brush.radialGradient(
                colors = listOf(palette.foliage.copy(alpha = .23f), palette.canopy.copy(alpha = .11f)),
                center = center - Offset(rx * .25f, ry * .30f),
                radius = rx * 1.35f,
            ))
            if (i < 3) drawPath(crown, Brush.radialGradient(
                colors = listOf(palette.light.copy(alpha = .10f), Color.Transparent),
                center = center - Offset(rx * .20f, ry * .50f),
                radius = rx * .9f,
            ))
        }
    }

    if (palette.night) {
        val moon = Offset(w * .88f, h * .10f)
        drawCircle(Brush.radialGradient(listOf(palette.light.copy(alpha = .16f), Color.Transparent),
            center = moon, radius = 23.dp.toPx()), 23.dp.toPx(), moon)
        drawCircle(palette.light.copy(alpha = .85f), 7.dp.toPx(), moon)
        listOf(Offset(.11f, .13f), Offset(.77f, .06f), Offset(.92f, .25f)).forEach {
            drawCircle(palette.light.copy(alpha = .6f), 1.dp.toPx(), Offset(w * it.x, h * it.y))
        }
    }

    drawOval(palette.barkShade.copy(alpha = .10f),
        Offset(w * .23f, h * .93f), Size(w * .54f, h * .048f))
    val ground = Path().apply {
        moveTo(w * .16f, h * .96f)
        cubicTo(w * .29f, h * .90f, w * .37f, h * .93f, w * .49f, h * .92f)
        cubicTo(w * .64f, h * .91f, w * .78f, h * .94f, w * .84f, h * .96f)
        cubicTo(w * .67f, h * .985f, w * .35f, h * .99f, w * .16f, h * .96f)
        close()
    }
    drawPath(ground, Brush.verticalGradient(listOf(palette.ground.copy(alpha = .30f),
        palette.ground.copy(alpha = .06f)), startY = h * .90f, endY = h * .99f))

    val barkBrush = Brush.horizontalGradient(
        listOf(palette.barkShade, palette.bark, palette.barkLight, palette.bark),
        startX = w * .47f, endX = w * .54f,
    )
    val trunk = Path().apply {
        moveTo(w * .49f, h * .17f)
        cubicTo(w * .53f, h * .36f, w * .49f, h * .45f, w * .51f, h * .57f)
        cubicTo(w * .53f, h * .70f, w * .495f, h * .80f, w * .53f, h * .91f)
        cubicTo(w * .55f, h * .943f, w * .60f, h * .94f, w * .65f, h * .95f)
        cubicTo(w * .60f, h * .968f, w * .54f, h * .951f, w * .50f, h * .966f)
        cubicTo(w * .46f, h * .953f, w * .40f, h * .97f, w * .35f, h * .958f)
        cubicTo(w * .42f, h * .943f, w * .46f, h * .93f, w * .475f, h * .90f)
        cubicTo(w * .49f, h * .79f, w * .46f, h * .72f, w * .478f, h * .59f)
        cubicTo(w * .50f, h * .44f, w * .49f, h * .32f, w * .475f, h * .20f)
        quadraticTo(w * .48f, h * .17f, w * .49f, h * .17f)
        close()
    }
    drawPath(trunk, barkBrush)

    // The original six limbs and their twigs end at the exact saved branch positions.
    limbs.forEach { (base, tip, slots) ->
        val limb = Path().apply {
            moveTo(w * base.x, h * base.y)
            cubicTo(w * base.x, h * (base.y - .10f), w * tip.x, h * (tip.y + .10f),
                w * tip.x, h * tip.y)
        }
        drawPath(limb, palette.barkShade.copy(alpha = .75f), style = Stroke(5.5.dp.toPx(), cap = StrokeCap.Round))
        drawPath(limb, palette.bark, style = Stroke(3.8.dp.toPx(), cap = StrokeCap.Round))
        drawPath(limb, palette.barkLight.copy(alpha = .45f), style = Stroke(1.dp.toPx(), cap = StrokeCap.Round))
        slots.forEach { i ->
            val a = points[i]
            val twig = Path().apply {
                moveTo(w * tip.x, h * tip.y)
                quadraticTo(w * a.x, h * tip.y, w * a.x, h * a.y)
            }
            drawPath(twig, palette.bark.copy(alpha = .85f), style = Stroke(1.7.dp.toPx(), cap = StrokeCap.Round))
        }
    }
    listOf(2, 13, 14, 19, 20).forEach { i ->
        val a = points[i]
        val twig = Path().apply {
            moveTo(w * .5f, h * (a.y + .09f))
            quadraticTo(w * .5f, h * a.y, w * a.x, h * a.y)
        }
        drawPath(twig, palette.bark, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    }

    val barkGrain = Path().apply {
        moveTo(w * .495f, h * .68f)
        cubicTo(w * .485f, h * .78f, w * .507f, h * .84f, w * .499f, h * .91f)
        moveTo(w * .50f, h * .51f)
        quadraticTo(w * .508f, h * .59f, w * .495f, h * .64f)
    }
    drawPath(barkGrain, palette.barkShade.copy(alpha = .6f), style = Stroke(.8.dp.toPx(), cap = StrokeCap.Round))
    val knot = Offset(w * .495f, h * .81f)
    drawOval(palette.barkShade.copy(alpha = .4f), knot - Offset(1.4.dp.toPx(), 3.dp.toPx()),
        Size(2.8.dp.toPx(), 6.dp.toPx()), style = Stroke(.7.dp.toPx()))

    // A few quiet tufts ground the tree, kept well below every interactive branch.
    listOf(.28f, .34f, .65f, .72f).forEachIndexed { i, x ->
        val base = Offset(w * x, h * (.955f + if (i % 2 == 0) .007f else 0f))
        val grass = Path().apply {
            moveTo(base.x - 4.dp.toPx(), base.y)
            quadraticTo(base.x - 4.dp.toPx(), base.y - 4.dp.toPx(), base.x - 7.dp.toPx(), base.y - 7.dp.toPx())
            moveTo(base.x, base.y)
            quadraticTo(base.x - 1.dp.toPx(), base.y - 5.dp.toPx(), base.x + 2.dp.toPx(), base.y - 9.dp.toPx())
            moveTo(base.x + 3.dp.toPx(), base.y)
            quadraticTo(base.x + 4.dp.toPx(), base.y - 4.dp.toPx(), base.x + 7.dp.toPx(), base.y - 5.dp.toPx())
        }
        drawPath(grass, palette.ground.copy(alpha = .7f), style = Stroke(1.1.dp.toPx(), cap = StrokeCap.Round))
    }
}
