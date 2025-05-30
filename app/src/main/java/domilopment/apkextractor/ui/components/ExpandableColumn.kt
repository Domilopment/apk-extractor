package domilopment.apkextractor.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp

@Composable
fun ExpandableColumn(
    title: String, hatMultipleItemsNotificationTitle: String, items: Array<String>
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.border(
            1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)
        )
    ) {
        Row(modifier = Modifier
            .clickable { expanded = !expanded }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .fillMaxWidth()
            .height(IntrinsicSize.Max)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh, shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    modifier = Modifier.weight(1f, fill = false),
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                if (items.size > 1) Text(
                    text = hatMultipleItemsNotificationTitle,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.inversePrimary,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
            }
            Row(
                modifier = Modifier.size(height = 32.dp, width = 42.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VerticalDivider(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEach {
                    Row {
                        Icon(
                            imageVector = Icons.Default.Circle,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .padding(end = 4.dp)
                                .size(8.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(text = it)
                    }
                }
            }
        }
    }
}

@PreviewFontScale
@PreviewScreenSizes
@Preview
@Composable
fun ExpandableColumnPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.padding(8.dp)) {
            ExpandableColumn(
                title = "Expandable column",
                hatMultipleItemsNotificationTitle = "has more",
                items = arrayOf("Item 1", "Item 2", "another one")
            )
        }
    }
}