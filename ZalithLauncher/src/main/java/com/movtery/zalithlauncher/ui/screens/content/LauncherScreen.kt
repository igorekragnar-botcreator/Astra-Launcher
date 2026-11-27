/*
 * Zalith Launcher 2
 * Copyright (C) 2025 MovTery <movtery228@qq.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/gpl-3.0.txt>.
 */

package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.movtery.cardgrid.state.rememberCardGridState
import com.movtery.guide.GuideSide
import com.movtery.guide.guideNode
import com.movtery.zalithlauncher.BuildConfig
import com.movtery.zalithlauncher.BuildKeys
import com.movtery.zalithlauncher.R
import com.movtery.zalithlauncher.game.account.Account
import com.movtery.zalithlauncher.game.account.AccountsManager
import com.movtery.zalithlauncher.game.account.getAccountTypeName
import com.movtery.zalithlauncher.game.version.installed.Version
import com.movtery.zalithlauncher.game.version.installed.VersionsManager
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.BackgroundCard
import com.movtery.zalithlauncher.ui.components.ScalingActionButton
import com.movtery.zalithlauncher.ui.guide.GuideKeys
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.content.elements.PlayerFace
import com.movtery.zalithlauncher.ui.screens.content.elements.VersionIconImage
import com.movtery.zalithlauncher.ui.screens.content.home.HomeGrid
import com.movtery.zalithlauncher.ui.screens.content.home.server.LocalServerCardQuickPlay
import com.movtery.zalithlauncher.ui.screens.content.home.version.LocalHomeCardLauncher
import com.movtery.zalithlauncher.ui.screens.content.home.version.LocalHomeCardVersionSettings
import com.movtery.zalithlauncher.ui.theme.GeistMono
import com.movtery.zalithlauncher.utils.animation.swapAnimateDpAsState
import com.movtery.zalithlauncher.viewmodel.ScreenBackStackViewModel
import kotlin.math.roundToInt

/**
 * Home screen: the card grid on the left, the fixed Astra launch deck on the right.
 * The deck is a permanent part of the layout instead of a draggable upstream action menu.
 */
@Composable
fun LauncherScreen(
    backStackViewModel: ScreenBackStackViewModel,
    navigateToVersions: (Version) -> Unit,
    onLaunchGame: (Version?) -> Unit,
    onQuickPlayServer: (Version?, String) -> Unit,
    onOpenLink: (String) -> Unit,
    startGuideOnce: (GuideKeys.Keys) -> Unit,
) {
    LaunchedEffect(Unit) {
        startGuideOnce(GuideKeys.Main)
    }

    BaseScreen(
        screenKey = NormalNavKey.LauncherMain,
        currentKey = backStackViewModel.mainScreen.currentKey
    ) { isVisible ->
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val deckWidth = if (maxWidth < 720.dp) 292.dp else 344.dp

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompositionLocalProvider(
                    LocalUriHandler provides object : UriHandler {
                        override fun openUri(uri: String) {
                            onOpenLink(uri)
                        }
                    }
                ) {
                    ContentMenu(
                        modifier = Modifier
                            .guideNode(
                                key = GuideKeys.Main.Step.CardTip,
                                holeRadius = 0.dp
                            )
                            .weight(1f),
                        isVisible = isVisible,
                        onLaunchGame = { version ->
                            onLaunchGame(version)
                        },
                        onQuickPlayServer = onQuickPlayServer,
                        onOpenVersionSettings = navigateToVersions,
                        toDownloadScreen = {
                            backStackViewModel.navigateToDownload()
                        }
                    )
                }

                LaunchDeck(
                    modifier = Modifier
                        .width(deckWidth)
                        .fillMaxHeight(),
                    isVisible = isVisible,
                    onLaunchGame = { onLaunchGame(null) },
                    onLaunchSpecificVersion = onLaunchGame,
                    onOpenVersionSettings = navigateToVersions,
                    toAccountManageScreen = {
                        backStackViewModel.mainScreen.navigateTo(
                            screenKey = NormalNavKey.AccountManager(FirstLoginMenu.NONE)
                        )
                    },
                    toVersionManageScreen = {
                        backStackViewModel.mainScreen.removeAndNavigateTo(
                            remove = NestedNavKey.VersionSettings::class,
                            screenKey = NormalNavKey.VersionsManager
                        )
                    },
                    toDownloadScreen = {
                        backStackViewModel.navigateToDownload()
                    }
                )
            }
        }
    }
}

