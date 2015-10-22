#!/bin/bash

curl -v -H 'Content-type: application/octet-stream' \
    --user superadmin:system \
    -X POST \
    --data-binary @riscoss-platform-dm-ui-mainwiki-all.xar \
    'http://localhost:8080/riscoss/rest/wikis/xwiki?backup=true&history=RESET'

curl -v \
    --user superadmin:system \
    -X POST \
    --data 'content=%7B%7Bgroovy%7D%7DString%20x%3D%22wiki1%22%3Bprintln(services.wiki.createWiki(x%2Cx%2C%22xwiki%3AXWiki.Admin%22%2Ctrue))%3B%7B%7B%2Fgroovy%7D%7D' \
    'http://localhost:8080/riscoss/bin/view/Main/preview//?xpage=plain&action_preview=1'

echo 'xwiki-enterprise-ui-mainwiki-all-6.0.1.xar
riscoss-wiki-config.xar
riscoss-wiki-ui.xar' | while read xar
do
    curl -v -H 'Content-type: application/octet-stream' \
        --user superadmin:system \
        -X POST \
        --data-binary "@${xar}" \
        'http://localhost:8080/riscoss/rest/wikis/wiki1?backup=true&history=RESET'
done
