# Serviço distribuído de Publicação e Subscrição de Ficheiros

## Compile in java
```
mvn clean compile assembly:single
```
## Compile Google Procol Buffers
```
protoc -I=protobuf/. --java_out=src/main/java/ protobuf/{name}.proto 
```
