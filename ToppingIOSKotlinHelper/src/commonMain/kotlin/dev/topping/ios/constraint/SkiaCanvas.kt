package dev.topping.ios.constraint

import dev.topping.ios.constraint.shared.graphics.isIdentity
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.refTo
import org.jetbrains.skia.*
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.paragraph.Alignment
import org.jetbrains.skia.paragraph.BaselineMode
import org.jetbrains.skia.paragraph.DecorationStyle
import org.jetbrains.skia.paragraph.Direction
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.Paragraph
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.PlaceholderAlignment
import org.jetbrains.skia.paragraph.PlaceholderStyle
import org.jetbrains.skia.paragraph.Shadow
import org.jetbrains.skia.paragraph.StrutStyle
import org.jetbrains.skia.paragraph.TextBox
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkikoUIView
import org.jetbrains.skiko.SkikoView
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.CoreGraphics.*
import platform.UIKit.UIImage
import platform.UIKit.UIImageOrientation

typealias Matrix = Matrix33

class Size(val width: Float, val height: Float)

fun TPaint.toSkiaPaint(): SkiaPaint {
    return this as SkiaPaint
}

class ObjcUtils {
    companion object {
        fun makePathMeasure() : PathMeasure {
            return PathMeasure()
        }
    }
}

fun Image.getAlignment() : Alignment? = null
fun Image.getBaselineMode() : BaselineMode? = null
fun Image.getDecorationStyle() : DecorationStyle? = null
fun Image.getDirection() : Direction? = null
fun Image.getParagraph() : Paragraph? = null
fun Image.getParagraphBuilder() : ParagraphBuilder? = null
fun Image.getParagraphStyle() : ParagraphStyle? = null
fun Image.getPlaceholderAlignment() : PlaceholderAlignment? = null
fun Image.getPlaceholderStyle() : PlaceholderStyle? = null
fun Image.getShadow() : Shadow? = null
fun Image.getStrutStyle() : StrutStyle? = null
fun Image.getTextBox() : TextBox? = null
fun Image.getTextStyle() : TextStyle? = null
fun Image.getFontMgr() : FontMgr? = null
fun Image.getFontCollection() : FontCollection? = null
fun Image.getTypeface() : Typeface? = null
fun Image.getTypefaceFontProvider() : TypefaceFontProvider? = null
fun Image.getFontFamilyName() : FontFamilyName? = null
fun Image.getBreakIterator() : BreakIterator? = null
fun Image.getSurface() : Surface? = null
fun Image.getSurfaceOrigin() : SurfaceOrigin? = null
fun Image.getSurfaceColorFormat() : SurfaceColorFormat? = null
fun Image.getMetalRedrawer() : MetalRedrawer? = null
fun Image.getPictureRecorder() : PictureRecorder? = null

open class ToppingSkiaLayer : SkiaLayer() {
    fun createSkikoView(): SkikoUIView {
        val view = SkikoUIView(this)
        attachTo(view)
        return view
    }
}

interface ISkikoView {
    fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long)
}

open class ToppingSkikoView(private val skikoView: ISkikoView) : SkikoView {
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        skikoView.onRender(canvas, width, height, nanoTime)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun SkiaCanvas.toUIImage(scale: Double = 1.0): UIImage {
    if(bitmap == null)
        return UIImage()
    memScoped {
        var info = bitmap.imageInfo

        val pixels = bitmap.readPixels()
        if(pixels == null)
            return UIImage()

        val provider = CGDataProviderCreateDirect(pixels.refTo(0), pixels.size.toLong(), null)
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val cgImage = CGImageCreate(info.width.toULong(), info.height.toULong(),
            8u, (info.bytesPerPixel * 8).toULong(),
            info.minRowBytes.toULong(), colorSpace,
            CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst.value or (2U shl 12),
            provider, null, false, CGColorRenderingIntent.kCGRenderingIntentDefault
        )
        CGDataProviderRelease(provider)
        if(cgImage == null)
            return UIImage()

        return UIImage(cgImage, scale, UIImageOrientation.UIImageOrientationUp)
    }
}

@OptIn(ExperimentalForeignApi::class)
fun UIImage.toSkiaImage(): Image? {
    val imageRef = CGImageCreateCopyWithColorSpace(this.CGImage, CGColorSpaceCreateDeviceRGB()) ?: return null

    val width = CGImageGetWidth(imageRef).toInt()
    val height = CGImageGetHeight(imageRef).toInt()

    val bytesPerRow = CGImageGetBytesPerRow(imageRef)
    val data = CGDataProviderCopyData(CGImageGetDataProvider(imageRef))
    val bytePointer = CFDataGetBytePtr(data)
    val length = CFDataGetLength(data)
    val alphaInfo = CGImageGetAlphaInfo(imageRef)

    val alphaType = when (alphaInfo) {
        CGImageAlphaInfo.kCGImageAlphaPremultipliedFirst, CGImageAlphaInfo.kCGImageAlphaPremultipliedLast -> ColorAlphaType.PREMUL
        CGImageAlphaInfo.kCGImageAlphaFirst, CGImageAlphaInfo.kCGImageAlphaLast -> ColorAlphaType.UNPREMUL
        CGImageAlphaInfo.kCGImageAlphaNone, CGImageAlphaInfo.kCGImageAlphaNoneSkipFirst, CGImageAlphaInfo.kCGImageAlphaNoneSkipLast -> ColorAlphaType.OPAQUE
        else -> ColorAlphaType.UNKNOWN
    }

    val byteArray = ByteArray(length.toInt()) { index ->
        bytePointer!![index].toByte()
    }
    CFRelease(data)
    CFRelease(imageRef)

    return Image.makeRaster(
        imageInfo = ImageInfo(width = width, height = height, colorType = ColorType.RGBA_8888, alphaType = alphaType),
        bytes = byteArray,
        rowBytes = bytesPerRow.toInt(),
    )
}

fun UIImage.toSkiaCanvas(): Canvas? {
    return toSkiaImage()?.toCanvas()
}

fun Image.toBitmap(): Bitmap {
    val bitmap = Bitmap()
    bitmap.allocPixels(ImageInfo.makeN32(width, height, ColorAlphaType.PREMUL))
    val canvas = Canvas(bitmap)
    canvas.drawImage(this, 0f, 0f)
    bitmap.setImmutable()
    return bitmap
}

fun Image.toCanvas(): Canvas {
    val bitmap = Bitmap()
    bitmap.allocPixels(ImageInfo.makeN32(width, height, ColorAlphaType.PREMUL))
    val canvas = Canvas(bitmap)
    canvas.drawImage(this, 0f, 0f)
    return canvas
}

class SkiaCanvas : TCanvas {

