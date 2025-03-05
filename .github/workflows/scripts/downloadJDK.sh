#!/bin/bash

set -ex

os="${OS}"
arch="${ARCH}"

if [[ ${os} == "windows-latest" ]]; then
  os_version="windows"
fi

if [[ ${os} == "ubuntu-latest" ]]; then
  os_version="linux"
fi

if [[ ${os} == "macos-latest" ]]; then
  os_version="mac"
fi

if [[ ${arch} == "x64" ]]; then
  arch_version="x64"
fi

if [[ ${arch} == "arm64" ]]; then
  arch_version="aarch64"
fi

JDK="https://api.adoptium.net/v3/binary/latest/21/ga/${os_version}/${arch_version}/jdk/hotspot/normal/eclipse"
ext="tar.gz"

if [[ ${os_version} == "windows" ]]; then
  ext="zip"
fi

checksumApiPath="https://api.adoptium.net/v3/assets/latest/21/hotspot"
file="./jdk/${os_version}/checksum.json"

JDK_FOLDER="./jdk/${os_version}"
JDK_TARGET="${JDK_FOLDER}/jdk21.${ext}"

if [ ! -d "$JDK_FOLDER" ]; then
    mkdir -p "${JDK_FOLDER}"
    response_checksum=$(curl --write-out %{http_code} -L $checksumApiPath -o $file)
    if [ "${response_checksum}" != "200" ]; then
      echo "Error on get checksum"
      exit 1;
    else
      response=$(curl --write-out %{http_code} -L $JDK -o $JDK_TARGET)

      if [ "${response}" != "200" ]; then
        echo "Error downloading Java"
        exit 1;
      fi

      release_checksum=$(jq -r --arg os_v $os_version --arg arch_v $arch_version '.[].binary | select(.architecture == $arch_v) | select(.heap_size == "normal") | select(.image_type == "jdk") | select(.jvm_impl == "hotspot") | select(.os == $os_v) | .package.checksum'  $file)
      
      if [ "${os_version}" == 'mac' ]; then
       download_checksum=$(shasum -a 256 $JDK_TARGET | cut -d " " -f 1)
      else
       download_checksum=$(sha256sum $JDK_TARGET | cut -d " " -f 1)
      fi
      
      if [ "${release_checksum}" != "${download_checksum}" ]; then
        echo "Error jdk checksum doesn't match with expected checksum"
        rm -rf "${JDK_FOLDER}"
        exit 1;
      fi
      
      rm $file
      
      if [[ ${os_version} == "windows" ]]; then
        unzip ${JDK_TARGET} -d ${JDK_FOLDER} > /dev/null 2>&1
        mv "${JDK_FOLDER}/jdk"*"/"* ${JDK_FOLDER}
        rm -rf "${JDK_FOLDER}/jdk-21.0.10+9"
      else
       tar -xvf $JDK_TARGET -C $JDK_FOLDER --strip-components=1 > /dev/null 2>&1
      fi
      
      rm -rf $JDK_TARGET
    fi
fi