package com.korina.cleanarch.idea.ui

import com.korina.cleanarch.idea.ui.model.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.DefaultCellEditor
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.table.TableColumn

/**
 * Two-tab dialog that collects the full feature schema from the developer.
 *
 * Tab 1 — **Schema**: feature name, package base, module routing, fields table.
 * Tab 2 — **Options**: DI framework, storage engine, API client, endpoints table,
 *          generation flags.
 *
 * Call [getSchema] after [showAndGet] returns `true` to retrieve the validated state.
 */
class FeatureSchemaDialog(project: Project) : DialogWrapper(project) {

    // ── Backing state ────────────────────────────────────────────────────────
    private val uiSchema = UiFeatureSchema()

    // ── Schema tab controls ──────────────────────────────────────────────────
    private val featureNameField  = JBTextField(24)
    private val packageBaseField  = JBTextField("com.korina.myapp", 24)
    private val domainModuleField = JBTextField(":domain", 12)
    private val dataModuleField   = JBTextField(":data",   12)
    private val appModuleField    = JBTextField(":app",    12)

    private val fieldTableModel   = FieldTableModel(uiSchema.fields)
    private val fieldTable        = JBTable(fieldTableModel)

    // ── Options tab controls ─────────────────────────────────────────────────
    private val diCombo      = ComboBox(DiFrameworkUi.values())
    private val storageCombo = ComboBox(StorageEngineUi.values())
    private val tableNameField = JBTextField(16)
    private val apiCombo     = ComboBox(ApiClientUi.values())

    private val endpointTableModel = EndpointTableModel(uiSchema.endpoints)
    private val endpointTable      = JBTable(endpointTableModel)

    private val useCasePerEndpointCheck = JBCheckBox("Generate a UseCase per endpoint", false)
    private val coroutinesFlowCheck     = JBCheckBox("Wrap list returns in Flow<>",     true)
    private val addSerializableCheck    = JBCheckBox("Add @Serializable to DTO",         true)

    // ── Init ─────────────────────────────────────────────────────────────────
    init {
        title = "Generate Clean Architecture Feature"
        setOKButtonText("Generate")
        configureFieldTables()
        init()
    }

    // ── DialogWrapper API ─────────────────────────────────────────────────────

    override fun createCenterPanel(): JComponent {
        val tabs = JBTabbedPane()
        tabs.addTab("Schema",  createSchemaTab())
        tabs.addTab("Options", createOptionsTab())
        tabs.preferredSize = Dimension(680, 500)
        return tabs
    }

    override fun doValidate(): ValidationInfo? {
        if (featureNameField.text.isBlank())
            return ValidationInfo("Feature name is required", featureNameField)
        if (!featureNameField.text.matches(Regex("[A-Za-z][A-Za-z0-9]*")))
            return ValidationInfo("Feature name must be alphanumeric (e.g. 'Product')", featureNameField)
        if (packageBaseField.text.isBlank())
            return ValidationInfo("Package base is required", packageBaseField)
        if (fieldTableModel.getFields().isEmpty())
            return ValidationInfo("Add at least one field before generating")
        fieldTableModel.getFields().forEach { f ->
            if (f.name.isBlank())
                return ValidationInfo("All fields must have a name")
        }
        return null
    }

    override fun getPreferredFocusedComponent(): JComponent = featureNameField

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Returns the schema as captured from the dialog controls.
     * Must be called only after [showAndGet] returned `true`.
     */
    fun getSchema(): UiFeatureSchema {
        uiSchema.featureName               = featureNameField.text.trim()
        uiSchema.packageBase               = packageBaseField.text.trim()
        uiSchema.domainModule              = domainModuleField.text.trim()
        uiSchema.dataModule                = dataModuleField.text.trim()
        uiSchema.appModule                 = appModuleField.text.trim()
        uiSchema.storageEngine             = storageCombo.selectedItem as StorageEngineUi
        uiSchema.tableName                 = tableNameField.text.trim()
        uiSchema.apiClient                 = apiCombo.selectedItem as ApiClientUi
        uiSchema.diFramework               = diCombo.selectedItem as DiFrameworkUi
        uiSchema.generateUseCasePerEndpoint = useCasePerEndpointCheck.isSelected
        uiSchema.coroutinesFlow            = coroutinesFlowCheck.isSelected
        uiSchema.addSerializable           = addSerializableCheck.isSelected
        return uiSchema
    }

    // ── Private — Tab builders ────────────────────────────────────────────────

