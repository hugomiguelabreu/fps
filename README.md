# Pub-Sub based P2P file sharing system with offline support

## Compile in java
```
mvn clean compile assembly:single
```
## Compile Google Procol Buffers
```
protoc -I=protobuf/. --java_out=src/main/java/ protobuf/{name}.proto 
```
