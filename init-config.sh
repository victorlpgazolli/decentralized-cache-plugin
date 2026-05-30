#!/bin/sh
set -e

echo "=== Applying custom ipfs configs ==="

ipfs config --json AutoConf.Enabled false
ipfs bootstrap rm --all || true

ipfs config --json DNS.Resolvers '{}'
ipfs config --json Routing.DelegatedRouters '[]'
ipfs config --json Ipns.DelegatedPublishers '[]'
ipfs config --json Swarm.AddrFilters '[]'

ipfs config --json API.HTTPHeaders.Access-Control-Allow-Origin '["*"]'
ipfs config --json API.HTTPHeaders.Access-Control-Allow-Methods '["PUT", "POST", "GET"]'

echo "=== Finished applying configs ==="