package com.arturo254.opentune.ui.screens.settings

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.arturo254.opentune.R
import com.arturo254.opentune.constants.StopOnSwipeDownKey
import com.arturo254.opentune.constants.SwipeToQueueKey
import com.arturo254.opentune.ui.component.SettingsGeneralCategory
import com.arturo254.opentune.ui.component.SettingsPage
import com.arturo254.opentune.ui.component.SwitchPreference
import com.arturo254.opentune.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BehaviourSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (swipeToQueue, onSwipeToQueueChange) = rememberPreference(
        SwipeToQueueKey,
        defaultValue = true
    )
    val (stopOnSwipeDown, onStopOnSwipeDownChange) = rememberPreference(
        StopOnSwipeDownKey,
        defaultValue = true
    )

    SettingsPage(
        title = stringResource(R.string.behaviour),
        navController = navController,
        scrollBehavior = scrollBehavior,
    ) {
        SettingsGeneralCategory(
            title = stringResource(R.string.behaviour),
            items = listOf(
                {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.swipe_to_queue)) },
                        description = stringResource(R.string.swipe_to_queue_desc),
                        icon = { Icon(painterResource(R.drawable.queue_music), null) },
                        checked = swipeToQueue,
                        onCheckedChange = onSwipeToQueueChange
                    )
                },
                {
                    SwitchPreference(
                        title = { Text(stringResource(R.string.swipe_down_to_stop)) },
                        description = stringResource(R.string.swipe_down_to_stop_desc),
                        icon = { Icon(painterResource(R.drawable.close), null) },
                        checked = stopOnSwipeDown,
                        onCheckedChange = onStopOnSwipeDownChange
                    )
                },
            )
        )
    }
}
