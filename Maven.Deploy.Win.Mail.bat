call mvn install:install-file -Dfile=hy.common.mail.jar                              -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.mail/pom.xml
call mvn install:install-file -Dfile=hy.common.mail-sources.jar -Dclassifier=sources -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.mail/pom.xml

call mvn deploy:deploy-file   -Dfile=hy.common.mail.jar                              -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.mail/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:8081/repository/thirdparty
call mvn deploy:deploy-file   -Dfile=hy.common.mail-sources.jar -Dclassifier=sources -DpomFile=./src/main/resources/META-INF/maven/cn.openapis/hy.common.mail/pom.xml -DrepositoryId=thirdparty -Durl=http://HY-ZhengWei:8081/repository/thirdparty

pause
