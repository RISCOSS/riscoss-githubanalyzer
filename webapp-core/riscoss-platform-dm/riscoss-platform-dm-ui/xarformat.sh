#!/bin/bash

# Reset <date>, <creationDate> and <contentUpdateDate>
for i in `find . -name '*.xml'`; do sed -i 's+<\(date\|creationDate\|contentUpdateDate\)>.*</\1>+<\1>0</\1>+' $i; done
mvn xar:format
mvn xar:format