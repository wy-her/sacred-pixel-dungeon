#!/bin/bash

# Function to extract keys from properties file
extract_keys() {
    grep -E '^\s*[^=]+=.*' "$1" | sed 's/=.*//' | sed 's/^\s*//'
}

# Function to check for empty values
check_empty_values() {
    local file="$1"
    echo "  Checking for empty values..."
    grep -nE '^\s*[^=]+=\s*$' "$file"
}

# Function to check for duplicate keys
check_duplicates() {
    local file="$1"
    echo "  Checking for duplicate keys..."
    grep -E '^\s*[^=]+=.*' "$file" | sed 's/=.*//' | sed 's/^\s*//' | sort | uniq -d
}

# Function to check missing keys
check_missing_keys() {
    local en_file="$1"
    local ko_file="$2"
    
    local en_keys=$(extract_keys "$en_file")
    local ko_keys=$(extract_keys "$ko_file")
    
    echo "  Checking for missing keys in Korean..."
    while read key; do
        if ! echo "$ko_keys" | grep -q "^${key}$"; then
            echo "    MISSING: $key"
        fi
    done <<< "$en_keys"
}

# Main loop through all Korean files
for ko_file in */*_ko.properties; do
    en_file="${ko_file/_ko/}"
    
    if [ ! -f "$en_file" ]; then
        echo "ERROR: English file not found: $en_file"
        continue
    fi
    
    echo ""
    echo "========== $ko_file =========="
    
    # Count lines
    en_count=$(grep -cE '^\s*[^=]+=.*' "$en_file" || true)
    ko_count=$(grep -cE '^\s*[^=]+=.*' "$ko_file" || true)
    echo "English: $en_count keys | Korean: $ko_count keys"
    
    check_empty_values "$ko_file"
    
    dups=$(check_duplicates "$ko_file")
    if [ -n "$dups" ]; then
        echo "  DUPLICATES FOUND:"
        echo "$dups"
    else
        echo "  No duplicates found"
    fi
    
    check_missing_keys "$en_file" "$ko_file"
done
