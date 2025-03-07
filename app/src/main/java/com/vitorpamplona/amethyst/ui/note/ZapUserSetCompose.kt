package com.vitorpamplona.amethyst.ui.note

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import com.vitorpamplona.amethyst.ui.screen.ZapUserSetCard
import com.vitorpamplona.amethyst.ui.screen.loggedIn.AccountViewModel
import com.vitorpamplona.amethyst.ui.theme.Size25dp
import com.vitorpamplona.amethyst.ui.theme.Size55Modifier
import com.vitorpamplona.amethyst.ui.theme.newItemBackgroundColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ZapUserSetCompose(zapSetCard: ZapUserSetCard, isInnerNote: Boolean = false, routeForLastRead: String, accountViewModel: AccountViewModel, nav: (String) -> Unit) {
    val defaultBackgroundColor = MaterialTheme.colors.background
    val backgroundColor = remember { mutableStateOf<Color>(defaultBackgroundColor) }
    val newItemColor = MaterialTheme.colors.newItemBackgroundColor

    LaunchedEffect(key1 = zapSetCard.createdAt()) {
        launch(Dispatchers.IO) {
            val isNew = zapSetCard.createdAt > accountViewModel.account.loadLastRead(routeForLastRead)

            accountViewModel.account.markAsRead(routeForLastRead, zapSetCard.createdAt)

            val newBackgroundColor = if (isNew) {
                newItemColor.compositeOver(defaultBackgroundColor)
            } else {
                defaultBackgroundColor
            }

            if (backgroundColor.value != newBackgroundColor) {
                backgroundColor.value = newBackgroundColor
            }
        }
    }

    Column(
        modifier = Modifier
            .background(backgroundColor.value)
            .clickable {
                nav("User/${zapSetCard.user.pubkeyHex}")
            }
    ) {
        Row(
            modifier = Modifier
                .padding(
                    start = if (!isInnerNote) 12.dp else 0.dp,
                    end = if (!isInnerNote) 12.dp else 0.dp,
                    top = 10.dp
                )
        ) {
            // Draws the like picture outside the boosted card.
            if (!isInnerNote) {
                Box(
                    modifier = Size55Modifier
                ) {
                    ZappedIcon(
                        remember {
                            Modifier
                                .size(Size25dp)
                                .align(Alignment.TopEnd)
                        }
                    )
                }
            }

            Column(modifier = Modifier.padding(start = if (!isInnerNote) 10.dp else 0.dp)) {
                val zapEvents by remember { derivedStateOf { zapSetCard.zapEvents } }
                AuthorGalleryZaps(zapEvents, backgroundColor, nav, accountViewModel)

                UserCompose(baseUser = zapSetCard.user, accountViewModel = accountViewModel, nav = nav)
            }
        }
    }
}
