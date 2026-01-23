package com.hritwik.avoid.presentation.ui.screen.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.hritwik.avoid.domain.model.library.MediaItem
import com.hritwik.avoid.presentation.ui.components.media.MediaCardType
import com.hritwik.avoid.presentation.ui.components.media.MediaItemCard
import com.hritwik.avoid.presentation.ui.state.FeatureContentFocusTarget
import com.hritwik.avoid.utils.extensions.homeContentFocusProperties
import com.hritwik.avoid.utils.helpers.calculateRoundedValue
import ir.kaaveh.sdpcompose.sdp

@Composable
fun ThumbnailRow(
    items: List<MediaItem>,
    serverUrl: String,
    contentFocusTarget: FeatureContentFocusTarget?,
    contentFocusRequester: FocusRequester,
    sideNavigationFocusRequester: FocusRequester?,
    onItemSelected: (MediaItem) -> Unit,
    onItemFocused: (MediaItem) -> Unit,
    onFocusedItemChange: (MediaItem?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(calculateRoundedValue(24).sdp),
        contentPadding = PaddingValues(horizontal = calculateRoundedValue(20).sdp)
    ) {
        itemsIndexed(
            items = items,
            key = { _, item -> item.id }
        ) { index, item ->
            val focusRequester = if (index == 0 && contentFocusTarget == FeatureContentFocusTarget.Latest) {
                contentFocusRequester
            } else {
                null
            }
            val itemModifier = if (index == 0) {
                Modifier.homeContentFocusProperties(
                    sideNavigationFocusRequester = sideNavigationFocusRequester,
                    initialFocusRequester = focusRequester
                )
            } else {
                Modifier
            }

            MediaItemCard(
                modifier = itemModifier,
                mediaItem = item,
                serverUrl = serverUrl,
                cardType = MediaCardType.THUMBNAIL,
                showProgress = false,
                enableBringIntoView = true,
                onClick = onItemSelected,
                onFocus = {
                    onItemFocused(it)
                    onFocusedItemChange(it)
                },
                focusRequester = focusRequester
            )
        }
    }
}
