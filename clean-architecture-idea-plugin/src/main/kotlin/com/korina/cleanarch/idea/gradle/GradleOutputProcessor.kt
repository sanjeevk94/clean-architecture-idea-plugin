package com.korina.cleanarch.idea.gradle

/**
 * @deprecated No longer used.
 *
 * This object previously parsed stdout from the `generateCleanArchFeature` Gradle task.
 * File generation is now handled entirely by
 * [com.korina.cleanarch.idea.generator.DirectFileGenerator], which writes files
 * directly via Java IO — no Gradle task output to parse.
 *
 * Safe to delete.
 */
@Deprecated("No longer used — generation is handled by DirectFileGenerator")
object GradleOutputProcessor
