#!/bin/sh

exec java -Dfile.encoding=UTF-8 -Duser.timezone=UTC -Xms128M -Xmx128M -jar /app.jar
