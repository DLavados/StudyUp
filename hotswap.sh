#!/bin/bash

NGINXCONF="/etc/nginx/nginx.conf"
PARAM="$1"

if grep -qE "server\s+$PARAM:" $NGINXCONF; then
  echo "The requested IP is already set"
  exit 1
fi

sed -i -E "s/server.*:/server $PARAM:/" $NGINXCONF
/usr/sbin/nginx -s reload