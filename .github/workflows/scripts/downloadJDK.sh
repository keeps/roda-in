#!/bin/bash
os="${OS}"

if [[ ${os} == "windows-latest" ]]; then
  os_version="windows"
fi

if [[ ${os} == "ubuntu-latest" ]]; then
  os_version="linux"
fi

if [[ ${os} == "macos-latest" ]]; then
  os_version="mac"
fi

JDK="https://api.adoptopenjdk.net/v3/binary/latest/11/ga/${os_version}/x64/jdk/openj9/large/adoptopenjdk"
ext="tar.gz"
if [[ ${os_version} == "windows" ]]; then
  ext="zip"
fi

JDK_FOLDER="./jdk/${os_version}"
JDK_TARGET="${JDK_FOLDER}/jdk11.${ext}"
ls 
if [ ! -d "$JDK_FOLDER" ]; then
    mkdir -p "${JDK_FOLDER}"
    response=$(curl --write-out %{http_code} -L $JDK -o $JDK_TARGET)

    if [ "${response}" != "200" ]; then
      echo "Error downloading Java"
      exit 1;
    fi

    if [[ ${os_version} == "windows" ]]; then
        unzip ${JDK_TARGET} -d ${JDK_FOLDER} > /dev/null 2>&1
        mv "${JDK_FOLDER}/jdk"*"/"* ${JDK_FOLDER}
        rm -rf "${JDK_FOLDER}/jdk-11.0.10+9"
    else
        tar -xvf $JDK_TARGET -C $JDK_FOLDER --strip-components=1 > /dev/null 2>&1
    fi

    rm -rf $JDK_TARGET
fi
