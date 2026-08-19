package com.korina.cleanarch.idea.ui

import com.korina.cleanarch.idea.ui.model.UiFieldDef
import javax.swing.table.AbstractTableModel

/**
 * Table model backing the fields [JBTable] in [FeatureSchemaDialog].
 *
 * Columns:
 *  0 → Name     (String, editable)
 *  1 → Type     (String, editable — combo box cell editor applied in the dialog)
 *  2 → Nullable (Boolean, renders as a checkbox)
 */
class FieldTableModel(
    private val fields: MutableList<UiFieldDef>
) : AbstractTableModel() {

    companion object {
        val COLUMNS      = arrayOf("Name", "Type", "Nullable")
        const val COL_NAME     = 0
        const val COL_TYPE     = 1
        const val COL_NULLABLE = 2

        /** Common Kotlin types offered in the Type combo-box cell editor. */
        val KOTLIN_TYPES = arrayOf(
            "String", "Int", "Long", "Double", "Float", "Boolean",
            "Char", "Byte", "Short", "List<String>", "List<Int>"
        )
    }

    // ── TableModel contract ────────────────────────────────────────────────

    override fun getRowCount()                     = fields.size
    override fun getColumnCount()                  = COLUMNS.size
    override fun getColumnName(col: Int)           = COLUMNS[col]
    override fun isCellEditable(row: Int, col: Int) = true

    override fun getColumnClass(col: Int): Class<*> = when (col) {
        COL_NULLABLE -> java.lang.Boolean::class.java
        else         -> String::class.java
    }

    override fun getValueAt(row: Int, col: Int): Any = when (col) {
        COL_NAME     -> fields[row].name
        COL_TYPE     -> fields[row].type
        COL_NULLABLE -> fields[row].nullable
        else         -> ""
    }

    override fun setValueAt(value: Any?, row: Int, col: Int) {
        when (col) {
            COL_NAME     -> fields[row].name     = value?.toString() ?: ""
            COL_TYPE     -> fields[row].type     = value?.toString() ?: "String"
            COL_NULLABLE -> fields[row].nullable = value as? Boolean ?: false
        }
        fireTableCellUpdated(row, col)
    }

    // ── Mutation helpers ───────────────────────────────────────────────────

    fun addField(field: UiFieldDef = UiFieldDef()) {
        fields.add(field)
        fireTableRowsInserted(fields.lastIndex, fields.lastIndex)
    }

    fun removeField(row: Int) {
        if (row in fields.indices) {
            fields.removeAt(row)
            fireTableRowsDeleted(row, row)
        }
    }

    fun getFields(): List<UiFieldDef> = fields.toList()
}
