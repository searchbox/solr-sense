mvn -T compile package -DskipTests=true
mkdir ../deploy-dir/searchbox-sense
cp -r ./for_package/*  ../deploy-dir/searchbox-sense/
cp ./target/searchbox-sense-1.0-SNAPSHOT.jar  ../deploy-dir/searchbox-sense/searchbox-sense-1.38.jar
cp ./for_package/README*  ../deploy-dir/searchbox-sense
cd ../deploy-dir/searchbox-sense
zip -r searchbox-sense.zip *
mv *.zip ..
cd ..
rm -rf searchbox-sense/
cd /salsasvn/searchbox-sense

