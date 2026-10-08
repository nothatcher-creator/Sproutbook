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
import com.nothatcher.sproutbook.core.TreeTheme
import com.nothatcher.sproutbook.core.TreeThemes
import kotlin.math.cos
import kotlin.math.sin

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
    val ornament: Color,
    val theme: TreeTheme,
) {
    val night: Boolean get() = theme == TreeTheme.NIGHT
}

internal fun memoryTreePalette(style: String, dark: Boolean): MemoryTreePalette =
    when (TreeThemes.find(style) ?: TreeThemes.default) {
        TreeTheme.AUTUMN -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF956F42 else 0xFFC6A36A),
            foliage = Color(if (dark) 0xFFB88C52 else 0xFFD5B17B),
            light = Color(0xFFF2D8A1),
            bark = Color(if (dark) 0xFFB99870 else 0xFF967247),
            barkShade = Color(if (dark) 0xFF75604C else 0xFF654E36),
            barkLight = Color(if (dark) 0xFFE3C399 else 0xFFC5A479),
            ground = Color(if (dark) 0xFF93825A else 0xFFAB996D),
            leafOutline = Color(0xFF624D35),
            ornament = Color(if (dark) 0xFFE9B578 else 0xFFAF693C),
            theme = TreeTheme.AUTUMN,
        )
        TreeTheme.NIGHT -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF3D6664 else 0xFF719794),
            foliage = Color(if (dark) 0xFF638A7C else 0xFF9AB6A5),
            light = Color(0xFFD6E8CE),
            bark = Color(if (dark) 0xFFABB7A6 else 0xFF788D7A),
            barkShade = Color(if (dark) 0xFF647A71 else 0xFF4E695E),
            barkLight = Color(if (dark) 0xFFD6DFCA else 0xFFA8BCA4),
            ground = Color(if (dark) 0xFF617F70 else 0xFF91A993),
            leafOutline = Color(0xFF355547),
            ornament = Color(0xFFD6E8CE),
            theme = TreeTheme.NIGHT,
        )
        TreeTheme.SPRING -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF497B68 else 0xFF72AC89),
            foliage = Color(if (dark) 0xFF83B789 else 0xFFA0D3A0),
            light = Color(0xFFE8F2BB),
            bark = Color(if (dark) 0xFFB7A588 else 0xFF947B60),
            barkShade = Color(if (dark) 0xFF756653 else 0xFF67533E),
            barkLight = Color(if (dark) 0xFFE3D1AE else 0xFFC7B795),
            ground = Color(if (dark) 0xFF73A078 else 0xFF8CBB84),
            leafOutline = Color(0xFF355B45),
            ornament = Color(if (dark) 0xFFF2CCA4 else 0xFFEBC09A),
            theme = TreeTheme.SPRING,
        )
        TreeTheme.BLOSSOM -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF96677D else 0xFFD4A0B5),
            foliage = Color(if (dark) 0xFFD39AAC else 0xFFE7B5C5),
            light = Color(0xFFFFE4DF),
            bark = Color(if (dark) 0xFFB59B96 else 0xFF94756F),
            barkShade = Color(if (dark) 0xFF776260 else 0xFF69534F),
            barkLight = Color(if (dark) 0xFFE3C8BB else 0xFFCCAA97),
            ground = Color(if (dark) 0xFF7F9674 else 0xFFA4B891),
            leafOutline = Color(0xFF674553),
            ornament = Color(if (dark) 0xFFF1C3CF else 0xFFF6CDDA),
            theme = TreeTheme.BLOSSOM,
        )
        TreeTheme.WINTER -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF4A7887 else 0xFF81A9B6),
            foliage = Color(if (dark) 0xFF8EB6BD else 0xFFB9D4D8),
            light = Color(0xFFEAF4F4),
            bark = Color(if (dark) 0xFFACBAB8 else 0xFF829A9A),
            barkShade = Color(if (dark) 0xFF667D80 else 0xFF567477),
            barkLight = Color(if (dark) 0xFFE0E9E4 else 0xFFBCCFC9),
            ground = Color(if (dark) 0xFFB3CCD1 else 0xFFD5E6E8),
            leafOutline = Color(0xFF375965),
            ornament = Color(if (dark) 0xFFEAF4F4 else 0xFFF8FCFD),
            theme = TreeTheme.WINTER,
        )
        TreeTheme.RAINBOW -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF777394 else 0xFFADA4C8),
            foliage = Color(if (dark) 0xFFADA1C7 else 0xFFCEC0E1),
            light = Color(0xFFF5E8CB),
            bark = Color(if (dark) 0xFFB4A0A9 else 0xFF98848C),
            barkShade = Color(if (dark) 0xFF776573 else 0xFF675566),
            barkLight = Color(if (dark) 0xFFE2CBCD else 0xFFC9ACB2),
            ground = Color(if (dark) 0xFF7F9A90 else 0xFFA5BDB0),
            leafOutline = Color(0xFF514665),
            ornament = Color(if (dark) 0xFFE9BBC9 else 0xFFDBA2BC),
            theme = TreeTheme.RAINBOW,
        )
        TreeTheme.SUMMER -> MemoryTreePalette(
            canopy = Color(if (dark) 0xFF527344 else 0xFF87A06B),
            foliage = Color(if (dark) 0xFF769354 else 0xFFA5BB80),
            light = Color(0xFFD5E3AF),
            bark = Color(if (dark) 0xFFB7A07C else 0xFF947B54),
            barkShade = Color(if (dark) 0xFF756751 else 0xFF67573B),
            barkLight = Color(if (dark) 0xFFDDCBA4 else 0xFFC1AE82),
            ground = Color(if (dark) 0xFF6D8756 else 0xFF96AB78),
            leafOutline = Color(0xFF3C5735),
            ornament = Color(0xFFF4E1AF),
            theme = TreeTheme.SUMMER,
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
    drawTreeSky(palette)
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
            center = moon, radius = artworkPx(23f)), artworkPx(23f), moon)
        drawCircle(palette.light.copy(alpha = .85f), artworkPx(7f), moon)
        listOf(Offset(.11f, .13f), Offset(.77f, .06f), Offset(.92f, .25f)).forEach {
            drawCircle(palette.light.copy(alpha = .6f), artworkPx(1f), Offset(w * it.x, h * it.y))
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
        drawPath(limb, palette.barkShade.copy(alpha = .75f), style = Stroke(artworkPx(5.5f), cap = StrokeCap.Round))
        drawPath(limb, palette.bark, style = Stroke(artworkPx(3.8f), cap = StrokeCap.Round))
        drawPath(limb, palette.barkLight.copy(alpha = .45f), style = Stroke(artworkPx(1f), cap = StrokeCap.Round))
        if (palette.theme == TreeTheme.WINTER) {
            drawPath(limb, palette.ornament.copy(alpha = .65f), style = Stroke(artworkPx(1.2f), cap = StrokeCap.Round))
        }
        slots.forEach { i ->
            val a = points[i]
            val twig = Path().apply {
                moveTo(w * tip.x, h * tip.y)
                quadraticTo(w * a.x, h * tip.y, w * a.x, h * a.y)
            }
            drawPath(twig, palette.bark.copy(alpha = .85f), style = Stroke(artworkPx(1.7f), cap = StrokeCap.Round))
        }
    }
    listOf(2, 13, 14, 19, 20).forEach { i ->
        val a = points[i]
        val twig = Path().apply {
            moveTo(w * .5f, h * (a.y + .09f))
            quadraticTo(w * .5f, h * a.y, w * a.x, h * a.y)
        }
        drawPath(twig, palette.bark, style = Stroke(artworkPx(2f), cap = StrokeCap.Round))
    }

    val barkGrain = Path().apply {
        moveTo(w * .495f, h * .68f)
        cubicTo(w * .485f, h * .78f, w * .507f, h * .84f, w * .499f, h * .91f)
        moveTo(w * .50f, h * .51f)
        quadraticTo(w * .508f, h * .59f, w * .495f, h * .64f)
    }
    drawPath(barkGrain, palette.barkShade.copy(alpha = .6f), style = Stroke(artworkPx(.8f), cap = StrokeCap.Round))
    val knot = Offset(w * .495f, h * .81f)
    drawOval(palette.barkShade.copy(alpha = .4f), knot - Offset(artworkPx(1.4f), artworkPx(3f)),
        Size(artworkPx(2.8f), artworkPx(6f)), style = Stroke(artworkPx(.7f)))

    // A few quiet tufts ground the tree, kept well below every interactive branch.
    listOf(.28f, .34f, .65f, .72f).forEachIndexed { i, x ->
        val base = Offset(w * x, h * (.955f + if (i % 2 == 0) .007f else 0f))
        val grass = Path().apply {
            moveTo(base.x - artworkPx(4f), base.y)
            quadraticTo(base.x - artworkPx(4f), base.y - artworkPx(4f), base.x - artworkPx(7f), base.y - artworkPx(7f))
            moveTo(base.x, base.y)
            quadraticTo(base.x - artworkPx(1f), base.y - artworkPx(5f), base.x + artworkPx(2f), base.y - artworkPx(9f))
            moveTo(base.x + artworkPx(3f), base.y)
            quadraticTo(base.x + artworkPx(4f), base.y - artworkPx(4f), base.x + artworkPx(7f), base.y - artworkPx(5f))
        }
        drawPath(grass, palette.ground.copy(alpha = .7f), style = Stroke(artworkPx(1.1f), cap = StrokeCap.Round))
    }
    drawTreeOrnaments(palette)
}

