#!/bin/bash

THIS_DIR=`pwd`
TOMCAT_DIR="${THIS_DIR}/`ls | grep '^apache-tomcat-'`"
TOMCAT_LOG="${TOMCAT_DIR}/logs/catalina.out"

TAIL=`which gtail || which tail`
$TAIL --version | grep 'GNU' >/dev/null || die "Could not find GNU Tail";

ls ${TOMCAT_DIR}/webapps/riscoss && rm ${TOMCAT_DIR}/webapps/riscoss;
ls ${TOMCAT_DIR}/webapps/riscoss-rdr && rm ${TOMCAT_DIR}/webapps/riscoss-rdr;
echo $THIS_DIR
ln -s $THIS_DIR/riscoss-platform-rdr-war-*-SNAPSHOT ${TOMCAT_DIR}/webapps/riscoss-rdr
ln -s $THIS_DIR/riscoss-platform-dm-war-*-SNAPSHOT ${TOMCAT_DIR}/webapps/riscoss

#export CATALINA_OUT="/dev/stdout"

export CATALINA_OPTS="\
    -Xmx1024m \
    -XX:MaxPermSize=256m \
    -Dxwiki.data.dir="$THIS_DIR/xwiki.data"
"

$TOMCAT_DIR/bin/catalina.sh jpda run 1>>$TOMCAT_LOG 2>>$TOMCAT_LOG &
JAVA_PID=$!

(
    killjava() {
        kill $JAVA_PID;
        sleep 10;
        kill -9 $JAVA_PID;
        return 0;
    }
    ctr=0
    while :; do
        ctr=$(($ctr+1))
        [ $ctr -gt 20 ] && killjava && exit 1;
        curl --user superadmin:system \
            'http://localhost:8080/riscoss/bin/preview/Sandbox/TestInit?content=works&xpage=plain' \
            2>/dev/null | grep '<p>works</p>' && break;
        sleep 1;
    done
) &
PID=$!

$TAIL -F $TOMCAT_LOG --pid=$PID;
kill -0 $JAVA_PID && echo 'Tomcat started in the background' && exit 0;
echo 'Tomcat failed to start' && exit 1;
