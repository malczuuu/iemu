#!/bin/sh

echo "********************************************************************************"
echo "*                                                                              *"
echo "*           Compiled output from frontend application will be stored           *"
echo "*                   in /src/main/resources/static directory                    *"
echo "*                                                                              *"
echo "********************************************************************************"

cd front/
npm run build
cd ../
rm -rf src/main/resources/static/*
cp front/dist/front/* src/main/resources/static/
