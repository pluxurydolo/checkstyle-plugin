package com.pluxurydolo

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.quality.Pmd
import org.gradle.api.plugins.quality.PmdExtension

class CheckstylePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.pluginManager.apply('pmd')

        InputStream rulesetStream = getClass().getResourceAsStream('/ruleset.xml')

        File tempDir = new File(project.layout.buildDirectory.get().asFile, 'pmd-config')
        tempDir.mkdirs()
        File rulesetFile = new File(tempDir, 'ruleset.xml')

        rulesetFile.withOutputStream { os ->
            os << rulesetStream
        }

        rulesetStream.close()

        project.extensions.configure(PmdExtension) { ext ->
            ext.toolVersion = '7.18.0'
            ext.consoleOutput = true
            ext.threads.set(8)
            ext.ruleSetFiles = project.files(rulesetFile.absolutePath)
            ext.ruleSets = []
            ext.ignoreFailures = false
        }

        project.tasks.withType(Pmd).configureEach { task ->
            task.group = 'pmd'
            task.source = taskSource(task)
        }
    }

    private static String taskSource(Pmd task) {
        if (task.name == 'pmdMain') {
            return 'src/main/java'
        } else if (task.name == 'pmdTest') {
            return 'src/test/java'
        } else if (task.name == 'pmdIntegrationTest') {
            return 'src/integrationTest/java'
        }
        return ''
    }
}
