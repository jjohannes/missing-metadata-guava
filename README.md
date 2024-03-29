> **⚠️ The functionality of this plugin has been integrated into 'org.gradlex.java-ecosystem-capabilities'**  
> Preferably use the [org.gradlex.java-ecosystem-capabilities](https://github.com/jjohannes/java-ecosystem-capabilities) plugin in your build.

A Gradle plugin that adds additional metadata to all releases of the [Google Guava](https://github.com/google/guava) library.

For more background, have a look at [this blog post](https://blog.gradle.org/guava).

## How to use the plugin

Add this plugin dependency to the build file of your [convention plugin](https://docs.gradle.org/release-nightly/samples/sample_convention_plugins.html) build
(e.g. `build-logic/build.gradle(.kts)` or `buildSrc/build.gradle(.kts)`).

```
dependencies {
    implementation("de.jjohannes.gradle:missing-metadata-guava:31.1.1")
}
```

Apply the plugin in a convention plugin you use in all your Java/Android/JVM Gradle (sub)projects:

```
plugins {
  id("de.jjohannes.missing-metadata-guava")
}
```

There is no further configuration required.

## Supported Guava releases

The latest release of this plugin can deal with all Guava releases up to **31.1**.

## Effect of the plugin

The plugin adds a 
[Component Metadata Rule](https://docs.gradle.org/current/userguide/component_metadata_rules.html)
and a 
[Capability Conflict Resolution Strategy](https://docs.gradle.org/current/userguide/dependency_capability_conflict.html#sub:selecting-between-candidates)
to automatically resolve conflicts between different variants and versions of Guava.
Have a look at [this blog post](https://blog.gradle.org/guava) for what these conflicts are and why they may not be resolved correctly if this plugin is not used.

## Making sure the plugin works

To verify that this plugin has the desired effect, you may want to check which Jars end up on your compile or runtime classpath.
Note that the dependency report alone can be a bit confusing, as it shows the Guava "version strings" that contain the suffix "-jre" or "-android".
With this plugin however, the selected version is independent of the actual Guava variant (jar) that gets selected in the end.

This `gradle dependencyInsight --configuration runtimeClasspath --dependency :guava:` report, for example, shows that the JRE (standard-jvm) variant of Guava was selected –
`org.gradle.jvm.environment = standard-jvm` – although the version String is _28.1-android_.

```
com.google.guava:guava:28.1-android
   variant "standardJvmRuntime" [
      org.gradle.jvm.version         = 8 (compatible with: 11)
      org.gradle.jvm.environment     = standard-jvm
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-runtime
      org.gradle.libraryelements     = jar
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
   ]
   Selection reasons:
      - By conflict resolution : between versions 28.1-android, 28.0-jre and 26.0-android
```

This will put `guava-28.1-jre.jar` on the compile classpath, which the report does not show.
You may add a small reporting task yourself if you want to check.
For example:

```
tasks.register("printJars") {
  doLast {
    configurations.compileClasspath.get().files.forEach { println(it.name) }
  }
}
```
