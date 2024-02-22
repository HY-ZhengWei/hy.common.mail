

del /Q hy.common.mail.jar
del /Q hy.common.mail-sources.jar


call mvn clean package
cd .\target\classes

rd /s/q .\org\hy\common\mail\junit


jar cvfm hy.common.mail.jar META-INF/MANIFEST.MF META-INF org

copy hy.common.mail.jar ..\..
del /q hy.common.mail.jar
cd ..\..





cd .\src\main\java
xcopy /S ..\resources\* .
jar cvfm hy.common.mail-sources.jar META-INF\MANIFEST.MF META-INF org 
copy hy.common.mail-sources.jar ..\..\..
del /Q hy.common.mail-sources.jar
rd /s/q META-INF
cd ..\..\..

pause