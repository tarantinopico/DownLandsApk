package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ViewMode { LIST, GRID, GALLERY }
enum class SortOrder { NEWEST, OLDEST, NAME_ASC, NAME_DESC, SIZE_DESC, SIZE_ASC, TYPE }
enum class ThemeMode { SYSTEM, LIGHT, DARK }

class SettingsRepository(
    private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val TREE_URI_KEY = stringPreferencesKey("tree_uri")
        val CURRENT_ROUTE_KEY = stringPreferencesKey("current_route")
        val VIEW_MODE_KEY = stringPreferencesKey("view_mode")
        val GROUP_BY_CATEGORY_KEY = booleanPreferencesKey("group_by_category")
        val SORT_ORDER_KEY = stringPreferencesKey("sort_order")
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR_KEY = booleanPreferencesKey("dynamic_color")
    }

    val treeUriFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[TREE_URI_KEY]
    }

    val currentRouteFlow: Flow<String> = dataStore.data.map { prefs ->
        prefs[CURRENT_ROUTE_KEY] ?: "all"
    }

    val viewModeFlow: Flow<ViewMode> = dataStore.data.map { prefs ->
        prefs[VIEW_MODE_KEY]?.let { ViewMode.valueOf(it) } ?: ViewMode.LIST
    }

    val groupByCategoryFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[GROUP_BY_CATEGORY_KEY] ?: false
    }

    val sortOrderFlow: Flow<SortOrder> = dataStore.data.map { prefs ->
        prefs[SORT_ORDER_KEY]?.let { SortOrder.valueOf(it) } ?: SortOrder.NEWEST
    }

    val themeModeFlow: Flow<ThemeMode> = dataStore.data.map { prefs ->
        prefs[THEME_MODE_KEY]?.let { ThemeMode.valueOf(it) } ?: ThemeMode.SYSTEM
    }

    val dynamicColorFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DYNAMIC_COLOR_KEY] ?: true
    }

    suspend fun setTreeUri(uri: String) {
        dataStore.edit { prefs ->
            prefs[TREE_URI_KEY] = uri
        }
    }

    suspend fun setCurrentRoute(route: String) {
        dataStore.edit { prefs ->
            prefs[CURRENT_ROUTE_KEY] = route
        }
    }

    suspend fun setViewMode(mode: ViewMode) {
        dataStore.edit { prefs ->
            prefs[VIEW_MODE_KEY] = mode.name
        }
    }

    suspend fun setGroupByCategory(group: Boolean) {
        dataStore.edit { prefs ->
            prefs[GROUP_BY_CATEGORY_KEY] = group
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        dataStore.edit { prefs ->
            prefs[SORT_ORDER_KEY] = order.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[DYNAMIC_COLOR_KEY] = enabled
        }
    }
}
