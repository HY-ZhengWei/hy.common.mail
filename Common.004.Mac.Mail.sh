#!/bin/sh

cd ./bin


rd /s/q .\org\hy\common\mail\junit


jar cvfm hy.common.mail.jar MANIFEST.MF META-INF org

cp hy.common.mail.jar ..
rm hy.common.mail.jar
cd ..





cd ./src
jar cvfm hy.common.mail-sources.jar MANIFEST.MF META-INF org 
cp hy.common.mail-sources.jar ..
rm hy.common.mail-sources.jar
cd ..
