

cd .\bin


rd /s/q .\org\hy\common\mail\junit


jar cvfm hy.common.mail.jar MANIFEST.MF META-INF org

copy hy.common.mail.jar ..
del /q hy.common.mail.jar
cd ..





cd .\src
jar cvfm hy.common.mail-sources.jar MANIFEST.MF META-INF org 
copy hy.common.mail-sources.jar ..
del /q hy.common.mail-sources.jar
cd ..
