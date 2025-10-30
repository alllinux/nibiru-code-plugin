#!/bin/bash

# Network Diagnostic Script for Nibiru Plugin Build
# Tests connectivity to required repositories

echo "============================================"
echo "Nibiru Plugin - Network Diagnostic"
echo "============================================"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

test_connectivity() {
    local url=$1
    local name=$2

    echo -n "Testing $name... "

    # Try curl first
    if command -v curl &> /dev/null; then
        if curl -I --connect-timeout 5 --max-time 10 "$url" &> /dev/null; then
            echo -e "${GREEN}✓ OK${NC}"
            return 0
        else
            echo -e "${RED}✗ FAILED${NC}"
            return 1
        fi
    # Fall back to wget
    elif command -v wget &> /dev/null; then
        if wget --spider --timeout=5 --tries=1 "$url" &> /dev/null; then
            echo -e "${GREEN}✓ OK${NC}"
            return 0
        else
            echo -e "${RED}✗ FAILED${NC}"
            return 1
        fi
    else
        echo -e "${YELLOW}? SKIPPED (no curl/wget)${NC}"
        return 2
    fi
}

echo "1. Testing Basic Connectivity"
echo "------------------------------"
test_connectivity "https://www.google.com" "Internet (Google)"
test_connectivity "https://www.github.com" "GitHub"
echo ""

echo "2. Testing Required Repositories"
echo "---------------------------------"
GRADLE_PLUGINS=$(test_connectivity "https://plugins.gradle.org" "Gradle Plugin Portal"; echo $?)
MAVEN_CENTRAL=$(test_connectivity "https://repo1.maven.org" "Maven Central"; echo $?)
MAVEN_CENTRAL_ALT=$(test_connectivity "https://repo.maven.apache.org" "Maven Central (alt)"; echo $?)
JETBRAINS=$(test_connectivity "https://cache-redirector.jetbrains.com" "JetBrains Repository"; echo $?)
echo ""

echo "3. Checking Proxy Settings"
echo "--------------------------"
if [ -n "$HTTP_PROXY" ] || [ -n "$http_proxy" ]; then
    echo "HTTP Proxy: ${HTTP_PROXY:-$http_proxy}"
else
    echo "HTTP Proxy: Not set"
fi

if [ -n "$HTTPS_PROXY" ] || [ -n "$https_proxy" ]; then
    echo "HTTPS Proxy: ${HTTPS_PROXY:-$https_proxy}"
else
    echo "HTTPS Proxy: Not set"
fi

if [ -n "$NO_PROXY" ] || [ -n "$no_proxy" ]; then
    echo "No Proxy: ${NO_PROXY:-$no_proxy}"
else
    echo "No Proxy: Not set"
fi
echo ""

echo "4. Checking Gradle Proxy Configuration"
echo "---------------------------------------"
if [ -f ~/.gradle/gradle.properties ]; then
    if grep -q "systemProp.http.proxy" ~/.gradle/gradle.properties; then
        echo "Gradle proxy configured in ~/.gradle/gradle.properties"
        grep "systemProp.*proxy" ~/.gradle/gradle.properties | sed 's/Password=.*/Password=***/'
    else
        echo "No proxy configuration in ~/.gradle/gradle.properties"
    fi
else
    echo "~/.gradle/gradle.properties does not exist"
fi
echo ""

echo "5. Testing DNS Resolution"
echo "-------------------------"
for host in plugins.gradle.org repo1.maven.org cache-redirector.jetbrains.com; do
    echo -n "Resolving $host... "
    if host $host &> /dev/null || nslookup $host &> /dev/null; then
        echo -e "${GREEN}✓ OK${NC}"
    else
        echo -e "${RED}✗ FAILED${NC}"
    fi
done
echo ""

echo "============================================"
echo "Diagnosis Summary"
echo "============================================"
echo ""

# Determine the issue
if [ $GRADLE_PLUGINS -ne 0 ] || [ $MAVEN_CENTRAL -ne 0 ]; then
    echo -e "${RED}❌ PROBLEM DETECTED${NC}"
    echo ""
    echo "Your system cannot reach the required repositories."
    echo ""
    echo "Possible causes:"
    echo "  1. Corporate firewall blocking repository access"
    echo "  2. Proxy configuration needed"
    echo "  3. DNS resolution issues"
    echo "  4. Network restrictions"
    echo ""
    echo "Recommended solutions:"
    echo ""

    if [ -z "$HTTP_PROXY" ] && [ -z "$http_proxy" ]; then
        echo "  → If you're behind a corporate proxy, configure it:"
        echo "    export HTTP_PROXY=http://proxy.company.com:8080"
        echo "    export HTTPS_PROXY=http://proxy.company.com:8080"
        echo ""
        echo "    Then add to ~/.gradle/gradle.properties:"
        echo "    systemProp.http.proxyHost=proxy.company.com"
        echo "    systemProp.http.proxyPort=8080"
        echo "    systemProp.https.proxyHost=proxy.company.com"
        echo "    systemProp.https.proxyPort=8080"
        echo ""
    fi

    echo "  → Use GitHub Actions to build (see solution below)"
    echo "  → Contact IT to whitelist: *.gradle.org, *.maven.org, *.jetbrains.com"
    echo "  → Build on a different machine with unrestricted access"
    echo ""
    echo "For detailed solutions, see: NETWORK_TROUBLESHOOTING.md"

else
    echo -e "${GREEN}✓ Network connectivity looks good!${NC}"
    echo ""
    echo "Repositories are reachable. The build should work."
    echo ""
    echo "If build still fails, try:"
    echo "  gradle buildPlugin --refresh-dependencies"
    echo "  gradle buildPlugin --debug > build.log 2>&1"
fi

echo ""
echo "============================================"
