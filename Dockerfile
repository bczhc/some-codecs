FROM ubuntu

COPY / /some-tools/

WORKDIR /

RUN apt update && \
    export DEBIAN_FRONTEND=noninteractive && \
    apt install openjdk-17-jdk groovy git wget unzip make curl gcc -y

RUN git clone https://github.com/openssl/openssl --depth 1 && \
    cd openssl && \
    git fetch --unshallow && \
    git fetch origin openssl-3.0 && \
    git fetch --all && \
    git checkout openssl-3.0.1

WORKDIR /
RUN mkdir sdk && \
    wget 'https://dl.google.com/android/repository/commandlinetools-linux-8092744_latest.zip' -O tools.zip && \
    unzip tools.zip && \
    yes | ./cmdline-tools/bin/sdkmanager --licenses --sdk_root=./sdk && \
    ./cmdline-tools/bin/sdkmanager --sdk_root=./sdk --install ndk-bundle && \
    ./cmdline-tools/bin/sdkmanager --sdk_root=./sdk --install 'cmake;3.18.1'

WORKDIR /some-tools
RUN git submodule update --init --recursive
RUN echo 'sdk.dir=/sdk' > local.properties && \
    echo 'opensslLib.dir=/openssl' > config.properties && \
    echo 'ndk.target=armeabi-v7a-21,arm64-v8a-29,x86-29,x86_64-29' >> config.properties

# build openssl of all Android ABI
RUN ./build-openssl /openssl

# install Rust
RUN curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs > install && \
    chmod +x install && \
    ./install -y && \
    . ~/.cargo/env && \
    rustup default nightly && \
    rustc --version && \
    ./configure-rust

# build single-Android-ABI App
RUN . ~/.cargo/env && \
    mkdir /apks && \
    for a in armeabi-v7a-21 arm64-v8a-29 x86-29 x86_64-29; do \
      # reconfigure
      echo 'opensslLib.dir=/openssl' > config.properties && \
      echo "ndk.target=$a" >> config.properties && \
      ./gradlew asD && \
      cp app/build/outputs/apk/debug/app-debug.apk /apks/$a.apk; \
    done

# build universal-Android-ABI App
RUN . ~/.cargo/env && \
    echo 'opensslLib.dir=/openssl' > config.properties && \
    echo 'ndk.target=armeabi-v7a-21,arm64-v8a-29,x86-29,x86_64-29' >> config.properties && \
    ./gradlew asD && \
    cp app/build/outputs/apk/debug/app-debug.apk /apks/universal.apk
