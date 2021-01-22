A Gradle plugin that adds additional metadata to all releases of the [Google Guava](https://github.com/google/guava) library.

For more background, have a look at [this blog post](https://blog.gradle.org/guava).

## How to use the plugin

Apply the plugin in your Gradle project:

```
plugins {
  id("de.jjohannes.missing-metadata-guava") version "0.3"
}
```

If you have a multi-project, it is recommended to use a [convention plugin](https://docs.gradle.org/release-nightly/samples/sample_convention_plugins.html) to make sure the plugin is applied to all subprojects that may depend on Guava.

There is no further configuration required.

## Supported Guava releases

The latest release of this plugin can deal with all Guava releases up to **29.0**.

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

This except form `gradle dependencyInsight` for example, shows that the Java 8 variant of Guava was selected – `org.gradle.jvm.version = 8` – although the version String is _28.1-android_.

```
com.google.guava:guava:28.1-android
   variant "jdk8Compile" [
      org.gradle.jvm.version         = 8 (compatible with: 11)
      org.gradle.status              = release (not requested)
      org.gradle.usage               = java-api
      org.gradle.libraryelements     = jar (compatible with: classes)
      org.gradle.category            = library

      Requested attributes not found in the selected variant:
         org.gradle.dependency.bundling = external
   ]
   Selection reasons:
      - By conflict resolution : between versions 28.1-android, 26.0-android and 28.0-android
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
