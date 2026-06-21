#!/bin/bash

echo "=== FORMAT STRING ANALYSIS ==="
echo ""

for category in actors items journal levels misc plants scenes ui windows; do
    eng_file="${category}/${category}.properties"
    pl_file="${category}/${category}_pl.properties"
    
    if [ -f "$eng_file" ] && [ -f "$pl_file" ]; then
        echo "[$category]"
        
        # Extract lines with format strings
        grep -E '%[0-9$]*[sdxfA-Z]' "$eng_file" > /tmp/eng_format.txt 2>/dev/null || true
        grep -E '%[0-9$]*[sdxfA-Z]' "$pl_file" > /tmp/pl_format.txt 2>/dev/null || true
        
        if [ -s /tmp/eng_format.txt ]; then
            eng_count=$(wc -l < /tmp/eng_format.txt)
            pl_count=$(wc -l < /tmp/pl_format.txt)
            
            echo "  Format strings: EN=$eng_count, PL=$pl_count"
            
            # Check for mismatches
            while IFS= read -r line; do
                key=$(echo "$line" | cut -d'=' -f1)
                eng_val=$(echo "$line" | cut -d'=' -f2-)
                eng_formats=$(echo "$eng_val" | grep -oE '%[0-9$]*[sdxfA-Z]' | sort | tr '\n' ',' | sed 's/,$//')
                
                pl_line=$(grep "^${key}=" "$pl_file" 2>/dev/null)
                if [ -n "$pl_line" ]; then
                    pl_val=$(echo "$pl_line" | cut -d'=' -f2-)
                    pl_formats=$(echo "$pl_val" | grep -oE '%[0-9$]*[sdxfA-Z]' | sort | tr '\n' ',' | sed 's/,$//')
                    
                    if [ "$eng_formats" != "$pl_formats" ]; then
                        echo "    MISMATCH: $key"
                        echo "      EN: $eng_formats"
                        echo "      PL: $pl_formats"
                    fi
                fi
            done < /tmp/eng_format.txt
        fi
        echo ""
    fi
done
