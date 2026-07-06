#!/bin/sh
set -e

MODEL="${MODEL:-llama3.2:3b}"

ollama serve &
OLLAMA_PID=$!

echo "Waiting for Ollama API to be ready..."
attempts=0
max_attempts=60
until ollama list >/dev/null 2>&1; do
    attempts=$((attempts + 1))
    if [ "$attempts" -ge "$max_attempts" ]; then
        echo "Ollama API did not become ready after ${max_attempts} attempts."
        kill "$OLLAMA_PID"
        exit 1
    fi
    sleep 1
done
echo "Ollama API is ready."

if ! ollama list | grep -q "^${MODEL} "; then
    echo "Model $MODEL not found. Pulling..."
    ollama pull "$MODEL"
    echo "Model pulled successfully."
else
    echo "Model $MODEL already present."
fi

echo "Ollama is ready and serving model $MODEL."
wait "$OLLAMA_PID"
