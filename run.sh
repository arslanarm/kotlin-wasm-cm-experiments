#!/usr/bin/env bash

COMPILED_WASM=./build/compileSync/wasmWasi/main/productionExecutable/kotlin
MODULE_NAME=kco
WASI_ADAPTER=wasi_snapshot_preview1.proxy.wasm

set -e

rm -rf ./build/out && \
mkdir -p ./build/out/dependencies && \

# Build Rust component
cp wit/example.wit ./rust-component/wit/ && \

(cd ./rust-component/ && \
cargo build --target wasm32-wasip1 --release -q && \
wasm-tools component new ./target/wasm32-wasip1/release/example.wasm -o ../build/out/dependencies/example.wasm --adapt ../$WASI_ADAPTER)

# Generate WIT bindings for Kotlin
# Compile Kotlin code
./gradlew :witBindgen
./gradlew :compileProductionExecutableKotlinWasmWasi -Pkotlin.wasm.stability.nowarn=true && \

./gradlew composeWasmComponent
wasmtime serve -W function-references,gc build/out/component/$MODULE_NAME.wasm

# Transpile component into JS + core Wasm
#npx jco transpile build/out/component/$MODULE_NAME.wasm -o build/out/jco --base64-cutoff 0 -q --map "cm:example/jsiface=./../../../jsiface.mjs"

# Replace a stub module with a real Kotlin core module
#cp $COMPILED_WASM/$MODULE_NAME.wasm build/out/jco/$MODULE_NAME.core.wasm

# Run in Node.js and call ini (main) function
#cp build/out/jco/$MODULE_NAME.js build/out/jco/$MODULE_NAME.mjs

#node run.mjs