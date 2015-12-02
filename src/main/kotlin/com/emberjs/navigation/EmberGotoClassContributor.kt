package com.emberjs.navigation

import com.emberjs.icons.EmberIconProvider
import com.emberjs.icons.EmberIcons
import com.emberjs.index.EmberFileIndex
import com.emberjs.resolver.EmberName
import com.emberjs.utils.getEmberModule
import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.DelegatingItemPresentation
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FindSymbolParameters.searchScopeFor

class EmberGotoClassContributor() : ChooseByNameContributor {

    override fun getNames(project: Project, includeNonProjectItems: Boolean) =
            FileBasedIndex.getInstance().getAllKeys(EmberFileIndex.NAME, project).toTypedArray()

    override fun getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean) =
            getItemsByName(name, project, searchScopeFor(project, includeNonProjectItems))

    fun getItemsByName(name: String, project: Project, scope: GlobalSearchScope): Array<NavigationItem> {
        // Query file index for the VirtualFile containing the indexed item
        return FileBasedIndex.getInstance().getContainingFiles(EmberFileIndex.NAME, name, scope)
                .flatMap { convert(it, project) }
                .toTypedArray()
    }

    private fun convert(file: VirtualFile, project: Project): Collection<NavigationItem> {
        val module = file.getEmberModule(project) ?: return listOf()
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return listOf()

        val iconProvider = EmberIconProvider()

        return ModuleRootManager.getInstance(module).contentRoots
                .map { EmberName.from(it, file) }
                .filterNotNull()
                .map {
                    val presentation = DelegatingItemPresentation(psiFile.presentation)
                            .withPresentableText(it.displayName)
                            .withLocationString(null)
                            .withIcon(iconProvider.getIcon(it) ?: DEFAULT_ICON)

                    DelegatingNavigationItem(psiFile).withPresentation(presentation)
                }
    }

    companion object {
        val DEFAULT_ICON = EmberIcons.EMPTY_16
    }
}

