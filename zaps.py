import sys
import socket

UDPClientSocket = socket.socket(family=socket.AF_INET, type=socket.SOCK_DGRAM)


if len(sys.argv) > 2:
    if sys.argv[1] == "send":
       buffer = str.encode("send "+sys.argv[2]+" "+sys.argv[3])
    else:
        print("não entendi o comando")   
elif len(sys.argv) == 2:
    if sys.argv[1] == "input":
        buffer = str.encode("input")
    else:  
       print("não entendi o comando") 
else:
    print("não entendi o comando")  
    
