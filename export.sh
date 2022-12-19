#!/bin/sh

TARGETDIR="../todotxt"

if [ ! -e ${TARGETDIR} ]; then
    mkdir -p ${TARGETDIR}
fi

rm -rf ${TARGETDIR}/files

cp *.tid *.info *.js ${TARGETDIR}
mkdir ${TARGETDIR}/files
cp -r public/js/*.js ${TARGETDIR}/files
cp public.files ${TARGETDIR}/files/tiddlywiki.files
