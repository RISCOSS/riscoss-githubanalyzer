xwiki-platform-legacy-oldcore-6.0-rc-1-objectpolicy.jar has been obtained in the following way:

1) git clone https://github.com/xwiki/xwiki-platform
2) git checkout stable-6.0.X
3) git pull git@github.com:woshilapin/xwiki-platform.git object-policy
4) cd xwiki-platform-core/xwiki-platform-oldcore
5) mvn clean install
6) cd xwiki-platform-legacy/xwiki-platform-legacy-oldcore
7) mvn clean install
8) Install the jar in the local repository patched-jars with the following command:
   
   mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=PATH_TO/xwiki-platform-legacy-oldcore-6.0-rc-1-objectpolicy.jar -DgroupId=org.xwiki.platform -DartifactId=xwiki-platform-legacy-oldcore -Dversion=6.0-rc-1 -Dclassifier=objectpolicy -Dpackaging=jar -DlocalRepositoryPath=PATH_TO/local-repository

