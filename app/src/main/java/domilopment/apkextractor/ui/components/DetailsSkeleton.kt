package domilopment.apkextractor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import domilopment.apkextractor.ui.shimmer

@Composable
fun DetailsSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(12.dp)) {
        // Header Skeleton
        ListItem(headlineContent = {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }, supportingContent = {
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                    .shimmer()
            )
        }, leadingContent = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .shimmer()
            )
        }, trailingContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .shimmer()
            )
        }, colors = ListItemDefaults.colors(containerColor = Color.Transparent))

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Info List Skeleton
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(8) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it % 2 == 0) 0.8f else 0.6f)
                        .height(16.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp))
                        .shimmer()
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Actions Skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(32.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .shimmer()
                )
            }
        }
    }
}