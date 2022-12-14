#!/bin/sh

TARGETDIR="../todotxt"

if [ ! -e ${TARGETDIR} ]; then
    mkdir ${TARGETDIR}
fi

rm -rf ${TARGETDIR}/files

cp *.tid *.info plugin.js ${TARGETDIR}
mkdir ${TARGETDIR}/files
cp -r public/js/*.js ${TARGETDIR}/files
cp public.files ${TARGETDIR}/files/tiddlywiki.files
