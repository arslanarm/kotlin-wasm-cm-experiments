import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import org.jetbrains.kotlin.incremental.createDirectory

plugins {
    kotlin("multiplatform") version "2.0.255-SNAPSHOT"
}

repositories {
    mavenLocal()
    mavenCentral()
}

kotlin {
    wasmWasi {
        binaries.executable()
        nodejs()

    }
}

 tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinJsCompile>().configureEach {
     kotlinOptions.freeCompilerArgs += listOf(
         "-Xwasm-use-traps-instead-of-exceptions",
         "-language-version", "2.0",
         "-Xwasm-initialize-in-start-function",
     )
 }

val witPath = "${projectDir}/wit"
val wasmDeps = listOf(
    "example.wasm"
)
val wasiAdapter = "${projectDir}/wasi_snapshot_preview1.proxy.wasm"

tasks {
    create("composeWasmComponent") {
        doLast {
            File("${projectDir}/build/out/wasm").run {
                createDirectory()
            }
            File("${projectDir}/build/out/component").run {
                createDirectory()
            }
            exec {
                workingDir("${projectDir}/build/")
                executable("wasm-tools")
                args(
                    "component",
                    "embed",
                    witPath,
                    "compileSync/wasmWasi/main/productionExecutable/kotlin/${project.name}-wasm-wasi.wasm",
                    "-o",
                    "out/wasm/${project.name}.embedded.wasm"
                )
            }
            exec {
                workingDir("${projectDir}/build/")
                executable("wasm-tools")
                args(
                    "component",
                    "new",
                    "out/wasm/${project.name}.embedded.wasm",
                    "-o",
                    "out/component/${project.name}.uncomposed.wasm",
                    "--adapt",
                    wasiAdapter,
                    "--realloc-via-memory-grow"
                )
            }
            exec {
                workingDir("${projectDir}/build/out")
                executable("wasm-tools")
                args(
                    "compose",
                    "component/${project.name}.uncomposed.wasm",
                    "-o",
                    "component/${project.name}.wasm",
                    "--definitions",
                    "dependencies/${wasmDeps.get(0)}",
                    "--search-path",
                    "dependencies"
                )
            }
        }
    }

    create("witBindgen") {
        doFirst {
            exec {
                workingDir(projectDir)
                executable("wit-bindgen")
                args(
                    "kotlin",
                    witPath,
                    "--out-dir",
                    "src/wasmWasiMain/kotlin/bindings"
                )
            }
            exec {
                workingDir(projectDir)
                executable("java")
                args(
                    "-jar",
                    "./ktfmt-0.47-jar-with-dependencies.jar",
                    "./src/wasmWasiMain/kotlin/bindings"
                )
            }
        }
    }
}