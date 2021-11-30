package analysis

import org.jetbrains.kotlin.psi.*
class KtVisitor : KtVisitorVoid() {


    private var packageList:ArrayList<String> = arrayListOf()


    override fun visitClass(klass: KtClass) {
        klass.name?.let { packageList.add(it) }
        super.visitClass(klass)
    }

    override fun visitModifierList(list: KtModifierList) {
        super.visitModifierList(list)
    }

    fun visitEnd():ArrayList<String>{
        return packageList;
    }
}
