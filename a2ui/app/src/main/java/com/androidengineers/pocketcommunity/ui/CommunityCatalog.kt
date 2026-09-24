package com.androidengineers.pocketcommunity.ui

import androidx.a2ui.compose.ui.A2uiCatalog
import androidx.a2ui.model.catalog.functions.A2uiLocaleProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.a2ui.catalog.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.androidengineers.pocketcommunity.R

fun communityCatalog(): A2uiCatalog =
    materialA2uiBasicCatalogV1(
        image =
            MaterialA2uiBasicCatalogV1Defaults.image { url, description, _, modifier, onError ->
                if (url == "asset://community")
                    Image(
                        painterResource(R.drawable.community_art),
                        description,
                        modifier.fillMaxWidth().height(160.dp).clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                else Text("Image unavailable", modifier)
            },
        video =
            MaterialA2uiBasicCatalogV1Defaults.video { _, modifier, _ ->
                Text("Video unavailable", modifier)
            },
        audioPlayer =
            MaterialA2uiBasicCatalogV1Defaults.audioPlayer { _, _, modifier, _ ->
                Text("Audio unavailable", modifier)
            },
        urlOpener = { _ -> error("Use a validated event action to open links") },
        messageFormatter = { pattern, _, _ -> pattern },
        localeProvider = A2uiLocaleProvider.Default,
    )
