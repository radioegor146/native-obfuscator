#!/bin/bash
cp ../build/libs/native-obfuscator.jar native-obfuscator.jar
mkdir current
for test_dir in tests/*/ ;
do
    test_id=`basename $test_dir`
    echo "Test #"$test_id
    rm -rf current/*
    java -jar native-obfuscator.jar $test_dir/test.jar current
    cmake current/cpp
    cmake --build current/cpp --config Release
    cp current/cpp/Release/* current/*
    cd current
    java -jar test.jar
    cd ..
done
