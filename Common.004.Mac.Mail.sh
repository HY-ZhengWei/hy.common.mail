#!/bin/sh

cd ./bin

jar cvfm hy.common.mail.jar MANIFEST.MF META-INF org

cp hy.common.mail.jar ..
rm hy.common.mail.jar
cd ..

