package com.example.data.repository

import android.graphics.Color
import com.example.data.local.CategoryDao
import com.example.data.local.CategoryEntity
import com.example.data.local.CategoryRuleEntity
import com.example.domain.Category
import com.example.domain.MatchType
import com.example.domain.Rule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class CategoryRepository(private val dao: CategoryDao) {

    val allCategories: Flow<List<Category>> = dao.getAllCategoriesWithRules().map { list ->
        list.map { it.toDomain() }
    }

    suspend fun saveCategory(category: Category) = withContext(Dispatchers.IO) {
        dao.insertCategory(
            CategoryEntity(
                id = category.id,
                name = category.name,
                color = category.color,
                iconName = category.iconName,
                matchType = category.matchType.name
            )
        )
        dao.deleteRulesForCategory(category.id)
        dao.insertRules(category.rules.map { rule ->
            val (type, value) = when (rule) {
                is Rule.NameContains -> "NAME_CONTAINS" to rule.text
                is Rule.ExtensionIs -> "EXTENSION_IS" to rule.extensions.joinToString(",")
                is Rule.NameStartsWith -> "NAME_STARTS_WITH" to rule.text
                is Rule.NameEndsWith -> "NAME_ENDS_WITH" to rule.text
            }
            CategoryRuleEntity(
                id = rule.id,
                categoryId = category.id,
                ruleType = type,
                ruleValue = value
            )
        })
    }

    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        dao.deleteCategory(
            CategoryEntity(
                id = category.id,
                name = category.name,
                color = category.color,
                iconName = category.iconName,
                matchType = category.matchType.name
            )
        )
    }

    suspend fun initDefaultCategoriesIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getCategoryCount() == 0) {
            val defaults = listOf(
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Apps",
                    color = Color.parseColor("#4CAF50"),
                    iconName = "Smartphone",
                    matchType = MatchType.OR,
                    rules = listOf(
                        Rule.ExtensionIs(UUID.randomUUID().toString(), listOf("apk")),
                        Rule.NameContains(UUID.randomUUID().toString(), "apk")
                    )
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Bank statements",
                    color = Color.parseColor("#2196F3"),
                    iconName = "AccountBalance",
                    matchType = MatchType.OR,
                    rules = listOf(
                        Rule.NameContains(UUID.randomUUID().toString(), "vypis_uctu"),
                        Rule.NameContains(UUID.randomUUID().toString(), "statement")
                    )
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Images",
                    color = Color.parseColor("#9C27B0"),
                    iconName = "Image",
                    matchType = MatchType.OR,
                    rules = listOf(
                        Rule.ExtensionIs(UUID.randomUUID().toString(), listOf("jpg", "jpeg", "png", "gif", "webp"))
                    )
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Documents",
                    color = Color.parseColor("#FF9800"),
                    iconName = "Description",
                    matchType = MatchType.OR,
                    rules = listOf(
                        Rule.ExtensionIs(UUID.randomUUID().toString(), listOf("pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx"))
                    )
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Archives",
                    color = Color.parseColor("#795548"),
                    iconName = "FolderZip",
                    matchType = MatchType.OR,
                    rules = listOf(
                        Rule.ExtensionIs(UUID.randomUUID().toString(), listOf("zip", "rar", "7z", "tar", "gz"))
                    )
                ),
                Category(
                    id = UUID.randomUUID().toString(),
                    name = "Installers",
                    color = Color.parseColor("#607D8B"),
                    iconName = "Build",
                    matchType = MatchType.OR,
                    rules = listOf(
                        Rule.ExtensionIs(UUID.randomUUID().toString(), listOf("exe", "msi", "dmg", "pkg", "deb", "rpm"))
                    )
                )
            )
            defaults.forEach { saveCategory(it) }
        }
    }
}

private fun com.example.data.local.CategoryWithRules.toDomain(): Category {
    return Category(
        id = this.category.id,
        name = this.category.name,
        color = this.category.color,
        iconName = this.category.iconName,
        matchType = MatchType.valueOf(this.category.matchType),
        rules = this.rules.mapNotNull { ruleEntity ->
            when (ruleEntity.ruleType) {
                "NAME_CONTAINS" -> Rule.NameContains(ruleEntity.id, ruleEntity.ruleValue)
                "EXTENSION_IS" -> Rule.ExtensionIs(ruleEntity.id, ruleEntity.ruleValue.split(",").map { it.trim() })
                "NAME_STARTS_WITH" -> Rule.NameStartsWith(ruleEntity.id, ruleEntity.ruleValue)
                "NAME_ENDS_WITH" -> Rule.NameEndsWith(ruleEntity.id, ruleEntity.ruleValue)
                else -> null
            }
        }
    )
}