@Composable
private fun LaunchDeck(
    isVisible: Boolean,
    onLaunchGame: () -> Unit,
    onLaunchSpecificVersion: (Version?) -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    toAccountManageScreen: () -> Unit,
    toVersionManageScreen: () -> Unit,
    toDownloadScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )

    val account by AccountsManager.currentAccountFlow.collectAsStateWithLifecycle()
    val version by VersionsManager.currentVersion.collectAsStateWithLifecycle()
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    var showVersions by remember { mutableStateOf(false) }

    BackgroundCard(
        modifier = modifier.offset { IntOffset(x = 0, y = yOffset.roundToPx()) },
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DeckHeader()

            AccountCapsule(
                account = account,
                onClick = toAccountManageScreen
            )

            VersionHero(
                modifier = Modifier.weight(1f),
                version = version,
                showVersions = showVersions,
                versions = versions,
                onOpenSettings = {
                    version?.let(onOpenVersionSettings)
                },
                onOpenList = { showVersions = true },
                onDismissList = { showVersions = false },
                onLaunchSpecificVersion = { target ->
                    showVersions = false
                    onLaunchSpecificVersion(target)
                }
            )

            LaunchButton(onClick = onLaunchGame)

            QuickActions(
                toVersions = toVersionManageScreen,
                toDownloads = toDownloadScreen,
                toAccounts = toAccountManageScreen
            )
        }
    }
}

@Composable
private fun DeckHeader(modifier: Modifier = Modifier) {
    val palette = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.splash_launcher),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = BuildKeys.LAUNCHER_NAME.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = GeistMono,
                letterSpacing = 2.sp
            ),
            color = palette.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = GeistMono),
            color = palette.onSurfaceVariant.copy(alpha = 0.55f),
            maxLines = 1
        )
    }
}

