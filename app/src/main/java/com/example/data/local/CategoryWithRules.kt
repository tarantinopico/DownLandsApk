package com.example.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithRules(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val rules: List<CategoryRuleEntity>
)
