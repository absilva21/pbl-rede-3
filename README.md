 # pbl-rede-3
 
  Aplicativo de troca de mensagens p2p versão 2, foi proposto o aprimoramento do código anterior para uma versão 
com uma difusão atômica de mensagens e a criação de um serviço de processamento da troca de mensagens, separado do processo do usuário, o projeto foi desenvolvido escrito em **Java** e executando em imagem docker.
  A estratégia de envio das mensagens é a difusão por muticast e a atomicidade pôde ser parcialmente garantida através 
da implementação de um algoritmo com **Nack**, onde os nós ao perceberem falta de uma sequência de mensagens propagam a todos os nós 
o pedido de retransmissão, somente um dos nós responde o pedido. O projeto só foi implementado até esse processo, portanto os nós exibem as mensagens mesmo sem garantir que todas as estão contidas nos nós.

![nack](https://github.com/absilva21/pbl-rede-3/assets/83670712/62e294fd-8a1c-42fc-a130-8ca76474a8ad)


# Protocolo de comunicação entre os nós

Os nós se comunicam com trocas de JSON usando socket multicast na rede local.

![multicast](https://github.com/absilva21/pbl-rede-3/assets/83670712/307a64c0-f3b9-4c09-b6c7-c0b512082b47)


Existem dois tipos de informação transmitida: **comando**, **mensagem**

## Comando 

### Pedido de nack
Quando um nó percebi a falta de alguma mensagem ele solicita aos nós as mensagens em falta

```
{"fouls":[vetor de faltas],"comNumber":número do camando=2,"origemID":id do nó,"origem":ip do nó solicitante,"type":tipo de mensagem="com","nackID":id do nack daquele nó}

```
### Resposta de Nack

  Existe uma fila de solicitações de retransmissão, em um tempo aleatório cada nó tenta responder a solicitação mais antiga ou seja primeira da fila, 
  entre esse momento outro nó já pode ter respondido a mensagem, por isso quando uma resposta é propagada os outros nós devem marcar o objeto da solicitação Nack
  como respondida em um atributo booleano, por isso ao ler uma solicitação para responder, o nó deve observar se esse atributo é **false** ou **true**
  se for falso responde a solicitação, se for verdadeiro não responde.

```
  {"comNumber":número do camando=3,"origem":ip do nó solicitante,"type":tipo de mensagem="com","nackId":id do nack daquele nó,"nodeID":id do nó solicitante,"mens":[vetor com mensagens perdidas]}
```

### Mensagem
Aqui está o layout de envio de mensagem

```
{"idm":id da menssagem,"nomeOrigem":nome do usuário,"grupo":nome do grupo","origem":ip de origem,"tempo":[relogio lógico],"id":id do nó,"type":"men","body":corpo da mensagem}
````

# Detalhes sobre o serviço
 O serviço executa em uma container **Docker**, além de ser o servidor que processa o funcionamento da solução descentralizada, ele serve como uma caixa de mensagens. Para acessa-lo foi criado um script em **Python** que pode enviar mensagens para o servidor via loopback da máquina e também visualizar a caixa de mensagens da mesma forma. 

## Uso de threads
Foi necessário o uso de algumas threads que fazem papeis importantes para o funcionamento do sistema

### Difusão de mensagens

Existe uma thread responsável pela difusão em multicast das mensagens e também pelo recebimento essa thread é iniciada ao se instânciar um objeto da classe grupo.

### Resposta de solicitações de retransmissão
Essa thread analisa uma **fila** de solicitações **nacks** acada intervalo aleatório de tempo e busca respoder a primeira solicitação da fila caso ela não tenha sido respondida.

### Comunicação entre o servido e o script de envio e recebimento

A comunicação entre o serviço e o script em **Python** é feita através de uma thread que é iniciada ao executar o serviço.


## Comandos para o script

### Envio 

```
zaps.py send [nome do usuário] [mensagem]
```
**OBS**: sem [ ]

### Visualizar caixa de mensagens

```
zaps.py input
```

## Referêncial teórico
Foram utilizados slides do professor Lau Cheuk Lung da Universidade Federal de Santa Catarina, as imagens presentes nesse relatório também pertencem ao mesmo.

[Comunicação de Grupo: Disfusão Confiável e Atômica ](https://www.inf.ufsc.br/~frank.siqueira/INE5418/Lau/1s-%20ComGrupo.pdf)

## Ferramentas

* Kit de desenvolvimento Java JDK 17.0.8.7-hotspost
* Python 3
* Visual studio Code
* Eclipse ide 2021-09
* Docker

