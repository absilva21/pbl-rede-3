import sys
import threading
import socket

UDPClientSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
endereço = ("127.0.0.1",7000)

#recebi as mensagens da caixa de entrada
def input():
    UDPServerSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)
    UDPServerSocket.bind(("127.0.0.1", 9000))
    buffer = UDPServerSocket.recvfrom(4096)
    resposta = buffer[0].decode("utf-8")
    print(resposta) 

if len(sys.argv) > 2:
    #envia mensagem
    if sys.argv[1] == "send":
       buffer = str.encode("send "+sys.argv[2]+" "+sys.argv[3])
       UDPClientSocket.sendto(buffer, endereço)
    else:
        print("não entendi o comando")   
elif len(sys.argv) == 2:
    #solicita mensagens recebidas
    if sys.argv[1] == "input":
        t = threading.Thread(target= input,args=())
        t.start()
        buffer = str.encode("input")
        UDPClientSocket.sendto(buffer, endereço)
    else:  
       print("não entendi o comando") 
else:
    print("não entendi o comando")  
    
