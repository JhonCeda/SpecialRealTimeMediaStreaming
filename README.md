# Encryption Framework for UDP Streaming

Real-time encryption for UDP media streams with three cryptographic algorithms.

## What It Has

- **AES-GCM**
- **ChaCha20-Poly1305**
- **DPRG-XOR**

All algorithms provide 256-bit keys, per-packet encryption, and easy algorithm switching via `config.properties`.

## Quick Start

### 1. Build
```bash
./build.sh build
```

### 2. Generate Keys
```bash
./build.sh keygen
```
Choose an algorithm and copy the key to `config.properties`.

### 3. Run (3 Terminals)

**Terminal 1 - Stream Server:**
```bash
java -cp build hjStreamServer.hjStreamServer hjStreamServer/movies/silent.dat localhost:6666 config.properties
```

**Terminal 2 - UDP Proxy:**
```bash
java -cp build hjUDPproxy.hjUDPproxy localhost:6666 localhost:8888 hjUDPproxy/config.properties
```

**Terminal 3 - Receiver:**
```bash
java -cp build EncryptedStreamReceiver localhost:8888 output.bin config.properties
```

## Configuration

Edit `config.properties`:
```properties
encryption.algorithm=aes-gcm           # or chacha20-poly1305 or dprg-xor
encryption.key=0f1e2d3c4b5a...         # 64-char hex (256-bit key)
encryption.seed=                        # Optional
```

Or use templates:
```bash
cp config.properties.aes-gcm config.properties
cp config.properties.chacha20 config.properties
cp config.properties.dprg config.properties
```

## Project Structure

```
SpecialRealTimeMediaStreaming/
├── encryption/                 # Core algorithms (pluggable)
│   ├── StreamEncryption.java
│   ├── EncryptionFactory.java
│   ├── AESGCMEncryption.java
│   ├── ChaCha20Poly1305Encryption.java
│   └── DPRGStreamCipherEncryption.java
│
├── hjStreamServer/             # Stream server with encryption
├── hjUDPproxy/                 # UDP proxy with encryption
├── EncryptedStreamReceiver.java
├── EncryptionKeyGenerator.java
├── StreamDecryptor.java
├── build.sh                    # Build automation
└── config.properties           # Configuration file
```

## Utilities

- **EncryptionKeyGenerator**: Interactive key generator for all 3 algorithms
- **EncryptedStreamReceiver**: Real-time UDP receiver with decryption
- **StreamDecryptor**: Offline batch decryption for testing
- **TESTING_GUIDE.sh**: Automated test suite


## Build Commands

```bash
./build.sh build              # Compile all source
./build.sh keygen             # Generate encryption keys
./build.sh test               # Run algorithm tests
./build.sh clean              # Remove build directory
```



