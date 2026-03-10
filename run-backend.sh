#!/bin/bash

# Set Java 21
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}Starting Rally Backend Server...${NC}"
echo -e "${GREEN}Java version:${NC}"
java -version

echo ""
echo -e "${GREEN}Starting Spring Boot application...${NC}"
mvn spring-boot:run