    private fun createSchemaTab(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc   = defaultGbc()

        // Feature name
        panel.add(JBLabel("Feature name:"), gbc.label())
        panel.add(featureNameField,         gbc.field())

        // Package base
        panel.add(JBLabel("Package base:"), gbc.label())
        panel.add(packageBaseField,         gbc.field())

        // Module routing
        panel.add(JBLabel("Domain module:"), gbc.label())
        panel.add(domainModuleField,         gbc.field())

        panel.add(JBLabel("Data module:"),  gbc.label())
        panel.add(dataModuleField,          gbc.field())

        panel.add(JBLabel("App module:"),   gbc.label())
        panel.add(appModuleField,           gbc.field())

        // Fields table with toolbar
        panel.add(JBLabel("Fields:"), gbc.label())
        panel.add(
            ToolbarDecorator.createDecorator(fieldTable)
                .setAddAction    { fieldTableModel.addField() }
                .setRemoveAction { fieldTableModel.removeField(fieldTable.selectedRow) }
                .disableUpDownActions()
                .createPanel()
                .also { it.preferredSize = Dimension(500, 180) },
            gbc.tablePanel()
        )

        return panel
    }

    private fun createOptionsTab(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc   = defaultGbc()

        // DI Framework
        panel.add(JBLabel("DI framework:"), gbc.label())
        panel.add(diCombo,                  gbc.field())

        // Storage
        panel.add(JBLabel("Storage engine:"), gbc.label())
        panel.add(storageCombo,               gbc.field())

        panel.add(JBLabel("Table name:"),   gbc.label())
        panel.add(tableNameField,           gbc.field())

        // API client
        panel.add(JBLabel("API client:"),   gbc.label())
        panel.add(apiCombo,                 gbc.field())

        // Endpoints table
        panel.add(JBLabel("Endpoints:"),   gbc.label())
        panel.add(
            ToolbarDecorator.createDecorator(endpointTable)
                .setAddAction    { endpointTableModel.addEndpoint() }
                .setRemoveAction { endpointTableModel.removeEndpoint(endpointTable.selectedRow) }
                .disableUpDownActions()
                .createPanel()
                .also { it.preferredSize = Dimension(500, 130) },
            gbc.tablePanel()
        )

        // Option checkboxes
        panel.add(JBLabel("Options:"),          gbc.label())
        val checksPanel = JPanel(GridBagLayout())
        val cgbc = defaultGbc()
        checksPanel.add(useCasePerEndpointCheck, cgbc.fullRow())
        checksPanel.add(coroutinesFlowCheck,     cgbc.fullRow())
        checksPanel.add(addSerializableCheck,    cgbc.fullRow())
        panel.add(checksPanel, gbc.field())

        return panel
    }

    // ── Private — cell-editor configuration ──────────────────────────────────

    private fun configureFieldTables() {
        // Type column → combo-box cell editor
        installComboEditor(fieldTable.columnModel.getColumn(FieldTableModel.COL_TYPE),
            FieldTableModel.KOTLIN_TYPES)

        // HTTP method column → combo-box cell editor
        installComboEditor(endpointTable.columnModel.getColumn(EndpointTableModel.COL_METHOD),
            EndpointTableModel.HTTP_METHODS)
    }

    private fun installComboEditor(column: TableColumn, items: Array<String>) {
        val combo = JComboBox(items)
        column.cellEditor = DefaultCellEditor(combo)
    }

    // ── Private — GridBagConstraints helpers ──────────────────────────────────

    private fun defaultGbc() = GridBagConstraints().apply {
        anchor  = GridBagConstraints.WEST
        insets  = Insets(4, 8, 4, 8)
        gridx   = 0
        gridy   = 0
        fill    = GridBagConstraints.NONE
        weightx = 0.0
    }

    private fun GridBagConstraints.label(): GridBagConstraints {
        gridx   = 0
        fill    = GridBagConstraints.NONE
        weightx = 0.0
        val copy = clone() as GridBagConstraints
        gridy++
        return copy
    }

    private fun GridBagConstraints.field(): GridBagConstraints {
        gridx   = 1
        fill    = GridBagConstraints.HORIZONTAL
        weightx = 1.0
        val copy = clone() as GridBagConstraints
        // label() already incremented gridy — step back so field shares the same row.
        copy.gridy = gridy - 1
        return copy
    }

    private fun GridBagConstraints.tablePanel(): GridBagConstraints {
        gridx   = 1
        fill    = GridBagConstraints.BOTH
        weightx = 1.0
        weighty = 1.0
        val copy = clone() as GridBagConstraints
        // label() already incremented gridy — step back so table shares the same row.
        copy.gridy = gridy - 1
        return copy
    }

    private fun GridBagConstraints.fullRow(): GridBagConstraints {
        gridx      = 0
        gridwidth  = 2
        fill       = GridBagConstraints.HORIZONTAL
        weightx    = 1.0
        val copy = clone() as GridBagConstraints
        gridy++
        return copy
    }
}
