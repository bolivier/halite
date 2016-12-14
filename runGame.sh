#!/bin/bash

lein uberjar

# ./halite -d "30 30" "java -cp target/MyBot.jar MyBot" "java -cp target/MyBot.jar RandomBot"

# myBot2 is the previous iteration of my bot 
./halite -d "30 30" "java -cp target/MyBot.jar MyBot" "java -cp target/MyBot.jar MyBot2"
