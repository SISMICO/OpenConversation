#!/usr/bin/env bash
set -euo pipefail

# Create the local audio bind-mount directory with ownership matching the
# non-root backend container user. Run this once per clone (or whenever the
# directory is deleted) before `docker compose up`.
#
# Usage:
#   infra/setup-audio-volume.sh
#
# The UID/GID default to 1000 and can be overridden via environment variables
# defined in infra/.env.

AUDIO_UID="${AUDIO_STORAGE_UID:-1000}"
AUDIO_GID="${AUDIO_STORAGE_GID:-1000}"
DIR="${1:-./audios}"

mkdir -p "$DIR"

if ! chown -R "${AUDIO_UID}:${AUDIO_GID}" "$DIR" 2>/dev/null; then
    echo "Owner change failed; retrying with sudo..."
    sudo chown -R "${AUDIO_UID}:${AUDIO_GID}" "$DIR"
fi

chmod 750 "$DIR"
echo "Prepared $DIR with ownership ${AUDIO_UID}:${AUDIO_GID}"
