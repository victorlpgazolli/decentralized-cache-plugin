#!/bin/bash

echo "🔄 Starting connection between Devs (IPFS)..."

# Wait 3 seconds to ensure the daemons have fully started
sleep 3

# 1. Capture the dynamic IDs from each container
echo "📌 Capturing Peer IDs..."
PEER_ID_A=$(docker exec ipfs-pc-1 ipfs id -f='<id>')
PEER_ID_B=$(docker exec ipfs-pc-2 ipfs id -f='<id>')

if [ -z "$PEER_ID_A" ] || [ -z "$PEER_ID_B" ]; then
    echo "❌ Error: Unable to capture Peer IDs. Are the containers running?"
    exit 1
fi

echo "   Dev A: $PEER_ID_A"
echo "   Dev B: $PEER_ID_B"

# 2. Configure mutual bootstrap (for automatic future reconnections)
echo "🔗 Configuring mutual bootstrap..."
docker exec ipfs-pc-1 ipfs bootstrap add /dns4/ipfs-pc-2/tcp/4001/p2p/$PEER_ID_B > /dev/null
docker exec ipfs-pc-2 ipfs bootstrap add /dns4/ipfs-pc-1/tcp/4001/p2p/$PEER_ID_A > /dev/null

# 3. Force an immediate Swarm connection
echo "⚡ Forcing immediate Swarm connection..."
docker exec ipfs-pc-1 ipfs swarm connect /dns4/ipfs-pc-2/tcp/4001/p2p/$PEER_ID_B

# 4. Validation
echo "✅ Validation: Listing connected peers on Dev A:"
docker exec ipfs-pc-1 ipfs swarm peers

echo "🚀 Decentralized environment ready for cache testing!"

echo "Key list from ipfs-pc-1:"
docker exec ipfs-pc-1 ipfs key list -l

echo "Key list from ipfs-pc-2:"
docker exec ipfs-pc-2 ipfs key list -l