/** Decorations are static and stay at the edge of the crown or below its memory anchors. */
private fun DrawScope.drawTreeSky(palette: MemoryTreePalette) {
    if (palette.theme == TreeTheme.RAINBOW) {
        val bands = listOf(0xFFE4A2B0, 0xFFE8B78C, 0xFFE9D79D, 0xFFA6CDAA, 0xFF91B7CF, 0xFFB7A4D0)
        bands.forEachIndexed { i, color ->
            val inset = artworkPx(3.2f) * i
            drawArc(Color(color).copy(alpha = .62f), 180f, 180f, false,
                Offset(size.width * .12f + inset, size.height * .015f + inset),
                Size(size.width * .76f - 2 * inset, size.height * .35f - 2 * inset),
                style = Stroke(artworkPx(2.8f), cap = StrokeCap.Round))
        }
    } else if (palette.theme in listOf(TreeTheme.SUMMER, TreeTheme.SPRING)) {
        val sun = Offset(size.width * .88f, size.height * .10f)
        drawCircle(Brush.radialGradient(listOf(palette.light.copy(alpha = .20f), Color.Transparent),
            center = sun, radius = artworkPx(18f)), artworkPx(18f), sun)
        drawCircle(palette.light.copy(alpha = .8f), artworkPx(6f), sun)
    }
}

