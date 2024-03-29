package cn.copaint.audience.brush

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import cn.copaint.audience.R
import com.wacom.ink.format.enums.RotationMode
import com.wacom.ink.format.rendering.RasterBrush
import com.wacom.ink.rendering.BlendMode
import java.io.ByteArrayOutputStream
import java.util.*

/**
 * Class collecting the all brushes which are used within the application.
 */
class BrushPalette {

    companion object {
        fun getBrush(context: Context, uri: String): RasterBrush? {
            var brush: RasterBrush? = null
            when {
                uri.lowercase(Locale.getDefault()).contains("pencil") -> brush = pencil(context)
                uri.lowercase(Locale.getDefault()).contains("waterbrush") -> brush = waterbrush(context)
                uri.lowercase(Locale.getDefault()).contains("crayon") -> brush = crayonbrush(context)
                uri.lowercase(Locale.getDefault()).contains("eraser") -> brush = eraser(context)
                uri.lowercase(Locale.getDefault()).contains("pen") -> brush = pen(context)
                uri.lowercase(Locale.getDefault()).contains("pen2") -> brush = pen2(context)
                uri.lowercase(Locale.getDefault()).contains("pen3") -> brush = pen3(context)
            }

            return brush
        }

        fun pen(context: Context): RasterBrush {
            val opts = BitmapFactory.Options()
            opts.inSampleSize = 1
            opts.inScaled = false
            // Texture for shape
            val shapeTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_shape, opts)
            // Texture for fill
            val fillTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_fill_11, opts)

            val stream = ByteArrayOutputStream()
            shapeTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val shapeTextureByteArray = stream.toByteArray()

            val stream2 = ByteArrayOutputStream()
            fillTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            val fillTextureByteArray = stream2.toByteArray()

            // Create the raster brush
            var brush = RasterBrush(
                URIBuilder.getBrushURI("raster", "Pen"), // name of the brush
                0.15f, // spacing
                0.15f, // scattering
                RotationMode.RANDOM, // rotation mode
                listOf(shapeTextureByteArray), // shape texture
                listOf(), fillTextureByteArray, // fill texture
                "", // fill texture URI
                fillTexture.width.toFloat(), // width of texture
                fillTexture.height.toFloat(), // height of texture
                false, // randomized fill
                BlendMode.MAX // mode of blending
            )

            shapeTexture.recycle()
            fillTexture.recycle()

