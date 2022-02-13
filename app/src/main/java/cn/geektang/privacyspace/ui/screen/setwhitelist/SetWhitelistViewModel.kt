package cn.geektang.privacyspace.ui.screen.setwhitelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cn.geektang.privacyspace.ConfigConstant
import cn.geektang.privacyspace.bean.AppInfo
import cn.geektang.privacyspace.util.AppHelper.loadAllAppList
import cn.geektang.privacyspace.util.AppHelper.sortApps
import cn.geektang.privacyspace.util.ConfigHelper
import cn.geektang.privacyspace.util.setDifferentValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SetWhitelistViewModel(private val context: Application) : AndroidViewModel(context) {
    private val allAppListFlow = MutableStateFlow<List<AppInfo>>(emptyList())
    val appListFlow = MutableStateFlow<List<AppInfo>>(emptyList())
    val whitelistFlow = MutableStateFlow<Set<String>>(emptySet())
    val showSystemAppsFlow = MutableStateFlow(false)
    private var isModified = false

    init {
        viewModelScope.launch {
            launch {
                val defaultWhitelist = ConfigConstant.defaultWhitelist
                allAppListFlow.value =
                    context.loadAllAppList()
                        .filter {
                            !defaultWhitelist.contains(it.packageName)
                        }
                        .sortApps(context = context, toTopCollections = whitelistFlow.value)
                updateAppInfoListFlow()
            }
            launch {
                val whiteList = mutableSetOf<String>()
                ConfigHelper.loadWhitelistConfig(whiteList)
                whitelistFlow.value = whiteList
                allAppListFlow.value = appListFlow.value.sortApps(
                    context = context,
                    toTopCollections = whitelistFlow.value
                )
                updateAppInfoListFlow()
            }
        }
    }

    fun addApp2Whitelist(appInfo: AppInfo) {
        val whitelist = whitelistFlow.value.toMutableSet()
        whitelist.add(appInfo.packageName)
        whitelistFlow.value = whitelist
        isModified = true
    }

    fun removeApp2Whitelist(appInfo: AppInfo) {
        val whitelist = whitelistFlow.value.toMutableSet()
        whitelist.remove(appInfo.packageName)
        whitelistFlow.value = whitelist
        isModified = true
    }

    private fun updateAppInfoListFlow() {
        val appList = allAppListFlow.value
        if (showSystemAppsFlow.value) {
            appListFlow.setDifferentValue(appList)
        } else {
            appListFlow.setDifferentValue(appList.filter {
                !it.isSystemApp
            })
        }
    }

    fun tryUpdateConfig() {
        if (isModified) {
            ConfigHelper.updateWhitelistConfig(context, whitelistFlow.value)
            isModified = false
        }
    }

    fun setSystemAppsVisible(showSystemApps: Boolean) {
        if (showSystemAppsFlow.value != showSystemApps) {
            showSystemAppsFlow.value = showSystemApps
            updateAppInfoListFlow()
        }
    }
}