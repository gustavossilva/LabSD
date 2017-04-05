import socket 

s = socket.socket()
host = "localhost"
port = 12346

s.connect((host,port))
while True:
	str = raw_input("Digite sua mensagem: ")
	s.send(str)
	print s.recv(1024)
s.close()
