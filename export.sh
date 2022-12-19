#!/bin/sh

TARGETDIR="target/todotxt"

if [ ! -e ${TARGETDIR} ]; then
    mkdir -p ${TARGETDIR}
fi

rm -rf ${TARGETDIR}/*

cp *.tid *.info *.js ${TARGETDIR}
#cp *.multids ${TARGETDIR}
mkdir ${TARGETDIR}/files
cp -r public/js/*.js ${TARGETDIR}/files
cp public.files ${TARGETDIR}/files/tiddlywiki.files
