#!/bin/sh
set -e

MODEL_DIR=/models
MODEL_FILE="$MODEL_DIR/ggml-base.bin"

if [ ! -f "$MODEL_FILE" ]; then
    echo "Whisper model not found at $MODEL_FILE. Downloading base model..."
    ./models/download-ggml-model.sh base "$MODEL_DIR"
    echo "Model downloaded successfully."
fi

echo "Starting whisper server on 0.0.0.0:8080..."
exec whisper-server --host 0.0.0.0 -m "$MODEL_FILE"
