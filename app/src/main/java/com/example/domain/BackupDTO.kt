package com.example.domain

import kotlinx.serialization.Serializable

@Serializable
data class BackupDTO(
    val categories: List<CategoryDTO>,
    val settings: SettingsDTO
)

@Serializable
data class CategoryDTO(
    val id: String,
    val name: String,
    val color: Int,
    val iconName: String,
    val matchType: String,
    val rules: List<RuleDTO>
)

@Serializable
data class RuleDTO(
    val id: String,
    val type: String,
    val value: String
)

@Serializable
data class SettingsDTO(
    val themeMode: String,
    val dynamicColor: Boolean,
    val viewMode: String,
    val groupByCategory: Boolean,
    val sortOrder: String
)
