# ktvisitor-kastree

#### private use with kastree in idea, in order to get package and method


#### USAGE:

```
var callback: ISourceClassCallback = ISourceClassCallback {
        it.forEach() { println( it) }
    }
var inputFile = File("kotlin file")
var instance = KtSourceAnalysisVisitor(callback)
instance.doVisitor(inputFile)
instance.doVisitorEnd()
    
```
