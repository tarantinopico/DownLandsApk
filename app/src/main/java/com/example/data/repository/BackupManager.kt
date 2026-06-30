package com.example.data.repository

import com.example.data.preferences.SettingsRepository
import com.example.domain.BackupDTO
import com.example.domain.CategoryDTO
import com.example.domain.RuleDTO
import com.example.domain.SettingsDTO
import com.example.domain.Category
import com.example.domain.MatchType
import com.example.domain.Rule
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class BackupManager(
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun createBackup(): String {
        val categories = categoryRepository.allCategories.first()
        val themeMode = settingsRepository.themeModeFlow.first()
        val dynamicColor = settingsRepository.dynamicColorFlow.first()
        val viewMode = settingsRepository.viewModeFlow.first()
        val groupByCategory = settingsRepository.groupByCategoryFlow.first()
        val sortOrder = settingsRepository.sortOrderFlow.first()

        val backup = BackupDTO(
            categories = categories.map { cat ->
                CategoryDTO(
                    id = cat.id,
                    name = cat.name,
                    color = cat.color,
                    iconName = cat.iconName,
                    matchType = cat.matchType.name,
                    rules = cat.rules.map { rule ->
                        val (type, value) = when (rule) {
                            is Rule.NameContains -> "NAME_CONTAINS" to rule.text
                            is Rule.ExtensionIs -> "EXTENSION_IS" to rule.extensions.joinToString(",")
                            is Rule.NameStartsWith -> "NAME_STARTS_WITH" to rule.text
                            is Rule.NameEndsWith -> "NAME_ENDS_WITH" to rule.text
                        }
                        RuleDTO(id = rule.id, type = type, value = value)
                    }
                )
            },
            settings = SettingsDTO(
                themeMode = themeMode.name,
                dynamicColor = dynamicColor,
                viewMode = viewMode.name,
                groupByCategory = groupByCategory,
                sortOrder = sortOrder.name
            )
        )
        return Json.encodeToString(backup)
    }

    suspend fun restoreBackup(jsonStr: String) {
        val backup = Json.decodeFromString<BackupDTO>(jsonStr)
        
        backup.categories.forEach { catDto ->
            val matchType = runCatching { MatchType.valueOf(catDto.matchType) }.getOrDefault(MatchType.OR)
            val rules = catDto.rules.mapNotNull { ruleDto ->
                when (ruleDto.type) {
                    "NAME_CONTAINS" -> Rule.NameContains(ruleDto.id, ruleDto.value)
                    "EXTENSION_IS" -> Rule.ExtensionIs(ruleDto.id, ruleDto.value.split(","))
                    "NAME_STARTS_WITH" -> Rule.NameStartsWith(ruleDto.id, ruleDto.value)
                    "NAME_ENDS_WITH" -> Rule.NameEndsWith(ruleDto.id, ruleDto.value)
                    else -> null
                }
            }
            val category = Category(
                id = catDto.id,
                name = catDto.name,
                color = catDto.color,
                iconName = catDto.iconName,
                matchType = matchType,
                rules = rules
            )
            categoryRepository.saveCategory(category)
        }

        runCatching {
            settingsRepository.setThemeMode(com.example.data.preferences.ThemeMode.valueOf(backup.settings.themeMode))
            settingsRepository.setDynamicColor(backup.settings.dynamicColor)
            settingsRepository.setViewMode(com.example.data.preferences.ViewMode.valueOf(backup.settings.viewMode))
            settingsRepository.setGroupByCategory(backup.settings.groupByCategory)
            settingsRepository.setSortOrder(com.example.data.preferences.SortOrder.valueOf(backup.settings.sortOrder))
        }
    }
}
