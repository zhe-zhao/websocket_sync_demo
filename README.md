To run the frontend app we need to install yarn package manager globally. And

```shell
cd front

yarn install
yarn run dev
```

To run the Rust backend we need to install the Rust tool chain (version 1.76.0) first. And then run the Cargo command (
Cargo comes with Rust tool chain by default) 

```shell
cd rust_backend/src

cargo build
cargo run
```

To run the KT backend we need the Kotlin environment
```shell
cd kotlin_backend

docker-compose -f docker-compose.yml up

./gradlew clean build
./gradlew bootRun
```