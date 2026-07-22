#!/bin/bash
set -euo pipefail

if systemctl is-active --quiet moa-api; then
  systemctl stop moa-api
fi
