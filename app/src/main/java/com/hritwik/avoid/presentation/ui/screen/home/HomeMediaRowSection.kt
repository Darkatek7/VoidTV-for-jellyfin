package com.hritwik.avoid.presentation.ui.screen.home

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import com.hritwik.avoid.domain.model.library.MediaItem
import com.hritwik.avoid.presentation.ui.components.layout.SectionHeader
import com.hritwik.avoid.presentation.ui.components.media.MediaCardType
import com.hritwik.avoid.presentation.ui.components.media.MediaItemCard
import com.hritwik.avoid.presentation.ui.state.FeatureContentFocusTarget
import com.hritwik.avoid.utils.extensions.homeContentFocusProperties
import com.hritwik.avoid.utils.helpers.calculateRoundedValue
import ir.kaaveh.sdpcompose.sdp

@Composable
fun HomeMediaRowSection(
    title: String,
    keyPrefix: String,
    items: List<MediaItem>,
    serverUrl: String,
    contentFocusTarget: FeatureContentFocusTarget?,
    contentFocusRequester: FocusRequester,
    sideNavigationFocusRequester: FocusRequester?,
    onMediaItemClick: (MediaItem) -> Unit,
    onMediaItemFocus: (MediaItem) -> Unit,
    onFocusedItemChange: (MediaItem?) -> Unit,
    focusTargetOverride: FeatureContentFocusTarget? = FeatureContentFocusTarget.Resume,
    showProgress: Boolean = true,
    showTitle: Boolean = true,
) {
    val focusTarget = focusTargetOverride ?: FeatureContentFocusTarget.Resume

    SectionHeader(title = title) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .focusGroup(),
            horizontalArrangement = Arrangement.spacedBy(calculateRoundedValue(24).sdp),
            contentPadding = PaddingValues(
                start = calculateRoundedValue(20).sdp,
                end = calculateRoundedValue(20).sdp
            )
        ) {
            itemsIndexed(
                items = items,
                key = { _, item -> "${keyPrefix}_${item.id}" }
            ) { index, mediaItem ->
                val focusRequester = if (index == 0 && contentFocusTarget == focusTarget) {
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
                    mediaItem = mediaItem,
                    serverUrl = serverUrl,
                    cardType = MediaCardType.THUMBNAIL,
                    showProgress = showProgress,
                    showTitle = showTitle,
                    enableBringIntoView = true,
                    onClick = onMediaItemClick,
                    onFocus = {
                        onMediaItemFocus(it)
                        onFocusedItemChange(it)
                    },
                    focusRequester = focusRequester
                )
            }
        }
    }
}
