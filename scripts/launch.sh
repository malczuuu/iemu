#!/bin/sh

exec java -Xms64M -Xmx64M -jar build/libs/*.jar "$@"
