package com.example.domain

data class Category(
    val id: String,
    val name: String,
    val color: Int,
    val iconName: String,
    val matchType: MatchType,
    val rules: List<Rule>
) {
    fun matches(file: FileItem): Boolean {
        if (rules.isEmpty()) return false
        return if (matchType == MatchType.AND) {
            rules.all { it.matches(file) }
        } else {
            rules.any { it.matches(file) }
        }
    }
}

enum class MatchType { AND, OR }

sealed class Rule {
    abstract val id: String
    abstract fun matches(file: FileItem): Boolean
    
    data class NameContains(override val id: String, val text: String) : Rule() {
        override fun matches(file: FileItem) = file.displayName.contains(text, ignoreCase = true)
    }
    data class ExtensionIs(override val id: String, val extensions: List<String>) : Rule() {
        override fun matches(file: FileItem) = extensions.any { it.equals(file.extension, ignoreCase = true) }
    }
    data class NameStartsWith(override val id: String, val text: String) : Rule() {
        override fun matches(file: FileItem) = file.displayName.startsWith(text, ignoreCase = true)
    }
    data class NameEndsWith(override val id: String, val text: String) : Rule() {
        override fun matches(file: FileItem) = file.displayName.endsWith(text, ignoreCase = true)
    }
}