    val skia: Canvas
    val bitmap: Bitmap?

    constructor() {
        this.bitmap = Bitmap()
        skia = Canvas(bitmap)
    }

    constructor(bitmap: Bitmap) {
        skia = Canvas(bitmap)
        this.bitmap = bitmap
    }

    constructor(bitmap: Bitmap, surfaceProps: SurfaceProps) {
        skia = Canvas(bitmap, surfaceProps)
        this.bitmap = bitmap
    }

    constructor(canvas: Canvas) {
        skia = canvas
        bitmap = null
    }

    var alphaMultiplier: Float = 1.0f

    override fun drawRect(r: dev.topping.ios.constraint.core.motion.utils.Rect, paint: TPaint) {
        drawRect(r.left.toFloat(), r.top.toFloat(), r.right.toFloat(), r.bottom.toFloat(), paint)
    }

    override fun save() {
        skia.save()
    }

    override fun restore() {
        skia.restore()
    }

    override fun saveLayer(bounds: Rect, paint: TPaint) {
        skia.saveLayer(
            bounds.left,
            bounds.top,
            bounds.right,
            bounds.bottom,
            paint.toSkiaPaint().skia
        )
    }

    override fun translate(dx: Float, dy: Float) {
        skia.translate(dx, dy)
    }

    override fun scale(sx: Float, sy: Float) {
        skia.scale(sx, sy)
    }

    override fun rotate(degrees: Float) {
        skia.rotate(degrees)
    }

    override fun skew(sx: Float, sy: Float) {
        skia.skew(sx, sy)
    }

    override fun concat(matrix: Matrix) {
        if (!matrix.isIdentity()) {
            skia.concat(matrix)
        }
    }

    override fun clipRect(left: Float, top: Float, right: Float, bottom: Float, clipOp: ClipMode) {
        val antiAlias = true
        skia.clipRect(Rect.makeLTRB(left, top, right, bottom), clipOp, antiAlias)
    }

    override fun clipPath(path: Path, clipMode: ClipMode) {
        val antiAlias = true
        skia.clipPath(path, clipMode, antiAlias)
    }

    override fun drawText(text: String, x: Float, y: Float, paint: TPaint) {
        skia.drawTextLine(TextLine.make(text, paint.font), x, y, paint.toSkiaPaint().skia)
    }

    override fun drawLine(p1: Point, p2: Point, paint: TPaint) {
        skia.drawLine(p1.x, p1.y, p2.x, p2.y, paint.toSkiaPaint().skia)
    }

    override fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int, paint: TPaint) {
        skia.drawLine(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat(), paint.toSkiaPaint().skia)
    }

    override fun drawRect(left: Float, top: Float, right: Float, bottom: Float, paint: TPaint) {
        skia.drawRect(Rect.makeLTRB(left, top, right, bottom), paint.toSkiaPaint().skia)
    }

