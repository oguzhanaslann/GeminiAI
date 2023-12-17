package com.oguzhanaslann.geminiai
import androidx.compose.foundation.Image
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.compose.SubcomposeAsyncImage

@Composable
fun ShapeableImage(
    modifier: Modifier = Modifier,
    painter: Painter? = null,
    shape: Shape = RectangleShape,
    placeHolderColor: Color = Color.LightGray,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (painter == null) placeHolderColor else placeHolderColor.copy(0f)
    ) {
        if (painter != null) {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                contentScale = contentScale
            )
        }
    }
}
@Composable
fun ShapeableImage(
    modifier: Modifier = Modifier,
    bitmap: ImageBitmap? = null,
    shape: Shape = RectangleShape,
    placeHolderColor: Color = Color.LightGray,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (bitmap == null) placeHolderColor else placeHolderColor.copy(0f)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                contentScale = contentScale
            )
        }
    }
}

@Composable
fun ShapeableImage(
    modifier: Modifier = Modifier,
    imageUrl: String,
    shape: Shape = RectangleShape,
    placeHolderColor: Color = Color.LightGray,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
    loadingContent: @Composable () -> Unit = { EmptyImageSurface(modifier, shape, placeHolderColor) },
    errorContent: @Composable () -> Unit = { EmptyImageSurface(modifier, shape, placeHolderColor) }
) {

    PreviewDispatchedView(
        mainContent = {
            when {
                imageUrl.isNotEmpty() -> SubcomposeAsyncImage(
                    modifier = modifier,
                    model = imageUrl,
                    contentDescription = contentDescription,
                    loading = { loadingContent() } ,
                    error = { errorContent() },
                    success = {
                        ShapeableImage(
                            modifier = modifier,
                            painter = painter,
                            shape = shape,
                            placeHolderColor = placeHolderColor,
                            contentScale = contentScale,
                            contentDescription = contentDescription
                        )
                    },
                    contentScale = contentScale
                )
                else -> EmptyImageSurface(modifier, shape, placeHolderColor)
            }
        },
        previewContent = { EmptyImageSurface(modifier, shape, placeHolderColor) }
    )
}

@Composable
private fun EmptyImageSurface(
    modifier: Modifier,
    shape: Shape,
    placeHolderColor: Color
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = placeHolderColor,
        content = {}
    )
}