@Composable
private fun AccountCapsule(
    account: Account?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.colorScheme
    BackgroundCard(
        modifier = modifier
            .fillMaxWidth()
            .guideNode(
                key = GuideKeys.Main.Step.Account,
                preferSide = GuideSide.Above
            ),
        shape = MaterialTheme.shapes.large,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.surfaceContainerHighest.copy(alpha = 0.4f))
                    .border(1.dp, palette.primary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (account != null) {
                    PlayerFace(
                        account = account,
                        avatarSize = 34.dp,
                        modifier = Modifier.clip(CircleShape)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = palette.primary
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = account?.username
                        ?: stringResource(R.string.account_add_new_account),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (account != null) {
                        getAccountTypeName(account)
                    } else {
                        stringResource(R.string.account_type_local)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Icon(
                painter = painterResource(R.drawable.ic_arrow_right_rounded),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = palette.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun VersionHero(
    version: Version?,
    versions: List<Version>,
    showVersions: Boolean,
    onOpenSettings: () -> Unit,
    onOpenList: () -> Unit,
    onDismissList: () -> Unit,
    onLaunchSpecificVersion: (Version?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        palette.primaryContainer.copy(alpha = 0.55f),
                        palette.surfaceContainerHigh.copy(alpha = 0.3f),
                        palette.tertiaryContainer.copy(alpha = 0.4f)
                    )
                )
            )
            .border(1.dp, palette.primary.copy(alpha = 0.18f), shape)
            .combinedClickable(
                role = Role.Button,
                onClick = onOpenSettings,
                onLongClick = {
                    if (version != null) onOpenList()
                }
            )
            .guideNode(
                key = GuideKeys.Main.Step.VersionList,
                preferSide = GuideSide.Above
            )
    ) {
        Image(
            painter = painterResource(R.drawable.splash_launcher),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-26).dp)
                .size(150.dp)
                .alpha(0.18f)
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(palette.surfaceContainerLowest.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                VersionIconImage(
                    version = version,
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                if (version == null) {
                    Text(
                        text = stringResource(R.string.versions_manage_no_versions),
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = version.getVersionName(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (version.isValid()) {
                            version.getVersionSummary()
                        } else {
                            stringResource(R.string.home_version_card_inaccessible)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                painter = painterResource(R.drawable.ic_settings_filled),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = palette.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        DropdownMenu(
            expanded = showVersions,
            onDismissRequest = onDismissList,
            modifier = Modifier.width(280.dp)
        ) {
            if (versions.isEmpty()) {
                DropdownMenuItem(
                    text = {
                        Text(text = stringResource(R.string.versions_manage_no_versions))
                    },
                    onClick = onDismissList
                )
            } else {
                versions.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = item.getVersionName(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = item.getVersionSummary(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        },
                        leadingIcon = {
                            VersionIconImage(
                                version = item,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        trailingIcon = {
                            if (item == version) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = palette.primary
                                )
                            }
                        },
                        onClick = {
                            onDismissList()
                            onLaunchSpecificVersion(item)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LaunchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.large

    ScalingActionButton(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = palette.primary.copy(alpha = 0.45f),
                spotColor = palette.primary.copy(alpha = 0.45f)
            ),
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = palette.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        listOf(palette.primary, palette.tertiary)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_play_arrow_filled),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = palette.onPrimary
                )
                Text(
                    text = stringResource(R.string.main_launch_game),
                    style = MaterialTheme.typography.titleSmall.copy(
                        letterSpacing = 0.6.sp
                    ),
                    color = palette.onPrimary
                )
            }
        }
    }
}

@Composable
private fun QuickActions(
    toVersions: () -> Unit,
    toDownloads: () -> Unit,
    toAccounts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickAction(
            modifier = Modifier.weight(1f),
            icon = R.drawable.ic_dashboard_filled,
            label = stringResource(R.string.home_quick_versions),
            onClick = toVersions
        )
        QuickAction(
            modifier = Modifier.weight(1f),
            icon = R.drawable.ic_download_2_filled,
            label = stringResource(R.string.generic_download),
            onClick = toDownloads
        )
        QuickAction(
            modifier = Modifier.weight(1f),
            icon = R.drawable.ic_person_outlined,
            label = stringResource(R.string.home_quick_accounts),
            onClick = toAccounts
        )
    }
}

@Composable
private fun QuickAction(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.height(44.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = palette.surfaceContainerHigh.copy(alpha = 0.45f),
        contentColor = palette.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ContentMenu(
    isVisible: Boolean,
    onLaunchGame: (Version) -> Unit,
    onQuickPlayServer: (Version?, String) -> Unit,
    onOpenVersionSettings: (Version) -> Unit,
    toDownloadScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val yOffset by swapAnimateDpAsState(
        targetValue = (-40).dp,
        swapIn = isVisible
    )
    val gridState = rememberCardGridState()
    val versions by VersionsManager.versions.collectAsStateWithLifecycle()
    val isRefreshing by VersionsManager.isRefreshing.collectAsStateWithLifecycle()

    CompositionLocalProvider(
        LocalHomeCardLauncher provides onLaunchGame,
        LocalHomeCardVersionSettings provides onOpenVersionSettings,
        LocalServerCardQuickPlay provides onQuickPlayServer
    ) {
        if (!isRefreshing && versions.isEmpty()) {
            HomeEmptyState(
                isVisible = isVisible,
                toDownload = toDownloadScreen,
                modifier = modifier
            )
        } else {
            HomeGrid(
                state = gridState,
                isVisible = isVisible,
                modifier = modifier
                    .fillMaxSize()
                    .offset { IntOffset(x = 0, y = yOffset.roundToPx()) }
            )
        }
    }
}

@Composable
private fun HomeEmptyState(
    isVisible: Boolean,
    toDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val enter: EnterTransition = fadeIn(
        animationSpec = tween(220, delayMillis = 60, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
    ) + scaleIn(
        initialScale = 0.96f,
        animationSpec = tween(260, delayMillis = 60, easing = CubicBezierEasing(0.2f, 0f, 0f, 1f))
    )

    AnimatedVisibility(
        visible = isVisible,
        enter = enter,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.splash_launcher),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.home_empty_body),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = LocalContentColor.current.copy(alpha = 0.72f)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = toDownload) {
                Text(text = stringResource(R.string.home_empty_button))
            }
        }
    }
}
