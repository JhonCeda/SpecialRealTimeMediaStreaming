#!/bin/bash

# Simple demo: Run encryption pipeline with your .dat files
# Usage: ./demo.sh [moviefile.dat]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build"

# Find a .dat file if not provided
MOVIE_FILE="${1:-$SCRIPT_DIR/hjStreamServer/movies/silent.dat}"

if [ ! -f "$MOVIE_FILE" ]; then
    echo "Error: Movie file not found: $MOVIE_FILE"
    echo "Usage: $0 [path-to-movie.dat]"
    echo "Example: $0 hjStreamServer/movies/silent.dat"
    exit 1
fi

if [ ! -d "$BUILD_DIR" ]; then
    echo "Building project..."
    cd "$SCRIPT_DIR"
    ./build.sh build
fi

echo "========================================"
echo "  Encryption Framework Demo"
echo "========================================"
echo
echo "Movie: $MOVIE_FILE"
echo
echo "Run these in separate terminals:"
echo
echo "Terminal 1:"
echo "  cd $SCRIPT_DIR"
echo "  java -cp build hjStreamServer.hjStreamServer $MOVIE_FILE localhost:6666 config.properties"
echo
echo "Terminal 2:"
echo "  cd $SCRIPT_DIR"
echo "  java -cp build hjUDPproxy.hjUDPproxy localhost:6666 localhost:8888 hjUDPproxy/config.properties"
echo
echo "Terminal 3:"
echo "  cd $SCRIPT_DIR"
echo "  java -cp build EncryptedStreamReceiver localhost:8888 output.bin config.properties"
echo
echo "========================================"
