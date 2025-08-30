# KAPT Java Module System Fix

## Issue Description
The project was experiencing a `java.lang.IllegalAccessError` when using KAPT (Kotlin Annotation Processing Tool) with JDK 9+ due to Java module system restrictions.

**Error Message:**
```
java.lang.IllegalAccessError: KaptJavaCompiler cannot access com.sun.tools.javac.main.JavaCompiler
superclass access check failed: class org.jetbrains.kotlin.kapt3.base.javac.KaptJavaCompiler (in unnamed module) cannot access class com.sun.tools.javac.main.JavaCompiler (in module jdk.compiler) because module jdk.compiler does not export com.sun.tools.javac.main to unnamed module
```

## Root Cause
Starting with JDK 9, the Java module system (Project Jigsaw) restricts access to internal APIs. KAPT needs to access internal javac classes that are not exported by the `jdk.compiler` module to unnamed modules.

## Solution
Added JVM export arguments to `gradle.properties` to allow KAPT access to required javac internal classes:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 --add-exports jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-exports jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
```

### Explanation of Export Arguments:
- `--add-exports jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED`: Exports javac main classes
- `--add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED`: Exports javac utility classes  
- `--add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED`: Exports javac AST tree classes
- `--add-exports jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED`: Exports javac code model classes

## KAPT Usage in Project
The project uses KAPT for:
- **Room Database**: Annotation processing for DAOs and database classes
- **Hilt Dependency Injection**: Annotation processing for dependency injection

## Environment
- **JDK Version**: 17.0.16 (Eclipse Adoptium)
- **Gradle Version**: 8.11.1  
- **Kotlin Version**: 1.8.0
- **KAPT Dependencies**: Room 2.5.0, Hilt 2.44.2

## Alternative Solutions
If this fix doesn't work, consider:
1. Migrating from KAPT to KSP (Kotlin Symbol Processing) for better performance and compatibility
2. Using a JDK version < 9 (not recommended for new projects)
3. Updating to newer versions of Kotlin and annotation processing libraries

## References
- [Kotlin KAPT Documentation](https://kotlinlang.org/docs/kapt.html)
- [Java Module System (JPMS) Documentation](https://docs.oracle.com/javase/9/docs/api/java.base/module-summary.html)
- [JetBrains Issue Tracker: KT-36743](https://youtrack.jetbrains.com/issue/KT-36743)