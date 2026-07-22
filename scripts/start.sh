#!/bin/bash
set -euo pipefail

systemctl daemon-reload
systemctl enable moa-api
systemctl restart moa-api