            return brush
        }

        fun pen2(context: Context): RasterBrush {
            val opts = BitmapFactory.Options()
            opts.inSampleSize = 1
            opts.inScaled = false
            // Texture for shape
            val shapeTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_shape, opts)
            // Texture for fill
            val fillTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_fill_11, opts)

            val stream = ByteArrayOutputStream()
            shapeTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val shapeTextureByteArray = stream.toByteArray()

            val stream2 = ByteArrayOutputStream()
            fillTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            val fillTextureByteArray = stream2.toByteArray()

            // Create the raster brush
            var brush = RasterBrush(
                URIBuilder.getBrushURI("raster", "Pen2"), // name of the brush
                0.15f, // spacing
                0.15f, // scattering
                RotationMode.RANDOM, // rotation mode
                listOf(shapeTextureByteArray), // shape texture
                listOf(), fillTextureByteArray, // fill texture
                "", // fill texture URI
                fillTexture.width.toFloat(), // width of texture
                fillTexture.height.toFloat(), // height of texture
                false, // randomized fill
                BlendMode.MAX // mode of blending
            )

            shapeTexture.recycle()
            fillTexture.recycle()

            return brush
        }

        fun pen3(context: Context): RasterBrush {
            val opts = BitmapFactory.Options()
            opts.inSampleSize = 1
            opts.inScaled = false
            // Texture for shape
            val shapeTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_shape, opts)
            // Texture for fill
            val fillTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_fill_11, opts)

            val stream = ByteArrayOutputStream()
            shapeTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val shapeTextureByteArray = stream.toByteArray()

            val stream2 = ByteArrayOutputStream()
            fillTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            val fillTextureByteArray = stream2.toByteArray()

            // Create the raster brush
            var brush = RasterBrush(
                URIBuilder.getBrushURI("raster", "Pen3"), // name of the brush
                0.15f, // spacing
                0.15f, // scattering
                RotationMode.RANDOM, // rotation mode
                listOf(shapeTextureByteArray), // shape texture
                listOf(), fillTextureByteArray, // fill texture
                "", // fill texture URI
                fillTexture.width.toFloat(), // width of texture
                fillTexture.height.toFloat(), // height of texture
                false, // randomized fill
                BlendMode.MAX // mode of blending
            )

            shapeTexture.recycle()
            fillTexture.recycle()

            return brush
        }

        fun pencil(context: Context): RasterBrush {
            val opts = BitmapFactory.Options()
            opts.inSampleSize = 1
            opts.inScaled = false
            // Texture for shape
            val shapeTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_shape, opts)
            // Texture for fill
            val fillTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_fill_11, opts)

            val stream = ByteArrayOutputStream()
            shapeTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val shapeTextureByteArray = stream.toByteArray()

            val stream2 = ByteArrayOutputStream()
            fillTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            val fillTextureByteArray = stream2.toByteArray()

            // Create the raster brush
            var brush = RasterBrush(
                URIBuilder.getBrushURI("raster", "Pencil"), // name of the brush
                0.15f, // spacing
                0.15f, // scattering
                RotationMode.RANDOM, // rotation mode
                listOf(shapeTextureByteArray), // shape texture
                listOf(), fillTextureByteArray, // fill texture
                "", // fill texture URI
                fillTexture.width.toFloat(), // width of texture
                fillTexture.height.toFloat(), // height of texture
                false, // randomized fill
                BlendMode.MAX // mode of blending
            )

            shapeTexture.recycle()
            fillTexture.recycle()

            return brush
        }

        fun waterbrush(context: Context): RasterBrush {
            val opts = BitmapFactory.Options()
            opts.inSampleSize = 1
            opts.inScaled = false

            val shapeTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_shape, opts)
            val fillTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_fill_14, opts)

            val stream = ByteArrayOutputStream()
            shapeTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val shapeTextureButeArray = stream.toByteArray()

            val stream2 = ByteArrayOutputStream()
            fillTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            val fillTextureByteArray = stream2.toByteArray()

            var brush = RasterBrush(
                URIBuilder.getBrushURI("raster", "Waterbrush"),
                0.1f, 0.03f,
                RotationMode.RANDOM,
                listOf(shapeTextureButeArray),
                listOf(), fillTextureByteArray,
                "",
                fillTexture.width.toFloat(), fillTexture.height.toFloat(),
                true, BlendMode.MAX
            )

            shapeTexture.recycle()
            fillTexture.recycle()

            return brush
        }

        fun crayonbrush(context: Context): RasterBrush {
            val opts = BitmapFactory.Options()
            opts.inSampleSize = 1
            opts.inScaled = false

            val shapeTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_shape, opts)
            val fillTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_fill_17, opts)

            val stream = ByteArrayOutputStream()
            shapeTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val shapeTextureButeArray = stream.toByteArray()

            val stream2 = ByteArrayOutputStream()
            fillTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            val fillTextureByteArray = stream2.toByteArray()

            var brush = RasterBrush(
                URIBuilder.getBrushURI("raster", "Crayon"),
                0.15f, 0.15f,
                RotationMode.RANDOM,
                listOf(shapeTextureButeArray),
                listOf(), fillTextureByteArray,
                "",
                fillTexture.width.toFloat(), fillTexture.height.toFloat(),
                true, BlendMode.MAX
            )

            shapeTexture.recycle()
            fillTexture.recycle()

            return brush
        }

        fun eraser(context: Context): RasterBrush {
            val opts = BitmapFactory.Options()
            opts.inSampleSize = 1
            opts.inScaled = false

            val shapeTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.shape_circle, opts)
            val fillTexture =
                BitmapFactory.decodeResource(context.resources, R.drawable.essential_fill_8, opts)

            val stream = ByteArrayOutputStream()
            shapeTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val shapeTextureButeArray = stream.toByteArray()

            val stream2 = ByteArrayOutputStream()
            fillTexture!!.compress(Bitmap.CompressFormat.PNG, 100, stream2)
            val fillTextureByteArray = stream2.toByteArray()

            var brush = RasterBrush(
                URIBuilder.getBrushURI("raster", "Eraser"),
                0.1f, 0f,
                RotationMode.RANDOM,
                listOf(shapeTextureButeArray),
                listOf(), fillTextureByteArray,
                "",
                fillTexture.width.toFloat(), fillTexture.height.toFloat(),
                false, BlendMode.MAX
            )

            shapeTexture.recycle()
            fillTexture.recycle()

            return brush
        }
    }
}
