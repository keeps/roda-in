
#! /bin/bash
# Version
NEXT_VERSION=$1

function syntax {
  echo "Syntax:  $1 NEXT_VERSION"
  echo "Example: $1 2.3.0"
}

if [[ -z "$NEXT_VERSION" ]]; then
  syntax "$0"
  exit 1
fi


cat << EOF
################################
# Prepare for next version
################################
EOF

# Updating RODA Maven modules with next version SNAPSHOT
mvn versions:set versions:commit -DnewVersion="$NEXT_VERSION-SNAPSHOT"

# Commit Maven version update
git add -u
git commit -S -m "Setting version $NEXT_VERSION-SNAPSHOT"

# Push commits
git push
