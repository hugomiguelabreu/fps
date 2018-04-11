# Starting manual

## Iniciar o cluster

1. Aceder pasta Core
2. A cada zk-server-# aceder zk-server-1/conf/zoo.cfg e alterar _dataDir_ e _dataLogDir_
3. Abrir 3 terminais, em cada zk-server-# aceder zk-server-1/bin/ e executar **./zkServer.sh start**
4. Em todos 3 terminais executar **./zkServer.sh status** - dois deles terão de indicar follower e outro master.
5. Executar **./zkCli.sh -server 127.0.0.1:218X**, sendo X 2, 3 e 4 para os servidores 1 2 3, correspondentemente.

## Aceder a Java

1. Criar classe Keeper
2. Usar o comando connect dado os parametro qual o server a ligar ("localhost:2182", "localhost:2183" ou "localhost:2184") e timeout (podem deixar 1000)
3. Usar init() se for primeira vez a usar ZK