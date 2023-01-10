#!/bin/sh

TARGETDIR="target/todotxt"

if [ ! -e ${TARGETDIR} ]; then
    mkdir -p ${TARGETDIR}
fi

rm -rf ${TARGETDIR}/*

cp *.tid *.info *.js ${TARGETDIR}
mkdir ${TARGETDIR}/files
cp -r public/js/*.js ${TARGETDIR}/files
#cp -f public/js/*.map ${TARGETDIR}/files
cp public.files ${TARGETDIR}/files/tiddlywiki.files
