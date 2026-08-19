package com.korina.cleanarch.idea.ui

import com.korina.cleanarch.idea.ui.model.UiEndpointDef
import javax.swing.table.AbstractTableModel

/**
 * Table model backing the endpoints [JBTable] in [FeatureSchemaDialog].
 *
 * Columns:
 *  0 → Name        (String, editable)
 *  1 → HTTP Method (String, combo-box cell editor: GET/POST/PUT/DELETE/PATCH)
 *  2 → Path        (String, editable)
 *  3 → Returns List (Boolean, checkbox)
 */
class EndpointTableModel(
    private val endpoints: MutableList<UiEndpointDef>
) : AbstractTableModel() {

    companion object {
        val COLUMNS           = arrayOf("Name", "Method", "Path", "Returns List")
        const val COL_NAME    = 0
        const val COL_METHOD  = 1
        const val COL_PATH    = 2
        const val COL_LIST    = 3
        val HTTP_METHODS      = arrayOf("GET", "POST", "PUT", "DELETE", "PATCH")
    }

    override fun getRowCount()                      = endpoints.size
    override fun getColumnCount()                   = COLUMNS.size
    override fun getColumnName(col: Int)            = COLUMNS[col]
    override fun isCellEditable(row: Int, col: Int) = true

    override fun getColumnClass(col: Int): Class<*> = when (col) {
        COL_LIST -> java.lang.Boolean::class.java
        else     -> String::class.java
    }

    override fun getValueAt(row: Int, col: Int): Any = when (col) {
        COL_NAME   -> endpoints[row].name
        COL_METHOD -> endpoints[row].httpMethod
        COL_PATH   -> endpoints[row].path
        COL_LIST   -> endpoints[row].returnsList
        else       -> ""
    }

    override fun setValueAt(value: Any?, row: Int, col: Int) {
        when (col) {
            COL_NAME   -> endpoints[row].name        = value?.toString() ?: ""
            COL_METHOD -> endpoints[row].httpMethod  = value?.toString() ?: "GET"
            COL_PATH   -> endpoints[row].path        = value?.toString() ?: "/"
            COL_LIST   -> endpoints[row].returnsList = value as? Boolean ?: false
        }
        fireTableCellUpdated(row, col)
    }

    fun addEndpoint(ep: UiEndpointDef = UiEndpointDef()) {
        endpoints.add(ep)
        fireTableRowsInserted(endpoints.lastIndex, endpoints.lastIndex)
    }

    fun removeEndpoint(row: Int) {
        if (row in endpoints.indices) {
            endpoints.removeAt(row)
            fireTableRowsDeleted(row, row)
        }
    }

    fun getEndpoints(): List<UiEndpointDef> = endpoints.toList()
}
