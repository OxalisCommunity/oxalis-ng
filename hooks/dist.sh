#!/bin/sh

rm -rf dist

version=$(ls oxalis-ng-api/target/ | grep jar | head -1 | sed "s:oxalis-ng\-api\-::" | sed "s:\.jar::")

mkdir -p dist/jars

cp **/target/*.jar dist/jars/
cp */**/target/*.jar dist/jars/

mv dist/jars/oxalis-ng-standalone.jar dist/oxalis-ng-standalone-$version.jar
cp oxalis-ng-dist/**/target/*.war dist/
cp oxalis-ng-dist/**/target/*.zip dist/
cp oxalis-ng-dist/**/target/*.tar.gz dist/

for file in $(ls dist | grep "\-distro"); do
    mv dist/$file dist/$(echo $file | sed "s:\-distro::")
done

for file in $(ls dist | grep "\-full"); do
    mv dist/$file dist/$(echo $file | sed "s:\-full::")
done

zip -j -9 dist/oxalis-ng-jars-$version.zip dist/jars/*.jar
tar -zcvf dist/oxalis-ng-jars-$version.tar.gz -C dist/jars $(ls dist/jars)

rm -rf dist/jars