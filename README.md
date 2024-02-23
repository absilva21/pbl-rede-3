 # pbl-rede-3
 
  Aplicativo de troca de mensagens p2p versão 2, foi proposto o aprimoramento do código anterior para uma versão 
com uma difusão atômica de mensagens e a criação de um serviço executando em imagem docker.
  A estratégia de envio das mensagens é a difusão por muticast e a atomicidade pôde parcialmente ser garantida através 
da implementação de um algoritmo com **Nack**, onde os nós ao perceberem falta de uma sequência de mensagens propagam a todos os nós 
o pedido de retransmissão, somente um dos nós responde o pedido.
  O projeto só foi implementado até esse processo, portanto os nós exibem mensagens mesmo sem garantir que  todas as mensagens estão em todos os nós.

# Protocolo de comunicação entre os nós

Os nós se comunicam com trocas de JSON

Existem dois tipos de comandos: **comando**, **mensagem**

## Comando 

### Pedido de nack
Quando um nó percebi a falta de alguma mensagem ele solicita aos nós as mensagens em falta

```
{"fouls":[vetor de faltas],"comNumber":número do camando=2,"origemID":id do nó,"origem":ip do nó solicitante,"type":tipo de mensagem="com","nackID":id do nack daquele nó}

```
### Resposta de Nack

  Existe uma fila de solicitações de retransmissão, em um tempo aleatório cada nó tenta responder a solicitação mais antiga ou seja primeira da fila, 
  entre esse momento outro nó já pode ter respondido a mensagem, por isso quando uma resposta é propagada os outros nós devem marcar o objeto da solicitação Nack
  como respondida em um atributo booleano, por isso ao ler uma solicitação para responder o nó deve observar se esse atributo é **false** ou **true**
  se for falso responde se for verdadeiro não responde


### Mensagem
```
{"idm":id da menssagem,"nomeOrigem":nome do usuário,"grupo":nome do grupo","origem":ip de origem,"tempo":[relogio lógico],"id":id do nó,"type":"men","body":corpo da mensagem}
````


