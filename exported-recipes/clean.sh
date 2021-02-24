#!/bin/sh
for i in *.json; do jq < $i > $i.fixed; done
