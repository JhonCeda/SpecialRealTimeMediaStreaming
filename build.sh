#!/bin/bash

# Build script for the Encryption Framework
# Compiles all Java source files and prepares for execution

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$SCRIPT_DIR/build"
SRC_DIR="$SCRIPT_DIR"

echo "========================================"
echo "  Encryption Framework Build System"
echo "========================================"
echo

# Check Java is available
if ! command -v javac &> /dev/null; then
    echo "Error: javac not found. Please install Java JDK."
    exit 1
fi

# Parse command line arguments
case "${1:-build}" in
    build)
        echo "Compiling Java source files..."
        mkdir -p "$BUILD_DIR"
        
        # Compile all Java files
        javac -d "$BUILD_DIR" \
            "$SRC_DIR/encryption/StreamEncryption.java" \
            "$SRC_DIR/encryption/EncryptionConfig.java" \
            "$SRC_DIR/encryption/AESGCMEncryption.java" \
            "$SRC_DIR/encryption/ChaCha20Poly1305Encryption.java" \
            "$SRC_DIR/encryption/DPRGStreamCipherEncryption.java" \
            "$SRC_DIR/encryption/EncryptionFactory.java" \
            "$SRC_DIR/hjStreamServer/hjStreamServer.java" \
            "$SRC_DIR/hjUDPproxy/hjUDPproxy.java" \
            "$SRC_DIR/EncryptedStreamReceiver.java" \
            "$SRC_DIR/EncryptionKeyGenerator.java" \
            "$SRC_DIR/StreamDecryptor.java"
        
        if [ $? -ne 0 ]; then
            echo "Build failed!"
            exit 1
        fi
        
        echo "Build successful!"
        echo "Compiled classes in: $BUILD_DIR"
        ;;
        
    keygen)
        echo "Running Key Generator..."
        cd "$BUILD_DIR"
        java EncryptionKeyGenerator
        ;;
        
    run-server)
        echo "Running Stream Server..."
        cd "$BUILD_DIR"
        java -cp . hjStreamServer.hjStreamServer localhost:6666 "$SRC_DIR/config.properties"
        ;;
        
    run-proxy)
        echo "Running UDP Proxy..."
        cd "$BUILD_DIR"
        java -cp . hjUDPproxy.hjUDPproxy localhost:6666 localhost:8888 "$SRC_DIR/hjUDPproxy/config.properties"
        ;;
        
    run-receiver)
        echo "Running Encrypted Stream Receiver..."
        if [ -z "$2" ]; then
            echo "Usage: $0 run-receiver <output-file>"
            exit 1
        fi
        cd "$BUILD_DIR"
        java EncryptedStreamReceiver localhost:8888 "$2" "$SRC_DIR/config.properties"
        ;;
        
    decrypt)
        echo "Running Stream Decryptor..."
        if [ -z "$2" ] || [ -z "$3" ]; then
            echo "Usage: $0 decrypt <input-file> <output-file>"
            exit 1
        fi
        cd "$BUILD_DIR"
        java StreamDecryptor "$2" "$3" "$SRC_DIR/config.properties"
        ;;
        
    clean)
        echo "Cleaning build directory..."
        rm -rf "$BUILD_DIR"
        echo "Clean complete!"
        ;;
        
    test)
        echo "Running tests..."
        if [ ! -f "$SRC_DIR/TESTING_GUIDE.sh" ]; then
            echo "Error: TESTING_GUIDE.sh not found"
            exit 1
        fi
        bash "$SRC_DIR/TESTING_GUIDE.sh"
        ;;
        
    *)
        echo "Usage: $0 {build|keygen|run-server|run-proxy|run-receiver|decrypt|test|clean}"
        echo
        echo "Commands:"
        echo "  build              - Compile all Java source files"
        echo "  keygen             - Generate encryption keys"
        echo "  run-server         - Start the stream server"
        echo "  run-proxy          - Start the UDP proxy"
        echo "  run-receiver FILE  - Receive and decrypt stream to FILE"
        echo "  decrypt INPUT OUT  - Decrypt captured encrypted stream"
        echo "  test               - Run comprehensive test suite"
        echo "  clean              - Remove build directory"
        exit 1
        ;;
esac