    override fun drawRoundRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radiusX: Float,
        radiusY: Float,
        paint: TPaint
    ) {
        skia.drawRRect(
            RRect.makeLTRB(
                left,
                top,
                right,
                bottom,
                radiusX,
                radiusY
            ),
            paint.toSkiaPaint().skia
        )
    }

    override fun drawOval(left: Float, top: Float, right: Float, bottom: Float, paint: TPaint) {
        skia.drawOval(Rect.makeLTRB(left, top, right, bottom), paint.toSkiaPaint().skia)
    }

    override fun drawCircle(center: Point, radius: Float, paint: TPaint) {
        skia.drawCircle(center.x, center.y, radius, paint.toSkiaPaint().skia)
    }

    override fun drawArc(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean,
        paint: TPaint
    ) {
        skia.drawArc(
            left,
            top,
            right,
            bottom,
            startAngle,
            sweepAngle,
            useCenter,
            paint.toSkiaPaint().skia
        )
    }

    override fun drawPath(path: Path, paint: TPaint) {
        skia.drawPath(path, paint.toSkiaPaint().skia)
    }

    override fun drawImage(image: Bitmap, topLeftOffset: Point, paint: TPaint) {
        val size = Size(image.width.toFloat(), image.height.toFloat())
        drawImageRect(image, Point(0f, 0f), size, topLeftOffset, size, paint)
    }

    override fun drawImageRect(
        image: Bitmap,
        srcOffset: Point,
        srcSize: Size,
        dstOffset: Point,
        dstSize: Size,
        paint: TPaint
    ) {
        drawImageRect(
            image,
            Point(srcOffset.x.toFloat(), srcOffset.y.toFloat()),
            Size(srcSize.width.toFloat(), srcSize.height.toFloat()),
            Point(dstOffset.x.toFloat(), dstOffset.y.toFloat()),
            Size(dstSize.width.toFloat(), dstSize.height.toFloat()),
            paint
        )
    }

    // TODO(demin): probably this method should be in the common Canvas
    private fun drawImageRect(
        image: Bitmap,
        srcOffset: Point,
        srcSize: Size,
        dstOffset: Point,
        dstSize: Size,
        samplingMode: SamplingMode,
        paint: TPaint
    ) {
        val bitmap = image
        // TODO(gorshenev): need to use skiko's .use() rather than jvm one here.
        // But can't do that as skiko is jvmTarget=11 for now, so can't inline
        // into jvmTarget=8 compose.
        // After this issue is resolved use:
        //     import org.jetbrains.skia.impl.use
        Image.makeFromBitmap(bitmap).use { skiaImage ->
            skia.drawImageRect(
                skiaImage,
                Rect.makeXYWH(
                    srcOffset.x,
                    srcOffset.y,
                    srcSize.width,
                    srcSize.height
                ),
                Rect.makeXYWH(
                    dstOffset.x,
                    dstOffset.y,
                    dstSize.width,
                    dstSize.height
                ),
                samplingMode,
                paint.toSkiaPaint().skia,
                true
            )
        }
    }

    override fun enableZ() = Unit

    override fun disableZ() = Unit

    private fun drawPoints(points: List<Point>, paint: TPaint) {
        points.forEach { point ->
            skia.drawPoint(
                point.x,
                point.y,
                paint.toSkiaPaint().skia
            )
        }
    }

    /**
     * Draw lines connecting points based on the corresponding step.
     *
     * ex. 3 points with a step of 1 would draw 2 lines between the first and second points
     * and another between the second and third
     *
     * ex. 4 points with a step of 2 would draw 2 lines between the first and second and another
     * between the third and fourth. If there is an odd number of points, the last point is
     * ignored
     *
     * @see drawRawLines
     */
    private fun drawLines(points: List<Point>, paint: TPaint, stepBy: Int) {
        if (points.size >= 2) {
            for (i in 0 until points.size - 1 step stepBy) {
                val p1 = points[i]
                val p2 = points[i + 1]
                skia.drawLine(
                    p1.x,
                    p1.y,
                    p2.x,
                    p2.y,
                    paint.toSkiaPaint().skia
                )
            }
        }
    }

    private fun drawRawPoints(points: FloatArray, paint: TPaint, stepBy: Int) {
        if (points.size % 2 == 0) {
            for (i in 0 until points.size - 1 step stepBy) {
                val x = points[i]
                val y = points[i + 1]
                skia.drawPoint(x, y, paint.toSkiaPaint().skia)
            }
        }
    }

    /**
     * Draw lines connecting points based on the corresponding step. The points are interpreted
     * as x, y coordinate pairs in alternating index positions
     *
     * ex. 3 points with a step of 1 would draw 2 lines between the first and second points
     * and another between the second and third
     *
     * ex. 4 points with a step of 2 would draw 2 lines between the first and second and another
     * between the third and fourth. If there is an odd number of points, the last point is
     * ignored
     *
     * @see drawLines
     */
    private fun drawRawLines(points: FloatArray, paint: TPaint, stepBy: Int) {
        // Float array is treated as alternative set of x and y coordinates
        // x1, y1, x2, y2, x3, y3, ... etc.
        if (points.size >= 4 && points.size % 2 == 0) {
            for (i in 0 until points.size - 3 step stepBy * 2) {
                val x1 = points[i]
                val y1 = points[i + 1]
                val x2 = points[i + 2]
                val y2 = points[i + 3]
                skia.drawLine(
                    x1,
                    y1,
                    x2,
                    y2,
                    paint.toSkiaPaint().skia
                )
            }
        }
    }
}