private fun DrawScope.drawTreeOrnaments(palette: MemoryTreePalette) {
    fun at(x: Float, y: Float) = Offset(size.width * x, size.height * y)
    when (palette.theme) {
        TreeTheme.SUMMER, TreeTheme.SPRING, TreeTheme.RAINBOW -> {
            val flowers = if (palette.theme == TreeTheme.SPRING) listOf(.24f, .31f, .69f, .77f)
                else listOf(.27f, .73f)
            flowers.forEachIndexed { i, x ->
                val base = at(x, .955f)
                val bloom = base - Offset(artworkPx(if (i % 2 == 0) 2f else -2f), artworkPx(9f + i % 2 * 3))
                drawLine(palette.ground.copy(alpha = .9f), base, bloom, artworkPx(1f), cap = StrokeCap.Round)
                val petals = if (palette.theme == TreeTheme.RAINBOW && i % 2 == 1) palette.light else palette.ornament
                drawWoodlandFlower(bloom, petals, palette.barkLight, artworkPx(3.2f))
                drawOval(palette.foliage.copy(alpha = .85f), base - Offset(artworkPx(5f), artworkPx(5f)),
                    Size(artworkPx(5f), artworkPx(2.4f)))
            }
        }
        TreeTheme.BLOSSOM -> {
            listOf(.13f to .14f, .83f to .15f, .06f to .43f, .94f to .34f, .17f to .77f, .78f to .77f)
                .forEach { (x, y) -> drawWoodlandFlower(at(x, y), palette.ornament, palette.light, artworkPx(3.8f)) }
            listOf(.29f to .83f, .76f to .87f, .34f to .94f).forEachIndexed { i, (x, y) ->
                val center = at(x, y)
                rotate(if (i % 2 == 0) -28f else 30f, center) {
                    drawOval(palette.ornament.copy(alpha = .85f), center - Offset(artworkPx(2f), artworkPx(1.2f)),
                        Size(artworkPx(4f), artworkPx(2.4f)))
                }
            }
        }
        TreeTheme.WINTER -> {
            listOf(.09f to .18f, .93f to .16f, .96f to .63f, .08f to .76f, .84f to .83f)
                .forEach { (x, y) ->
                    val center = at(x, y)
                    repeat(3) { i ->
                        rotate(i * 60f, center) {
                            drawLine(palette.ornament.copy(alpha = .9f), center - Offset(artworkPx(3.3f), 0f),
                                center + Offset(artworkPx(3.3f), 0f), artworkPx(.9f), cap = StrokeCap.Round)
                        }
                    }
                }
            drawOval(palette.ornament.copy(alpha = .72f), at(.23f, .94f), Size(size.width * .17f, artworkPx(6f)))
            drawOval(palette.ornament.copy(alpha = .65f), at(.64f, .945f), Size(size.width * .16f, artworkPx(5f)))
        }
        TreeTheme.AUTUMN -> {
            listOf(.25f, .73f).forEachIndexed { i, x ->
                val stem = at(x, .958f)
                val capCenter = stem - Offset(0f, artworkPx(if (i == 0) 7f else 10f))
                val radius = artworkPx(if (i == 0) 5f else 6f)
                drawLine(palette.barkLight, stem, capCenter, artworkPx(2.5f), cap = StrokeCap.Round)
                val cap = Path().apply {
                    moveTo(capCenter.x - radius, capCenter.y)
                    cubicTo(capCenter.x - radius, capCenter.y - radius * 1.2f,
                        capCenter.x + radius, capCenter.y - radius * 1.2f, capCenter.x + radius, capCenter.y)
                    quadraticTo(capCenter.x, capCenter.y + radius * .3f, capCenter.x - radius, capCenter.y)
                    close()
                }
                drawPath(cap, palette.ornament)
                drawCircle(palette.light.copy(alpha = .85f), artworkPx(.8f), capCenter - Offset(radius * .25f, radius * .4f))
                drawCircle(palette.light.copy(alpha = .8f), artworkPx(.7f), capCenter + Offset(radius * .3f, -radius * .25f))
            }
        }
        TreeTheme.NIGHT -> {
            listOf(.14f to .78f, .84f to .79f, .74f to .89f).forEach { (x, y) ->
                val center = at(x, y)
                drawCircle(Brush.radialGradient(listOf(palette.light.copy(alpha = .2f), Color.Transparent),
                    center = center, radius = artworkPx(7f)), artworkPx(7f), center)
                drawCircle(palette.light.copy(alpha = .85f), artworkPx(1.2f), center)
            }
        }
    }
}

private fun DrawScope.drawWoodlandFlower(center: Offset, petals: Color, heart: Color, radius: Float) {
    repeat(5) { i ->
        val angle = i * (2 * Math.PI / 5) - Math.PI / 2
        val petal = center + Offset(cos(angle).toFloat() * radius * .8f, sin(angle).toFloat() * radius * .8f)
        drawCircle(petals, radius * .6f, petal)
    }
    drawCircle(heart, radius * .35f, center)
}

// Match the full tree's detail sizes while keeping the profile preview delicately drawn.
private fun DrawScope.artworkPx(dp: Float): Float =
    dp.dp.toPx() * (size.height / 315.dp.toPx()).coerceIn(.3f, 1f)
