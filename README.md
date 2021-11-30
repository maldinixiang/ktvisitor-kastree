# ktvisitor-kastree
private use with kastree in idea
USAGE:

```
var callback: ISourceClassCallback = ISourceClassCallback {
        it.forEach() { println( it) }
    }
var inputFile = File("kotlin file")
var instance = KtSourceAnalysisVisitor(callback)
instance.doVisitor(inputFile)
instance.doVisitorEnd()
    
```
